package tachiyomi.presentation.core.components

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ScrollChromeBoundaryTest {

    @Test
    fun `isScrollableAtBoundary returns true at top of list`() {
        val state = FakeScrollableState(canScrollBackward = false, canScrollForward = true)

        isScrollableAtBoundary(state) shouldBe true
    }

    @Test
    fun `isScrollableAtBoundary returns true at bottom without pending items`() {
        val state = FakeScrollableState(canScrollBackward = true, canScrollForward = false)

        isScrollableAtBoundary(state, hasPendingItems = false) shouldBe true
    }

    @Test
    fun `isScrollableAtBoundary returns false at bottom while paging has pending items`() {
        val state = FakeScrollableState(canScrollBackward = true, canScrollForward = false)

        isScrollableAtBoundary(state, hasPendingItems = true) shouldBe false
    }

    @Test
    fun `isScrollableAtBoundary returns false mid-list`() {
        val state = FakeScrollableState(canScrollBackward = true, canScrollForward = true)

        isScrollableAtBoundary(state) shouldBe false
    }

    @Test
    fun `isScrollableAtBoundary returns true when list is non-scrollable`() {
        val state = FakeScrollableState(canScrollBackward = false, canScrollForward = false)

        isScrollableAtBoundary(state) shouldBe true
    }

    private class FakeScrollableState(
        override val canScrollBackward: Boolean,
        override val canScrollForward: Boolean,
    ) : ScrollableState {
        override val isScrollInProgress: Boolean = false

        override suspend fun scroll(
            scrollPriority: MutatePriority,
            block: suspend ScrollScope.() -> Unit,
        ) = Unit

        override fun dispatchRawDelta(delta: Float): Float = 0f
    }
}
