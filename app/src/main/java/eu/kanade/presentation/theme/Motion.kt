package eu.kanade.presentation.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.flow.distinctUntilChanged
import tachiyomi.presentation.core.components.ScrollChromeBoundaryEntry
import tachiyomi.presentation.core.components.ScrollChromeObserver

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

val appBarSlideAnimationSpec: FiniteAnimationSpec<IntOffset> = tween(
    durationMillis = 700,
    easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f),
)
val appBarFadeAnimationSpec: FiniteAnimationSpec<Float> = tween(
    durationMillis = 450,
    easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f),
)

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
    private val isAtBoundary: () -> Boolean = { false },
) : NestedScrollConnection {

    private var accumulatedDelta = 0f

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (isAtBoundary()) {
            accumulatedDelta = 0f
            onVisibilityChanged(true)
            return Offset.Zero
        }

        val dy = -consumed.y
        if (dy == 0f) {
            return Offset.Zero
        }

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

@Stable
class ScrollChromeState(
    initialVisible: Boolean = true,
    thresholdPx: Float = 96f,
    private val isPinned: () -> Boolean = { false },
) : ScrollChromeObserver {

    var isVisible by mutableStateOf(initialVisible)
        private set

    private val entries = mutableListOf<ScrollChromeBoundaryEntry>()
    private val pinChecks = mutableListOf<() -> Boolean>()

    val nestedScrollConnection: ScrollChromeConnection = ScrollChromeConnection(
        onVisibilityChanged = { visible ->
            if (visible || (!isPinnedNow() && !isActiveAtBoundary())) {
                isVisible = visible
            }
        },
        thresholdPx = thresholdPx,
        isAtBoundary = { isPinnedNow() || isActiveAtBoundary() },
    )

    fun reveal() {
        isVisible = true
        nestedScrollConnection.reset()
    }

    fun registerPin(pinCheck: () -> Boolean): () -> Unit {
        pinChecks.add(pinCheck)
        return { pinChecks.remove(pinCheck) }
    }

    override fun register(entry: ScrollChromeBoundaryEntry): () -> Unit {
        entries.add(entry)
        return { entries.remove(entry) }
    }

    override fun notifyBoundaryChanged() {
        if (isPinnedNow() || isActiveAtBoundary()) {
            reveal()
        }
    }

    private fun isPinnedNow(): Boolean {
        if (isPinned()) return true
        for (i in pinChecks.indices) {
            if (pinChecks[i]()) return true
        }
        return false
    }

    private fun isActiveAtBoundary(): Boolean {
        var hasActive = false
        for (i in entries.indices) {
            val entry = entries[i]
            if (entry.isActive()) {
                if (entry.isAtBoundary()) return true
                hasActive = true
            }
        }
        return !hasActive
    }
}

val LocalScrollChromeState = staticCompositionLocalOf<ScrollChromeState?> { null }

@Composable
fun rememberScrollChromeState(
    initialVisible: Boolean = true,
    thresholdPx: Float = 96f,
    isPinned: () -> Boolean = { false },
): ScrollChromeState {
    val currentIsPinned by rememberUpdatedState(isPinned)
    val state = remember(thresholdPx) {
        ScrollChromeState(
            initialVisible = initialVisible,
            thresholdPx = thresholdPx,
            isPinned = { currentIsPinned() },
        )
    }
    LaunchedEffect(state) {
        snapshotFlow { currentIsPinned() }
            .distinctUntilChanged()
            .collect { pinned ->
                if (pinned) {
                    state.reveal()
                }
            }
    }
    return state
}

class HorizontalSwipeConnection(
    private val onSwipeLeft: () -> Unit,
    private val onSwipeRight: () -> Unit,
    private val thresholdPx: Float = 100f,
    private val velocityThreshold: Float = 400f,
    private val cooldownMs: Long = 350L,
    private val timeProvider: () -> Long = System::currentTimeMillis,
) : NestedScrollConnection {

    private var accumulatedDeltaX = 0f
    private var lastTriggerTime = 0L

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
