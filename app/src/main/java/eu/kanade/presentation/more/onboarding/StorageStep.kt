package eu.kanade.presentation.more.onboarding

import android.content.ActivityNotFoundException
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.more.settings.screen.SettingsDataScreen
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.flow.collectLatest
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.Help
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.Folder
import mihon.icons.materialsymbols.rounded.Save
import mihon.icons.materialsymbols.roundedfilled.Extension
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

private val CardSurfaceColor = Color(0xFF121316)
private val SurfaceRaisedColor = Color(0xFF17181C)
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
private val AmberAccentColor = Color(0xFFD2BD93)
private val AmberBgColor = Color(0x1AD2BD93)
private val AmberBorderColor = Color(0x40D2BD93)

internal class StorageStep : OnboardingStep {

    private var _isComplete by mutableStateOf(false)

    override val isComplete: Boolean
        get() = _isComplete

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val handler = LocalUriHandler.current

        val storagePref = remember { context.appGraph.storagePreferences.baseStorageDirectory }
        val storagePath by storagePref.collectAsState()
        val pickStorageLocation = SettingsDataScreen.storageLocationPicker(storagePref)

        val isLinked = storagePath.isNotBlank()
        val appName = stringResource(MR.strings.app_name)

        val launchPicker: () -> Unit = {
            try {
                pickStorageLocation.launch(null)
            } catch (e: ActivityNotFoundException) {
                context.toast(MR.strings.file_picker_error)
            }
        }

        val avatarBg by animateColorAsState(
            targetValue = if (isLinked) EmeraldBgColor else SurfaceInsetColor,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "avatarBg",
        )
        val avatarBorder by animateColorAsState(
            targetValue = if (isLinked) EmeraldBorderColor else CardBorderColor,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "avatarBorder",
        )
        val avatarTint by animateColorAsState(
            targetValue = if (isLinked) EmeraldAccentColor else TextMutedColor,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "avatarTint",
        )

        val animatedBaseBg by animateColorAsState(
            targetValue = if (isLinked) EmeraldBgColor else Color(0xFF1E2025),
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "vaultBaseBg",
        )
        val animatedBaseBorder by animateColorAsState(
            targetValue = if (isLinked) EmeraldBorderColor else Color(0x1AFFFFFF),
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "vaultBaseBorder",
        )
        val animatedTabBg by animateColorAsState(
            targetValue = if (isLinked) Color(0x2AA9BFA9) else Color(0xFF252830),
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "vaultTabBg",
        )
        val animatedTabBorder by animateColorAsState(
            targetValue = if (isLinked) EmeraldBorderColor else Color(0x1AFFFFFF),
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "vaultTabBorder",
        )
        val animatedPageOffsetY by animateDpAsState(
            targetValue = if (isLinked) 20.dp else 0.dp,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "vaultPageOffsetY",
        )
        val animatedPageRotation by animateFloatAsState(
            targetValue = if (isLinked) 0f else -4f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "vaultPageRotation",
        )
        val animatedPageScale by animateFloatAsState(
            targetValue = if (isLinked) 0.92f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "vaultPageScale",
        )
        val animatedPageBg by animateColorAsState(
            targetValue = if (isLinked) Color(0x28A9BFA9) else Color(0x0AFFFFFF),
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "vaultPageBg",
        )
        val animatedPageBorder by animateColorAsState(
            targetValue = if (isLinked) EmeraldAccentColor else Color(0x33FFFFFF),
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "vaultPageBorder",
        )
        val animatedPageTextColor by animateColorAsState(
            targetValue = if (isLinked) EmeraldAccentColor else TextMutedColor,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "vaultPageTextColor",
        )
        val vaultScale by animateFloatAsState(
            targetValue = if (isLinked) 1.02f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "vaultScale",
        )

