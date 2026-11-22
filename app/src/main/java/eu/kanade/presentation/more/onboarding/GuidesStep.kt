package eu.kanade.presentation.more.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.OpenInNew
import mihon.icons.materialsymbols.rounded.Autorenew
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.rounded.Explore
import mihon.icons.materialsymbols.rounded.RocketLaunch
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

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

internal class GuidesStep(
    private val onRestoreBackup: () -> Unit,
) : OnboardingStep {

    override val isComplete: Boolean = true

    @Composable
    override fun Content() {
        val handler = LocalUriHandler.current
        val appName = stringResource(MR.strings.app_name)
        var selectedOption by rememberSaveable { mutableStateOf(GuideOption.START_FRESH) }

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
                        .background(SurfaceInsetColor)
                        .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.RocketLaunch,
                        contentDescription = null,
                        tint = EmeraldAccentColor,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Getting Started",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextForegroundColor,
                            letterSpacing = (-0.02).sp,
                        ),
                    )
                    Text(
                        text = "Choose your setup path to start reading or restore your existing library data.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = TextMutedColor,
                        ),
                    )
                }
            }

            GuideChoiceCard(
                isSelected = selectedOption == GuideOption.START_FRESH,
                onSelect = { selectedOption = GuideOption.START_FRESH },
                badgeText = "NEW READER",
                title = "Start Fresh",
                description = "New to $appName? Learn how to configure repositories, add extensions, and organize your reading library.",
                icon = MaterialSymbols.Rounded.Explore,
                ctaText = stringResource(MR.strings.getting_started_guide),
                ctaIcon = MaterialSymbols.AutoMirroredRounded.OpenInNew,
                onCtaClick = {
                    selectedOption = GuideOption.START_FRESH
                    handler.openUri(GETTING_STARTED_URL)
                },
            )

            GuideChoiceCard(
                isSelected = selectedOption == GuideOption.RESTORE_BACKUP,
                onSelect = { selectedOption = GuideOption.RESTORE_BACKUP },
                badgeText = "RETURNING READER",
                title = "Restore Backup",
                description = "Reinstalling $appName? Restore your library, reading history, categories, and settings from a backup file.",
                icon = MaterialSymbols.Rounded.Autorenew,
                ctaText = stringResource(MR.strings.pref_restore_backup),
                ctaIcon = MaterialSymbols.Rounded.Autorenew,
                onCtaClick = {
                    selectedOption = GuideOption.RESTORE_BACKUP
                    onRestoreBackup()
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceInsetColor)
                    .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "You can restore backups or change repository settings anytime later from Settings.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = TextSubtleColor,
                    ),
                )
            }
        }
    }
}

private enum class GuideOption {
    START_FRESH,
    RESTORE_BACKUP,
}

@Composable
private fun GuideChoiceCard(
    isSelected: Boolean,
    onSelect: () -> Unit,
    badgeText: String,
    title: String,
    description: String,
    icon: ImageVector,
    ctaText: String,
    ctaIcon: ImageVector,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.01f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "choice_scale",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) EmeraldAccentColor else CardBorderColor,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "choice_border",
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) SurfaceRaisedColor else CardSurfaceColor,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "choice_bg",
    )
    val iconBg by animateColorAsState(
        targetValue = if (isSelected) EmeraldBgColor else Color(0x0AFFFFFF),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "choice_icon_bg",
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) EmeraldAccentColor else TextMutedColor,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "choice_icon_tint",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect,
            )
            .padding(18.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg)
                        .border(
                            BorderStroke(1.dp, if (isSelected) EmeraldBorderColor else CardBorderColor),
                            RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 1.6.sp,
                            color = if (isSelected) EmeraldAccentColor else TextSubtleColor,
                        ),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            letterSpacing = (-0.015).sp,
                            color = TextForegroundColor,
                        ),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) EmeraldBgColor else Color.Transparent)
                        .border(
                            BorderStroke(1.dp, if (isSelected) EmeraldAccentColor else Color(0x22FFFFFF)),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Check,
                            contentDescription = null,
                            tint = EmeraldAccentColor,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = TextMutedColor,
                ),
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                onClick = onCtaClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) PrimaryCtaColor else Color(0x14FFFFFF),
                    contentColor = if (isSelected) PrimaryCtaTextColor else TextForegroundColor,
                ),
                border = if (isSelected) null else BorderStroke(1.dp, CardBorderColor),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = ctaText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                        ),
                    )
                    Icon(
                        imageVector = ctaIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

const val GETTING_STARTED_URL = "https://mihon.app/docs/guides/getting-started"

@PreviewLightDark
@Composable
private fun GuidesStepPreview() {
    TachiyomiPreviewTheme {
        GuidesStep(
            onRestoreBackup = {},
        ).Content()
    }
}
