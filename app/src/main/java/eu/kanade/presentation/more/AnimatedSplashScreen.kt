package eu.kanade.presentation.more

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.tachiyomi.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

private val CanvasColor = Color(0xFF08090B)
private val CardSurfaceColor = Color(0xFF121316)
private val CardBorderColor = Color(0x12FFFFFF)
private val TextForegroundColor = Color(0xFFF4F4F2)
private val TextMutedColor = Color(0xFF8A8C92)
private val TextSubtleColor = Color(0xFF5F6065)
private val EmeraldAccentColor = Color(0xFFA9BFA9)
private val StatusBadgeBackground = Color(0xFF17181C)
private val RingTrackColor = Color(0x14FFFFFF)

private const val TOTAL_DURATION_MS = 1200L

@Composable
fun AnimatedSplashScreen(
    isReady: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var isSkipped by remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }

    val logoEntryScale = remember { Animatable(0.7f) }
    val exitScale = remember { Animatable(1.0f) }
    val exitAlpha = remember { Animatable(1.0f) }

    val handleSkip = {
        if (!isSkipped && !isExiting) {
            isSkipped = true
            elapsedMs = TOTAL_DURATION_MS
        }
    }

    BackHandler(enabled = true) {
        handleSkip()
    }

    LaunchedEffect(Unit) {
        logoEntryScale.animateTo(
            targetValue = 1.05f,
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        )
        logoEntryScale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )
    }

    LaunchedEffect(isSkipped) {
        if (isSkipped) return@LaunchedEffect
        val startTime = withFrameMillis { it }
        while (elapsedMs < TOTAL_DURATION_MS && !isSkipped) {
            withFrameMillis { now ->
                val diff = now - startTime
                elapsedMs = diff.coerceAtMost(TOTAL_DURATION_MS)
            }
        }
    }

    LaunchedEffect(elapsedMs, isReady, isSkipped) {
        if ((elapsedMs >= TOTAL_DURATION_MS || isSkipped) && isReady && !isExiting) {
            isExiting = true
        }
    }

    LaunchedEffect(isExiting) {
        if (isExiting) {
            coroutineScope {
                launch {
                    exitScale.animateTo(
                        targetValue = 1.15f,
                        animationSpec = keyframes {
                            durationMillis = 350
                            1.0f at 0 using FastOutSlowInEasing
                            0.92f at 100 using FastOutSlowInEasing
                            1.15f at 350 using FastOutSlowInEasing
                        },
                    )
                }
                launch {
                    exitAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 350,
                            easing = FastOutSlowInEasing,
                        ),
                    )
                }
            }
            onDismiss()
        }
    }

    val progress = (elapsedMs.toFloat() / TOTAL_DURATION_MS).coerceIn(0f, 1f)
    val phaseText = when {
        elapsedMs < 450L -> "Initializing engine..."
        elapsedMs < 900L -> "Checking database..."
        else -> "Ready to launch"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = exitAlpha.value
            }
            .background(CanvasColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = handleSkip,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(horizontal = 32.dp)
                .graphicsLayer {
                    scaleX = exitScale.value
                    scaleY = exitScale.value
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 3.5.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)

                    drawArc(
                        color = RingTrackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth),
                    )

                    if (progress > 0f) {
                        drawArc(
                            color = EmeraldAccentColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .graphicsLayer {
                            scaleX = logoEntryScale.value
                            scaleY = logoEntryScale.value
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardSurfaceColor)
                        .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mihon),
                        contentDescription = null,
                        tint = TextForegroundColor,
                        modifier = Modifier.size(54.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Mihon",
                color = TextForegroundColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(StatusBadgeBackground)
                    .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(9999.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(EmeraldAccentColor),
                )
                Text(
                    text = phaseText,
                    color = TextMutedColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = String.format(Locale.US, "%04dms / %04dms", elapsedMs, TOTAL_DURATION_MS),
                color = TextSubtleColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
            )
        }
    }
}
