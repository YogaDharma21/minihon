package eu.kanade.presentation.more.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource

private val CanvasColor = Color(0xFF08090B)
private val CardSurfaceColor = Color(0xFF121316)
private val CardBorderColor = Color(0x12FFFFFF)
private val TextForegroundColor = Color(0xFFF4F4F2)
private val TextMutedColor = Color(0xFF8A8C92)
private val PrimaryCtaColor = Color(0xFFF2F1EC)
private val PrimaryCtaTextColor = Color(0xFF0A0A0B)
private val EmeraldAccentColor = Color(0xFFA9BFA9)
private val DisabledButtonColor = Color(0xFF17181C)
private val DisabledTextColor = Color(0xFF5F6065)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onRestoreBackup: () -> Unit,
) {
    val slideDistance = rememberSlideDistance()

    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val steps = remember {
        listOf(
            StorageStep(),
            PermissionStep(),
            GuidesStep(onRestoreBackup = onRestoreBackup),
        )
    }
    val isLastStep = currentStep == steps.lastIndex

    BackHandler(enabled = currentStep != 0) {
        currentStep--
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CanvasColor,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CanvasColor)
                    .drawBehind {
                        drawLine(
                            color = CardBorderColor,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                    .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = TextForegroundColor,
                        ),
                        modifier = Modifier.defaultMinSize(minWidth = 96.dp, minHeight = 48.dp),
                    ) {
                        Text(
                            text = stringResource(MR.strings.action_webview_back),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(1.dp))
                }

                Button(
                    onClick = {
                        if (isLastStep) {
                            onComplete()
                        } else {
                            currentStep++
                        }
                    },
                    enabled = steps[currentStep].isComplete,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryCtaColor,
                        contentColor = PrimaryCtaTextColor,
                        disabledContainerColor = DisabledButtonColor,
                        disabledContentColor = DisabledTextColor,
                    ),
                    modifier = Modifier.defaultMinSize(minWidth = 112.dp, minHeight = 48.dp),
                ) {
                    Text(
                        text = stringResource(
                            if (isLastStep) {
                                MR.strings.onboarding_action_finish
                            } else {
                                MR.strings.onboarding_action_next
                            },
                        ),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (i in 0 until steps.size) {
                        val animatedProgress by animateFloatAsState(
                            targetValue = if (i <= currentStep) 1f else 0f,
                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                            label = "segment_progress_$i",
                        )
                        val targetColor = if (i < currentStep) EmeraldAccentColor else PrimaryCtaColor
                        val animatedColor by animateColorAsState(
                            targetValue = targetColor,
                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                            label = "segment_color_$i",
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color(0x14FFFFFF)),
                        ) {
                            if (animatedProgress > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                                        .clip(CircleShape)
                                        .background(animatedColor),
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Step ${currentStep + 1} of ${steps.size}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontFeatureSettings = "tnum",
                            color = TextMutedColor,
                            letterSpacing = 0.5.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                    Text(
                        text = when (currentStep) {
                            0 -> "Storage"
                            1 -> "Permissions"
                            else -> "Guides & Restore"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextForegroundColor,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = CardSurfaceColor,
                contentColor = TextForegroundColor,
                border = BorderStroke(1.dp, CardBorderColor),
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        materialSharedAxisX(
                            forward = targetState > initialState,
                            slideDistance = slideDistance,
                        )
                    },
                    label = "stepContent",
                ) { stepIndex ->
                    steps[stepIndex].Content()
                }
            }
        }
    }
}
