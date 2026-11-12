package eu.kanade.presentation.util

import androidx.compose.ui.unit.dp
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CascadeMotionTest {

    @Test
    fun `cascade constants match specification`() {
        CASCADE_DURATION_MILLIS shouldBe 800
        CASCADE_STAGGER_MILLIS shouldBe 60
        CASCADE_RISE_DP shouldBe 12.dp
    }

    @Test
    fun `target delay increases by stagger per index`() {
        calculateCascadeTiming(
            index = 0,
            startUptimeMillis = 1000L,
            currentUptimeMillis = 1000L,
        ).targetDelayMillis shouldBe
            0L
        calculateCascadeTiming(
            index = 1,
            startUptimeMillis = 1000L,
            currentUptimeMillis = 1000L,
        ).targetDelayMillis shouldBe
            60L
        calculateCascadeTiming(
            index = 2,
            startUptimeMillis = 1000L,
            currentUptimeMillis = 1000L,
        ).targetDelayMillis shouldBe
            120L
        calculateCascadeTiming(
            index = 5,
            startUptimeMillis = 1000L,
            currentUptimeMillis = 1000L,
        ).targetDelayMillis shouldBe
            300L
        calculateCascadeTiming(
            index = -1,
            startUptimeMillis = 1000L,
            currentUptimeMillis = 1000L,
        ).targetDelayMillis shouldBe
            0L
    }

    @Test
    fun `remaining delay decreases as elapsed time increases until zero`() {
        val start = 1000L
        val index = 2 // target delay = 120ms

        val t0 = calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start)
        t0.remainingDelayMillis shouldBe 120L

        val t50 = calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start + 50L)
        t50.remainingDelayMillis shouldBe 70L

        val t120 = calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start + 120L)
        t120.remainingDelayMillis shouldBe 0L

        val t200 = calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start + 200L)
        t200.remainingDelayMillis shouldBe 0L
    }

    @Test
    fun `bypass threshold marks completed when elapsed exceeds total duration`() {
        val start = 1000L
        val index = 2 // target delay = 120ms, duration = 800ms, total = 920ms

        val justBefore = calculateCascadeTiming(
            index = index,
            startUptimeMillis = start,
            currentUptimeMillis =
            start + 919L,
        )
        justBefore.isCompleted shouldBe false

        val exact = calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start + 920L)
        exact.isCompleted shouldBe true

        val after =
            calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start + 1500L)
        after.isCompleted shouldBe true
    }

    @Test
    fun `remaining duration is full duration before start and decays during animation`() {
        val start = 1000L
        val index = 2 // target delay = 120ms, duration = 800ms

        // Before start
        val beforeStart = calculateCascadeTiming(
            index = index,
            startUptimeMillis = start,
            currentUptimeMillis =
            start + 50L,
        )
        beforeStart.remainingDurationMillis shouldBe 800L
        beforeStart.initialFraction shouldBe 0f

        // Exact start of item animation
        val atStart = calculateCascadeTiming(
            index = index,
            startUptimeMillis = start,
            currentUptimeMillis =
            start + 120L,
        )
        atStart.remainingDurationMillis shouldBe 800L
        atStart.initialFraction shouldBe 0f

        // Halfway through animation (120ms delay + 400ms = 520ms)
        val halfway = calculateCascadeTiming(
            index = index,
            startUptimeMillis = start,
            currentUptimeMillis =
            start + 520L,
        )
        halfway.remainingDurationMillis shouldBe 400L
        halfway.initialFraction shouldBe (0.5f plusOrMinus 0.001f)

        // At end
        val atEnd = calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start + 920L)
        atEnd.remainingDurationMillis shouldBe 0L
        atEnd.initialFraction shouldBe 1f

        // Past end
        val pastEnd = calculateCascadeTiming(
            index = index,
            startUptimeMillis = start,
            currentUptimeMillis =
            start + 2000L,
        )
        pastEnd.remainingDurationMillis shouldBe 0L
        pastEnd.initialFraction shouldBe 1f
    }

    @Test
    fun `cascade trigger defaults to id 0`() {
        val trigger = CascadeMotionTrigger()
        trigger.id shouldBe 0L
    }
}
