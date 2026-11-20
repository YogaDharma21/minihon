package eu.kanade.presentation.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity

fun <T> springLowStiffness(): SpringSpec<T> = spring(
    stiffness = Spring.StiffnessMediumLow,
    dampingRatio = Spring.DampingRatioNoBouncy,
)

val itemFadeInSpec: FiniteAnimationSpec<Float> = springLowStiffness()
val itemFadeOutSpec: FiniteAnimationSpec<Float> = springLowStiffness()
val itemPlacementSpec: FiniteAnimationSpec<IntOffset> = spring(
    stiffness = Spring.StiffnessMediumLow,
    dampingRatio = Spring.DampingRatioNoBouncy,
)

val appBarSlideAnimationSpec: FiniteAnimationSpec<IntOffset> = tween(200)
val appBarFadeAnimationSpec: FiniteAnimationSpec<Float> = tween(150)

val topAppBarEnterTransition: EnterTransition =
    slideInVertically(appBarSlideAnimationSpec) { -it } + fadeIn(appBarFadeAnimationSpec)
val topAppBarExitTransition: ExitTransition =
    slideOutVertically(appBarSlideAnimationSpec) { -it } + fadeOut(appBarFadeAnimationSpec)

val bottomAppBarEnterTransition: EnterTransition =
    slideInVertically(appBarSlideAnimationSpec) { it } + fadeIn(appBarFadeAnimationSpec)
val bottomAppBarExitTransition: ExitTransition =
    slideOutVertically(appBarSlideAnimationSpec) { it } + fadeOut(appBarFadeAnimationSpec)

fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = springLowStiffness(),
        label = "bouncyClickableScale",
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
}

class ScrollChromeConnection(
    private val onVisibilityChanged: (Boolean) -> Unit,
    private val thresholdPx: Float = 96f,
) : NestedScrollConnection {

    private var accumulatedDelta = 0f

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val dy = -available.y
        if ((dy > 0f && accumulatedDelta < 0f) || (dy < 0f && accumulatedDelta > 0f)) {
            accumulatedDelta = 0f
        }
        accumulatedDelta += dy

        if (accumulatedDelta >= thresholdPx) {
            onVisibilityChanged(false)
            accumulatedDelta = 0f
        } else if (accumulatedDelta <= -thresholdPx) {
            onVisibilityChanged(true)
            accumulatedDelta = 0f
        }

        return Offset.Zero
    }

    fun reset() {
        accumulatedDelta = 0f
    }
}

class HorizontalSwipeConnection(
    private val onSwipeLeft: () -> Unit,
    private val onSwipeRight: () -> Unit,
    private val thresholdPx: Float = 120f,
    private val velocityThreshold: Float = 800f,
    private val timeProvider: () -> Long = System::currentTimeMillis,
) : NestedScrollConnection {

    private var accumulatedDeltaX = 0f
    private var lastTriggerTime = 0L
    private val cooldownMs = 400L

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (source == NestedScrollSource.UserInput) {
            val now = timeProvider()
            if (now - lastTriggerTime < cooldownMs) {
                return Offset.Zero
            }

            if ((available.x > 0f && accumulatedDeltaX < 0f) || (available.x < 0f && accumulatedDeltaX > 0f)) {
                accumulatedDeltaX = 0f
            }
            accumulatedDeltaX += available.x

            if (accumulatedDeltaX <= -thresholdPx) {
                accumulatedDeltaX = 0f
                lastTriggerTime = now
                onSwipeLeft()
            } else if (accumulatedDeltaX >= thresholdPx) {
                accumulatedDeltaX = 0f
                lastTriggerTime = now
                onSwipeRight()
            }
        }
        return Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val now = timeProvider()
        accumulatedDeltaX = 0f
        if (now - lastTriggerTime >= cooldownMs) {
            val absX = kotlin.math.abs(available.x)
            val absY = kotlin.math.abs(available.y)
            if (absX > absY * 1.25f && absX >= velocityThreshold) {
                lastTriggerTime = now
                if (available.x < 0f) {
                    onSwipeLeft()
                } else {
                    onSwipeRight()
                }
            }
        }
        return Velocity.Zero
    }

    fun reset() {
        accumulatedDeltaX = 0f
        lastTriggerTime = 0L
    }
}
