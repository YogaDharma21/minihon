package eu.kanade.presentation.more

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.tachiyomi.R
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

private val CanvasColor = Color(0xFF08090B)
private val CardSurfaceColor = Color(0xFF121316)
private val CardBorderColor = Color(0x12FFFFFF)
private val TextForegroundColor = Color(0xFFF4F4F2)
private val TextMutedColor = Color(0xFF8A8C92)
private val EmeraldAccentColor = Color(0xFFA9BFA9)
private val EmeraldTintColor = Color(0x1FA9BFA9)
private val RingTrackColor = Color(0x0FFFFFFF)

/**
 * Animated splash readiness and migration screen.
 * Follows DESIGN.md charcoal canvas, solid matte surfaces, and muted emerald accents.
 */
@Composable
fun AppMigratingScreen(
    isDatabaseMigrating: Boolean = false,
    isAppMigrating: Boolean = false,
) {
    val stageLabel = when {
        isDatabaseMigrating -> "SYNCING DATABASE"
        isAppMigrating -> "MIGRATING DATA"
        else -> "INITIALIZING"
    }

    SplashReadinessIndicator(
        stage = stageLabel,
        title = stringResource(MR.strings.app_migrating_title),
        summary = stringResource(MR.strings.app_migrating_summary),
    )
}

/**
 * Stage indicator and readiness gauge for app cold start and migrations.
 */
@Composable
fun SplashReadinessIndicator(
    stage: String,
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashReadinessAnimations")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "GaugeRotation",
    )

    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "LogoPulse",
    )

    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "DotAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(148.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(148.dp)) {
                    val strokeWidth = 3.5.dp.toPx()
                    drawCircle(
                        color = RingTrackColor,
                        style = Stroke(width = strokeWidth),
                    )
                    drawArc(
                        color = EmeraldAccentColor,
                        startAngle = rotation,
                        sweepAngle = 110f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(logoScale)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardSurfaceColor)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mihon),
                        contentDescription = null,
                        tint = TextForegroundColor,
                        modifier = Modifier.size(50.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(EmeraldTintColor)
                    .border(BorderStroke(1.dp, Color(0x33A9BFA9)), RoundedCornerShape(9999.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(EmeraldAccentColor.copy(alpha = dotAlpha)),
                )
                Text(
                    text = stage,
                    color = EmeraldAccentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.6.sp,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                color = TextForegroundColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = summary,
                color = TextMutedColor,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 300.dp),
            )
        }
    }
}

