package eu.kanade.presentation.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class MotionTest {

    @Test
    fun `springLowStiffness configures medium low stiffness and no bouncy damping`() {
        val spec = springLowStiffness<Float>()
        spec.stiffness shouldBe Spring.StiffnessMediumLow
        spec.dampingRatio shouldBe Spring.DampingRatioNoBouncy
    }

    @Test
    fun `reusable item animation specs match design motion parameters`() {
        val fadeIn = itemFadeInSpec as SpringSpec<Float>
        fadeIn.stiffness shouldBe Spring.StiffnessMediumLow
        fadeIn.dampingRatio shouldBe Spring.DampingRatioNoBouncy

        val fadeOut = itemFadeOutSpec as SpringSpec<Float>
        fadeOut.stiffness shouldBe Spring.StiffnessMediumLow
        fadeOut.dampingRatio shouldBe Spring.DampingRatioNoBouncy

        val placement = itemPlacementSpec as SpringSpec<IntOffset>
        placement.stiffness shouldBe Spring.StiffnessMediumLow
        placement.dampingRatio shouldBe Spring.DampingRatioNoBouncy
    }

    @Test
    fun `ScrollChromeConnection hides chrome when scrolling down past threshold`() {
        var visibleState = true
        val connection = ScrollChromeConnection(
            onVisibilityChanged = { visibleState = it },
            thresholdPx = 48f,
        )

        connection.onPreScroll(
            available = Offset(0f, -50f),
            source = NestedScrollSource.UserInput,
        )

        visibleState shouldBe false
    }

    @Test
    fun `ScrollChromeConnection reveals chrome when scrolling up past threshold`() {
        var visibleState = false
        val connection = ScrollChromeConnection(
            onVisibilityChanged = { visibleState = it },
            thresholdPx = 48f,
        )

        connection.onPreScroll(
            available = Offset(0f, 50f),
            source = NestedScrollSource.UserInput,
        )

        visibleState shouldBe true
    }

    @Test
    fun `jitter filtering ensures deltas smaller than threshold do not toggle visibility`() {
        var callCount = 0
        var visibleState = true
        val connection = ScrollChromeConnection(
            onVisibilityChanged = {
                visibleState = it
                callCount++
            },
            thresholdPx = 48f,
        )

        connection.onPreScroll(
            available = Offset(0f, -20f),
            source = NestedScrollSource.UserInput,
        )
        callCount shouldBe 0
        visibleState shouldBe true

        connection.onPreScroll(
            available = Offset(0f, 15f),
            source = NestedScrollSource.UserInput,
        )
        callCount shouldBe 0
        visibleState shouldBe true
    }

    @Test
    fun `accumulated small deltas trigger visibility change when threshold is reached`() {
        var visibleState = true
        val connection = ScrollChromeConnection(
            onVisibilityChanged = { visibleState = it },
            thresholdPx = 48f,
        )

        connection.onPreScroll(Offset(0f, -25f), NestedScrollSource.UserInput)
        visibleState shouldBe true

        connection.onPreScroll(Offset(0f, -25f), NestedScrollSource.UserInput)
        visibleState shouldBe false
    }

    @Test
    fun `HorizontalSwipeConnection triggers onSwipeLeft when scrolling left past threshold`() {
        var leftTriggered = false
        var rightTriggered = false
        val connection = HorizontalSwipeConnection(
            onSwipeLeft = { leftTriggered = true },
            onSwipeRight = { rightTriggered = true },
            thresholdPx = 120f,
            timeProvider = { 1000L },
        )

        connection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(-130f, 0f),
            source = NestedScrollSource.UserInput,
        )

        leftTriggered shouldBe true
        rightTriggered shouldBe false
    }

    @Test
    fun `HorizontalSwipeConnection triggers onSwipeRight when scrolling right past threshold`() {
        var leftTriggered = false
        var rightTriggered = false
        val connection = HorizontalSwipeConnection(
            onSwipeLeft = { leftTriggered = true },
            onSwipeRight = { rightTriggered = true },
            thresholdPx = 120f,
            timeProvider = { 1000L },
        )

        connection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(130f, 0f),
            source = NestedScrollSource.UserInput,
        )

        leftTriggered shouldBe false
        rightTriggered shouldBe true
    }

    @Test
    fun `HorizontalSwipeConnection triggers swipe on fling when velocity exceeds threshold`() = runTest {
        var leftTriggered = false
        var rightTriggered = false
        var currentTime = 1000L
        val connection = HorizontalSwipeConnection(
            onSwipeLeft = { leftTriggered = true },
            onSwipeRight = { rightTriggered = true },
            velocityThreshold = 800f,
            timeProvider = { currentTime },
        )

        connection.onPostFling(
            consumed = Velocity.Zero,
            available = Velocity(-1000f, 100f),
        )

        leftTriggered shouldBe true
        rightTriggered shouldBe false

        currentTime = 1500L
        connection.onPostFling(
            consumed = Velocity.Zero,
            available = Velocity(1000f, 100f),
        )

        rightTriggered shouldBe true
    }

    @Test
    fun `HorizontalSwipeConnection triggers swipe on moderate flick with default thresholds`() = runTest {
        var leftTriggered = false
        var rightTriggered = false
        val connection = HorizontalSwipeConnection(
            onSwipeLeft = { leftTriggered = true },
            onSwipeRight = { rightTriggered = true },
        )

        connection.onPostFling(
            consumed = Velocity.Zero,
            available = Velocity(-500f, 0f),
        )

        leftTriggered shouldBe true
        rightTriggered shouldBe false
    }

    @Test
    fun `HorizontalSwipeConnection debounces triggers within cooldown window`() {
        var leftCount = 0
        var currentTime = 1000L
        val connection = HorizontalSwipeConnection(
            onSwipeLeft = { leftCount++ },
            onSwipeRight = {},
            thresholdPx = 100f,
            timeProvider = { currentTime },
        )

        connection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(-120f, 0f),
            source = NestedScrollSource.UserInput,
        )
        leftCount shouldBe 1

        // Within cooldown window of 350ms
        currentTime = 1200L
        connection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(-120f, 0f),
            source = NestedScrollSource.UserInput,
        )
        leftCount shouldBe 1

        // After cooldown window
        currentTime = 1500L
        connection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(-120f, 0f),
            source = NestedScrollSource.UserInput,
        )
        leftCount shouldBe 2
    }
}
