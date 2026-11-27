package eu.kanade.presentation.util

import android.os.SystemClock
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Collections

const val CASCADE_DURATION_MILLIS: Int = 700
const val CASCADE_STAGGER_MILLIS: Int = 70
const val CASCADE_INITIAL_WAVE_COUNT: Int = 12
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
val LocalCascadeDirection = compositionLocalOf { CascadeDirection.FromBottom }

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

@Stable
internal class CascadeScrollDirectionState(
    initialDirection: CascadeDirection = CascadeDirection.FromBottom,
) {
    var direction: CascadeDirection by mutableStateOf(initialDirection)
        private set

    val connection: CascadeScrollConnection = CascadeScrollConnection { nextDirection ->
        if (direction != nextDirection) {
            direction = nextDirection
        }
    }
}

@Composable
fun ProvideCascadeScrollDirection(
    resetKey: Any? = Unit,
    content: @Composable (Modifier) -> Unit,
) {
    val state = remember(resetKey) { CascadeScrollDirectionState() }
    val scrollModifier = remember(state) { Modifier.nestedScroll(state.connection) }
    CompositionLocalProvider(LocalCascadeDirection provides state.direction) {
        content(scrollModifier)
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

data class CascadeRevealPlan(
    val isInitialCascade: Boolean,
    val initialFraction: Float,
    val remainingDelayMillis: Long,
    val remainingDurationMillis: Int,
)

fun resolveCascadeReveal(
    index: Int,
    startUptimeMillis: Long,
    currentUptimeMillis: Long,
    durationMillis: Long = CASCADE_DURATION_MILLIS.toLong(),
    staggerMillis: Long = CASCADE_STAGGER_MILLIS.toLong(),
): CascadeRevealPlan {
    if (index in 0 until CASCADE_INITIAL_WAVE_COUNT) {
        val timing = calculateCascadeTiming(
            index = index,
            startUptimeMillis = startUptimeMillis,
            currentUptimeMillis = currentUptimeMillis,
            durationMillis = durationMillis,
            staggerMillis = staggerMillis,
        )
        if (!timing.isCompleted) {
            return CascadeRevealPlan(
                isInitialCascade = true,
                initialFraction = timing.initialFraction,
                remainingDelayMillis = timing.remainingDelayMillis,
                remainingDurationMillis = timing.remainingDurationMillis.toInt(),
            )
        }
    }
    return CascadeRevealPlan(
        isInitialCascade = false,
        initialFraction = 0f,
        remainingDelayMillis = 0L,
        remainingDurationMillis = durationMillis.toInt(),
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

    val plan = remember(trigger.id, index) {
        resolveCascadeReveal(
            index = index,
            startUptimeMillis = trigger.startUptimeMillis,
            currentUptimeMillis = defaultUptimeMillis(),
        )
    }

    val animatable = remember(trigger.id, index) { Animatable(plan.initialFraction) }

    LaunchedEffect(trigger.id, index) {
        if (plan.isInitialCascade) {
            if (plan.remainingDelayMillis > 0L) {
                delay(plan.remainingDelayMillis)
            }
            if (plan.remainingDurationMillis > 0) {
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = plan.remainingDurationMillis,
                        easing = CASCADE_EASING,
                    ),
                )
            } else {
                animatable.snapTo(1f)
            }
        } else if (animatable.value < 1f) {
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = plan.remainingDurationMillis,
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