        val statusBadgeBg by animateColorAsState(
            targetValue = if (isLinked) EmeraldBgColor else AmberBgColor,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "statusBadgeBg",
        )
        val statusBadgeBorder by animateColorAsState(
            targetValue = if (isLinked) EmeraldBorderColor else AmberBorderColor,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "statusBadgeBorder",
        )
        val statusBadgeIconTint by animateColorAsState(
            targetValue = if (isLinked) EmeraldAccentColor else AmberAccentColor,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "statusBadgeIconTint",
        )
        val statusBadgeTextColor by animateColorAsState(
            targetValue = if (isLinked) TextForegroundColor else TextMutedColor,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "statusBadgeTextColor",
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
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
                        imageVector = MaterialSymbols.Rounded.Folder,
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
                        text = "Select storage location",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextForegroundColor,
                            letterSpacing = (-0.02).sp,
                        ),
                    )
                    Text(
                        text = "Where $appName stores downloaded chapters, auto-backups, and local library data.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = TextMutedColor,
                        ),
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = CardSurfaceColor,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isLinked) Color(0x33A9BFA9) else CardBorderColor,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = vaultScale
                                scaleY = vaultScale
                            }
                            .size(width = 140.dp, height = 110.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = launchPicker,
                            ),
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = 18.dp, y = (-68).dp)
                                .size(width = 44.dp, height = 14.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(animatedTabBg)
                                .border(
                                    BorderStroke(1.dp, animatedTabBorder),
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                                ),
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .graphicsLayer {
                                    translationY = animatedPageOffsetY.toPx()
                                    rotationZ = animatedPageRotation
                                    scaleX = animatedPageScale
                                    scaleY = animatedPageScale
                                }
                                .size(width = 76.dp, height = 60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(animatedPageBg)
                                .border(
                                    BorderStroke(1.dp, animatedPageBorder),
                                    shape = RoundedCornerShape(8.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                if (isLinked) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Check,
                                        contentDescription = null,
                                        tint = animatedPageTextColor,
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                                Text(
                                    text = if (isLinked) "MANGA" else "CHOOSE",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = animatedPageTextColor,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .height(68.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(animatedBaseBg)
                                .border(
                                    BorderStroke(1.dp, animatedBaseBorder),
                                    shape = RoundedCornerShape(12.dp),
                                ),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusBadgeBg)
                            .border(BorderStroke(1.dp, statusBadgeBorder), RoundedCornerShape(12.dp))
                            .clickable(onClick = launchPicker)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Folder,
                            contentDescription = null,
                            tint = statusBadgeIconTint,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = if (isLinked) {
                                SettingsDataScreen.storageLocationText(storagePref)
                            } else {
                                "No storage folder selected"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontFeatureSettings = "tnum",
                                fontSize = 12.sp,
                                color = statusBadgeTextColor,
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (isLinked) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Check,
                                contentDescription = null,
                                tint = EmeraldAccentColor,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        onClick = launchPicker,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryCtaColor,
                            contentColor = PrimaryCtaTextColor,
                        ),
                    ) {
                        Text(
                            text = if (isLinked) {
                                "Change folder"
                            } else {
                                stringResource(MR.strings.onboarding_storage_action_select)
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        onClick = { handler.openUri(SettingsDataScreen.HELP_URL) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = TextForegroundColor,
                        ),
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.AutoMirroredRounded.Help,
                            contentDescription = null,
                            tint = TextMutedColor,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(MR.strings.onboarding_storage_help_action),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = TextForegroundColor,
                            ),
                        )
                    }

                    Text(
                        text = "Android 11+ scoped storage requires dedicated directory selection.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = TextSubtleColor,
                            textAlign = TextAlign.Center,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "WHAT IS STORED HERE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            color = TextSubtleColor,
                        ),
                    )

                    StorageFeatureRow(
                        icon = MaterialSymbols.Rounded.Download,
                        title = "Chapter downloads",
                        description = "Offline manga chapters and cached reading pages",
                    )

                    StorageFeatureRow(
                        icon = MaterialSymbols.Rounded.Save,
                        title = "Backups",
                        description = "Automatic and manual exports of library and settings",
                    )

                    StorageFeatureRow(
                        icon = MaterialSymbols.RoundedFilled.Extension,
                        title = "Extension data",
                        description = "Installed repositories, custom sources, and site state",
                    )
                }
            }
        }

        LaunchedEffect(storagePref) {
            _isComplete = storagePref.isSet()
            storagePref.changes()
                .collectLatest {
                    _isComplete = storagePref.isSet()
                }
        }
    }
}

@Composable
private fun StorageFeatureRow(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceInsetColor)
                .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EmeraldAccentColor,
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
                    lineHeight = 15.sp,
                    color = TextMutedColor,
                ),
            )
        }
    }
}
