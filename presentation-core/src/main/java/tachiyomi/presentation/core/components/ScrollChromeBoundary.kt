package tachiyomi.presentation.core.components

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.distinctUntilChanged

class ScrollChromeBoundaryEntry(
    val isActive: () -> Boolean,
    val isAtBoundary: () -> Boolean,
)

@Stable
interface ScrollChromeObserver {
    fun register(entry: ScrollChromeBoundaryEntry): () -> Unit
    fun notifyBoundaryChanged()
}

val LocalScrollChromeObserver = staticCompositionLocalOf<ScrollChromeObserver?> { null }

val LocalScrollChromeActive = staticCompositionLocalOf<() -> Boolean> { { true } }

internal fun isScrollableAtBoundary(
    state: ScrollableState,
    hasPendingItems: Boolean = false,
): Boolean = !state.canScrollBackward || (!state.canScrollForward && !hasPendingItems)

@Composable
fun ObserveScrollChromeBoundary(
    state: ScrollableState,
    hasPendingItems: () -> Boolean = { false },
    enabled: Boolean = true,
) {
    val observer = LocalScrollChromeObserver.current
    if (!enabled || observer == null) return

    val currentHasPendingItems by rememberUpdatedState(hasPendingItems)
    val currentIsActive by rememberUpdatedState(LocalScrollChromeActive.current)

    val entry = remember(observer, state) {
        ScrollChromeBoundaryEntry(
            isActive = { currentIsActive() },
            isAtBoundary = { isScrollableAtBoundary(state, currentHasPendingItems()) },
        )
    }

    DisposableEffect(observer, state) {
        val unregister = observer.register(entry)
        onDispose {
            unregister()
            observer.notifyBoundaryChanged()
        }
    }

    LaunchedEffect(observer, state) {
        snapshotFlow { entry.isActive() to entry.isAtBoundary() }
            .distinctUntilChanged()
            .collect { (active, atBoundary) ->
                if (active && atBoundary) {
                    observer.notifyBoundaryChanged()
                }
            }
    }
}
