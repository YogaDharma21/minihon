package eu.kanade.presentation.more.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import eu.kanade.presentation.util.rememberRequestPackageInstallsPermissionState
import eu.kanade.tachiyomi.util.system.launchRequestPackageInstallsPermission
import eu.kanade.tachiyomi.util.system.telemetryIncluded
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.BugReport
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.rounded.Info
import mihon.icons.materialsymbols.rounded.MobileGear
import mihon.icons.materialsymbols.rounded.QueryStats
import mihon.icons.materialsymbols.rounded.Security
import mihon.icons.materialsymbols.roundedfilled.Extension
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

private val CardSurfaceColor = Color(0xFF121316)
private val SurfaceInsetColor = Color(0x09FFFFFF)
private val CardBorderColor = Color(0x12FFFFFF)
private val TextForegroundColor = Color(0xFFF4F4F2)
private val TextMutedColor = Color(0xFF8A8C92)
private val TextSubtleColor = Color(0xFF5F6065)
private val PrimaryCtaColor = Color(0xFFF2F1EC)
private val PrimaryCtaTextColor = Color(0xFF0A0A0B)
private val EmeraldAccentColor = Color(0xFFA9BFA9)
private val EmeraldBgColor = Color(0x1FA9BFA9)
private val EmeraldBorderColor = Color(0x59A9BFA9)

internal class PermissionStep : OnboardingStep {

    private var notificationGranted by mutableStateOf(false)
    private var batteryGranted by mutableStateOf(false)

    override val isComplete: Boolean = true

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val privacyPreferences = remember { context.appGraph.privacyPreferences }
        val lifecycleOwner = LocalLifecycleOwner.current

        val installGranted = rememberRequestPackageInstallsPermissionState()

