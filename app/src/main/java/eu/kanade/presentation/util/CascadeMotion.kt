package eu.kanade.presentation.util

import android.os.SystemClock
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Collections
import kotlinx.coroutines.launch

const val CASCADE_DURATION_MILLIS: Int = 800
const val CASCADE_STAGGER_MILLIS: Int = 60
val CASCADE_EASING: Easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
val CASCADE_RISE_DP: Dp = 12.dp

enum class CascadeSurface {
    Library,
    Updates,
    History,
    Sources,
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
) {
    companion object {
        val Disabled = CascadeMotionTrigger(disabled = true)
    }
}

val LocalCascadeMotionTrigger = staticCompositionLocalOf { CascadeMotionTrigger() }

@Composable
fun rememberCascadeMotionTrigger(surface: CascadeSurface): CascadeMotionTrigger {
    return remember(surface) {
        if (CascadeMotionSession.hasPlayed(surface)) {
            CascadeMotionTrigger.Disabled
        } else {
            CascadeMotionSession.markPlayed(surface)
            CascadeMotionTrigger()
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
): Modifier {
    if (trigger.disabled) return this
    if (LocalInspectionMode.current) return this

    val context = LocalContext.current
    val isReducedMotion = remember(context) {
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
    if (isReducedMotion) return this

    val currentUptime = remember(trigger.id) { defaultUptimeMillis() }
    val timing = remember(trigger.id, index) {
        calculateCascadeTiming(
            index = index,
            startUptimeMillis = trigger.startUptimeMillis,
            currentUptimeMillis = currentUptime,
        )
    }

    if (timing.isCompleted) {
        return this
    }

    val density = LocalDensity.current
    val risePx = with(density) { CASCADE_RISE_DP.toPx() }
    val initialAlpha = if (timing.initialFraction > 0f) {
        CASCADE_EASING.transform(timing.initialFraction)
    } else {
        0f
    }
    val initialTranslation = if (timing.initialFraction > 0f) {
        (1f - CASCADE_EASING.transform(timing.initialFraction)) * risePx
    } else {
        risePx
    }

    val alphaAnimatable = remember(trigger.id) { Animatable(initialAlpha) }
    val translationAnimatable = remember(trigger.id) { Animatable(initialTranslation) }

    LaunchedEffect(trigger.id) {
        val duration = timing.remainingDurationMillis.toInt()
        val delay = timing.remainingDelayMillis.toInt()
        val spec = tween<Float>(
            durationMillis = duration,
            delayMillis = delay,
            easing = CASCADE_EASING,
        )
        launch {
            alphaAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = spec,
            )
        }
        launch {
            translationAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = spec,
            )
        }
    }

    return this.graphicsLayer {
        this.alpha = alphaAnimatable.value
        this.translationY = translationAnimatable.value
    }
}
