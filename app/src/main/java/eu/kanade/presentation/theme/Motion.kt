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