        val checkLifecyclePermissions = remember(context) {
            {
                notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }
                val powerManager = context.getSystemService<PowerManager>()
                batteryGranted = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
            }
        }

        LaunchedEffect(Unit) {
            checkLifecyclePermissions()
        }

        DisposableEffect(lifecycleOwner.lifecycle) {
            val observer = object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    checkLifecyclePermissions()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        val allPermissionsGranted = installGranted &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || notificationGranted) &&
            batteryGranted

        val avatarBg by animateColorAsState(
            targetValue = if (allPermissionsGranted) EmeraldBgColor else SurfaceInsetColor,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "avatarBg",
        )
        val avatarBorder by animateColorAsState(
            targetValue = if (allPermissionsGranted) EmeraldBorderColor else CardBorderColor,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "avatarBorder",
        )
        val avatarTint by animateColorAsState(
            targetValue = if (allPermissionsGranted) EmeraldAccentColor else TextMutedColor,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "avatarTint",
        )

        val permissionRequester = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = {
                    // Handled on resume
                },
            )
        } else {
            null
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(avatarBg)
                        .border(BorderStroke(1.dp, avatarBorder), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Security,
                        contentDescription = null,
                        tint = avatarTint,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Permissions & Privacy",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextForegroundColor,
                            letterSpacing = (-0.02).sp,
                        ),
                    )
                    Text(
                        text = "Grant access for background updates, downloads, and extension installs.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = TextMutedColor,
                        ),
                    )
                }
            }

            PrivilegeCard(
                title = stringResource(MR.strings.onboarding_permission_install_apps),
                description = stringResource(MR.strings.onboarding_permission_install_apps_description),
                icon = MaterialSymbols.RoundedFilled.Extension,
                granted = installGranted,
                onActionClick = { context.launchRequestPackageInstallsPermission() },
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PrivilegeCard(
                    title = stringResource(MR.strings.onboarding_permission_notifications),
                    description = stringResource(MR.strings.onboarding_permission_notifications_description),
                    icon = MaterialSymbols.Rounded.Info,
                    granted = notificationGranted,
                    onActionClick = { permissionRequester?.launch(Manifest.permission.POST_NOTIFICATIONS) },
                )
            }

            PrivilegeCard(
                title = stringResource(MR.strings.onboarding_permission_ignore_battery_opts),
                description = stringResource(MR.strings.onboarding_permission_ignore_battery_opts_description),
                icon = MaterialSymbols.Rounded.MobileGear,
                granted = batteryGranted,
                onActionClick = {
                    @SuppressLint("BatteryLife")
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    context.startActivity(intent)
                },
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardSurfaceColor,
                border = BorderStroke(1.dp, CardBorderColor),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Security,
                            contentDescription = null,
                            tint = EmeraldAccentColor,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "OFFLINE-FIRST PRIVACY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.2.sp,
                                color = EmeraldAccentColor,
                            ),
                        )
                    }

                    Text(
                        text = "Your library, reading history, and downloaded files stay stored directly on your device. Mihon connects to sources without middleman servers and collects zero personal data.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = TextMutedColor,
                        ),
                    )
                }
            }

            if (telemetryIncluded) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurfaceColor,
                    border = BorderStroke(1.dp, CardBorderColor),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "TELEMETRY & CRASH REPORTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.2.sp,
                                color = TextSubtleColor,
                            ),
                        )

                        val crashlyticsPref = privacyPreferences.crashlytics
                        val crashlytics by crashlyticsPref.collectAsState()

                        val analyticsPref = privacyPreferences.analytics
                        val analytics by analyticsPref.collectAsState()

                        TelemetrySwitchRow(
                            title = stringResource(MR.strings.onboarding_permission_crashlytics),
                            description = stringResource(MR.strings.onboarding_permission_crashlytics_description),
                            icon = MaterialSymbols.Rounded.BugReport,
                            checked = crashlytics,
                            onCheckedChange = crashlyticsPref::set,
                        )

                        HorizontalDivider(
                            color = CardBorderColor,
                            thickness = 1.dp,
                        )

                        TelemetrySwitchRow(
                            title = stringResource(MR.strings.onboarding_permission_analytics),
                            description = stringResource(MR.strings.onboarding_permission_analytics_description),
                            icon = MaterialSymbols.Rounded.QueryStats,
                            checked = analytics,
                            onCheckedChange = analyticsPref::set,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun PrivilegeCard(
        title: String,
        description: String,
        icon: ImageVector,
        granted: Boolean,
        onActionClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val iconBubbleBg by animateColorAsState(
            targetValue = if (granted) EmeraldBgColor else Color(0xFF17181C),
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
            label = "iconBubbleBg",
        )
        val iconBubbleBorder by animateColorAsState(
            targetValue = if (granted) EmeraldBorderColor else CardBorderColor,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
            label = "iconBubbleBorder",
        )
        val iconTint by animateColorAsState(
            targetValue = if (granted) EmeraldAccentColor else TextMutedColor,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
            label = "iconTint",
        )

        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardSurfaceColor,
            border = BorderStroke(1.dp, CardBorderColor),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = !granted,
                        onClick = onActionClick,
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBubbleBg)
                        .border(BorderStroke(1.dp, iconBubbleBorder), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextForegroundColor,
                        ),
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = TextMutedColor,
                        ),
                    )
                }

                AnimatedContent(
                    targetState = granted,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 50)) +
                            scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)))
                            .togetherWith(fadeOut(animationSpec = tween(90)))
                    },
                    label = "privilegeActionState",
                ) { isGranted ->
                    if (isGranted) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldBgColor)
                                .border(BorderStroke(1.dp, EmeraldBorderColor), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Check,
                                contentDescription = null,
                                tint = EmeraldAccentColor,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "Granted",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldAccentColor,
                                ),
                            )
                        }
                    } else {
                        Button(
                            onClick = onActionClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryCtaColor,
                                contentColor = PrimaryCtaTextColor,
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp),
                        ) {
                            Text(
                                text = stringResource(MR.strings.onboarding_permission_action_grant),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TelemetrySwitchRow(
        title: String,
        description: String,
        icon: ImageVector,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onCheckedChange(!checked) },
                )
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF17181C))
                    .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) EmeraldAccentColor else TextMutedColor,
                    modifier = Modifier.size(16.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextForegroundColor,
                    ),
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = TextMutedColor,
                    ),
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF0A0A0B),
                    checkedTrackColor = EmeraldAccentColor,
                    checkedBorderColor = EmeraldAccentColor,
                    uncheckedThumbColor = TextMutedColor,
                    uncheckedTrackColor = Color(0xFF1E2025),
                    uncheckedBorderColor = Color(0x26FFFFFF),
                ),
                modifier = Modifier.scale(0.85f),
            )
        }
    }
}
