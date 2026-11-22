package eu.kanade.presentation.util

import android.os.SystemClock
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Collections

const val CASCADE_DURATION_MILLIS: Int = 700
const val CASCADE_STAGGER_MILLIS: Int = 70
val CASCADE_EASING: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
val CASCADE_RISE_DP: Dp = 40.dp

enum class CascadeDirection {
    FromBottom,
    FromTop,
}

enum class CascadeSurface {
    Library,
    Updates,
    History,
    Sources,
    Extensions,
}

object CascadeMotionSession {
    private val playedSurfaces = Collections.synchronizedSet(mutableSetOf<CascadeSurface>())

    fun hasPlayed(surface: CascadeSurface): Boolean = surface in playedSurfaces

    fun markPlayed(surface: CascadeSurface) {
        playedSurfaces += surface
    }

    fun resetForTesting() {
        playedSurfaces.clear()
    }
}

internal fun defaultUptimeMillis(): Long {
    return try {
        SystemClock.uptimeMillis()
    } catch (_: Throwable) {
        System.currentTimeMillis()
    }
}

data class CascadeMotionTrigger(
    val id: Long = 0L,
    val startUptimeMillis: Long = defaultUptimeMillis(),
    val disabled: Boolean = false,
    val direction: CascadeDirection = CascadeDirection.FromBottom,
) {
    companion object {
        val Disabled = CascadeMotionTrigger(disabled = true)
    }
}

val LocalCascadeMotionTrigger = staticCompositionLocalOf { CascadeMotionTrigger() }
val LocalCascadeDirection = staticCompositionLocalOf { CascadeDirection.FromBottom }

class CascadeScrollConnection(
    private val onDirectionChanged: (CascadeDirection) -> Unit,
) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val dy = -available.y
        if (dy > 8f) {
            onDirectionChanged(CascadeDirection.FromBottom)
        } else if (dy < -8f) {
            onDirectionChanged(CascadeDirection.FromTop)
        }
        return Offset.Zero
    }
}

@Composable
fun rememberCascadeMotionTrigger(
    surface: CascadeSurface,
    direction: CascadeDirection = CascadeDirection.FromBottom,
    key: Any? = Unit,
): CascadeMotionTrigger {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current
    val isReducedMotion = remember(isInspection) {
        if (isInspection) return@remember true
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f,
            ) == 0f
        } catch (_: Throwable) {
            false
        }
    }
    return remember(surface, isReducedMotion, direction, key) {
        if (isReducedMotion) {
            CascadeMotionTrigger.Disabled
        } else {
            CascadeMotionSession.markPlayed(surface)
            CascadeMotionTrigger(
                id = if (key != Unit && key != null) key.hashCode().toLong() else 0L,
                startUptimeMillis = defaultUptimeMillis(),
                direction = direction,
            )
        }
    }
}

data class CascadeTiming(
    val targetDelayMillis: Long,
    val elapsedMillis: Long,
    val isCompleted: Boolean,
    val remainingDelayMillis: Long,
    val remainingDurationMillis: Long,
    val initialFraction: Float = 0f,
)

fun calculateCascadeTiming(
    index: Int,
    startUptimeMillis: Long,
    currentUptimeMillis: Long,
    durationMillis: Long = CASCADE_DURATION_MILLIS.toLong(),
    staggerMillis: Long = CASCADE_STAGGER_MILLIS.toLong(),
): CascadeTiming {
    val targetDelay = maxOf(0, index) * staggerMillis
    val elapsed = maxOf(0L, currentUptimeMillis - startUptimeMillis)
    val totalDuration = targetDelay + durationMillis
    val isCompleted = elapsed >= totalDuration
    val remainingDelay = if (elapsed < targetDelay) targetDelay - elapsed else 0L
    val remainingDuration = if (isCompleted) {
        0L
    } else if (elapsed <= targetDelay) {
        durationMillis
    } else {
        totalDuration - elapsed
    }
    val initialFraction = if (isCompleted) {
        1f
    } else if (elapsed <= targetDelay) {
        0f
    } else {
        ((elapsed - targetDelay).toFloat() / durationMillis).coerceIn(0f, 1f)
    }
    return CascadeTiming(
        targetDelayMillis = targetDelay,
        elapsedMillis = elapsed,
        isCompleted = isCompleted,
        remainingDelayMillis = remainingDelay,
        remainingDurationMillis = remainingDuration,
        initialFraction = initialFraction,
    )
}

@Composable
fun Modifier.cascadeItemMotion(
    index: Int,
    trigger: CascadeMotionTrigger = LocalCascadeMotionTrigger.current,
    direction: CascadeDirection = LocalCascadeDirection.current,
): Modifier {
    if (trigger.disabled) return this
    if (LocalInspectionMode.current) return this

    val density = LocalDensity.current
    val risePx = remember(density) { with(density) { CASCADE_RISE_DP.toPx() } }

    val staggerIndex = if (index >= 0) index % 6 else 0
    val timing = remember(trigger.id, index) {
        calculateCascadeTiming(
            index = staggerIndex,
            startUptimeMillis = trigger.startUptimeMillis,
            currentUptimeMillis = defaultUptimeMillis(),
            durationMillis = CASCADE_DURATION_MILLIS.toLong(),
            staggerMillis = CASCADE_STAGGER_MILLIS.toLong(),
        )
    }

    val isInitialCascade = !timing.isCompleted
    val initialProgress = if (isInitialCascade) timing.initialFraction else 0f
    val animatable = remember(trigger.id, index) { Animatable(initialProgress) }

    LaunchedEffect(trigger.id, index) {
        if (isInitialCascade) {
            if (timing.remainingDelayMillis > 0L) {
                delay(timing.remainingDelayMillis)
            }
            val remaining = timing.remainingDurationMillis.toInt()
            if (remaining > 0) {
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = remaining,
                        easing = CASCADE_EASING,
                    ),
                )
            } else {
                animatable.snapTo(1f)
            }
        } else if (animatable.value < 1f) {
            val delayMs = (staggerIndex % 3) * CASCADE_STAGGER_MILLIS
            if (delayMs > 0) {
                delay(delayMs.toLong())
            }
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = CASCADE_DURATION_MILLIS,
                    easing = CASCADE_EASING,
                ),
            )
        }
    }

    val effectiveDirection = if (trigger.direction != CascadeDirection.FromBottom) {
        trigger.direction
    } else {
        direction
    }
    val directionSign = if (effectiveDirection == CascadeDirection.FromTop) -1f else 1f

    return this.graphicsLayer {
        val fraction = animatable.value
        alpha = fraction.coerceIn(0f, 1f)
        translationY = directionSign * risePx * (1f - fraction)
        val s = 0.96f + (0.04f * fraction)
        scaleX = s
        scaleY = s
    }
}
