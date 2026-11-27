package eu.kanade.presentation.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.dp
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CascadeMotionTest {

    @Test
    fun `cascade constants match specification`() {
        CASCADE_DURATION_MILLIS shouldBe 700
        CASCADE_STAGGER_MILLIS shouldBe 70
        CASCADE_INITIAL_WAVE_COUNT shouldBe 12
        CASCADE_RISE_DP shouldBe 40.dp
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
            70L
        calculateCascadeTiming(
            index = 2,
            startUptimeMillis = 1000L,
            currentUptimeMillis = 1000L,
        ).targetDelayMillis shouldBe
            140L
        calculateCascadeTiming(
            index = 5,
            startUptimeMillis = 1000L,
            currentUptimeMillis = 1000L,
        ).targetDelayMillis shouldBe
            350L
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
        val index = 2 // target delay = 140ms

        val t0 = calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start)
        t0.remainingDelayMillis shouldBe 140L

        val t50 = calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start + 50L)
        t50.remainingDelayMillis shouldBe 90L

        val t140 = calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start + 140L)
        t140.remainingDelayMillis shouldBe 0L

        val t200 = calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start + 200L)
        t200.remainingDelayMillis shouldBe 0L
    }

    @Test
    fun `bypass threshold marks completed when elapsed exceeds total duration`() {
        val start = 1000L
        val index = 2 // target delay = 140ms, duration = 700ms, total = 840ms

        val justBefore = calculateCascadeTiming(
            index = index,
            startUptimeMillis = start,
            currentUptimeMillis =
            start + 839L,
        )
        justBefore.isCompleted shouldBe false

        val exact = calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start + 840L)
        exact.isCompleted shouldBe true

        val after =
            calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start + 1500L)
        after.isCompleted shouldBe true
    }

    @Test
    fun `remaining duration is full duration before start and decays during animation`() {
        val start = 1000L
        val index = 2 // target delay = 140ms, duration = 700ms

        // Before start
        val beforeStart = calculateCascadeTiming(
            index = index,
            startUptimeMillis = start,
            currentUptimeMillis =
            start + 50L,
        )
        beforeStart.remainingDurationMillis shouldBe 700L
        beforeStart.initialFraction shouldBe 0f

        // Exact start of item animation
        val atStart = calculateCascadeTiming(
            index = index,
            startUptimeMillis = start,
            currentUptimeMillis =
            start + 140L,
        )
        atStart.remainingDurationMillis shouldBe 700L
        atStart.initialFraction shouldBe 0f

        // Halfway through animation (140ms delay + 350ms = 490ms)
        val halfway = calculateCascadeTiming(
            index = index,
            startUptimeMillis = start,
            currentUptimeMillis =
            start + 490L,
        )
        halfway.remainingDurationMillis shouldBe 350L
        halfway.initialFraction shouldBe (0.5f plusOrMinus 0.001f)

        // At end
        val atEnd = calculateCascadeTiming(index = index, startUptimeMillis = start, currentUptimeMillis = start + 840L)
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
        trigger.disabled shouldBe false
    }

    @Test
    fun `cascade trigger disabled companion object has disabled set to true`() {
        CascadeMotionTrigger.Disabled.disabled shouldBe true
    }

    @Test
    fun `cascade motion session tracks first entrance and marks subsequent accesses as played`() {
        CascadeMotionSession.resetForTesting()

        CascadeMotionSession.hasPlayed(CascadeSurface.Library) shouldBe false
        CascadeMotionSession.hasPlayed(CascadeSurface.Updates) shouldBe false
        CascadeMotionSession.hasPlayed(CascadeSurface.History) shouldBe false
        CascadeMotionSession.hasPlayed(CascadeSurface.Sources) shouldBe false
        CascadeMotionSession.hasPlayed(CascadeSurface.Extensions) shouldBe false

        CascadeMotionSession.markPlayed(CascadeSurface.Library)
        CascadeMotionSession.hasPlayed(CascadeSurface.Library) shouldBe true
        CascadeMotionSession.hasPlayed(CascadeSurface.Updates) shouldBe false

        CascadeMotionSession.markPlayed(CascadeSurface.Updates)
        CascadeMotionSession.hasPlayed(CascadeSurface.Updates) shouldBe true

        CascadeMotionSession.resetForTesting()
        CascadeMotionSession.hasPlayed(CascadeSurface.Library) shouldBe false
        CascadeMotionSession.hasPlayed(CascadeSurface.Updates) shouldBe false
    }

    @Test
    fun `trigger resolution returns disabled on subsequent accesses`() {
        CascadeMotionSession.resetForTesting()

        fun resolveTrigger(surface: CascadeSurface): CascadeMotionTrigger {
            return if (CascadeMotionSession.hasPlayed(surface)) {
                CascadeMotionTrigger.Disabled
            } else {
                CascadeMotionSession.markPlayed(surface)
                CascadeMotionTrigger()
            }
        }

        val firstAccess = resolveTrigger(CascadeSurface.Library)
        firstAccess.disabled shouldBe false

        val secondAccess = resolveTrigger(CascadeSurface.Library)
        secondAccess.disabled shouldBe true

        val otherSurfaceFirst = resolveTrigger(CascadeSurface.Sources)
        otherSurfaceFirst.disabled shouldBe false

        CascadeMotionSession.resetForTesting()
        val afterReset = resolveTrigger(CascadeSurface.Library)
        afterReset.disabled shouldBe false
    }

    @Test
    fun `resolveCascadeReveal uses linear initial wave stagger for indices 0 through 11`() {
        val start = 1000L

        for (index in 0 until CASCADE_INITIAL_WAVE_COUNT) {
            val plan = resolveCascadeReveal(
                index = index,
                startUptimeMillis = start,
                currentUptimeMillis = start,
            )
            plan.isInitialCascade shouldBe true
            plan.initialFraction shouldBe 0f
            plan.remainingDelayMillis shouldBe index * 70L
            plan.remainingDurationMillis shouldBe 700
        }

        val midWave = resolveCascadeReveal(
            index = 2,
            startUptimeMillis = start,
            currentUptimeMillis = start + 490L,
        )
        midWave.isInitialCascade shouldBe true
        midWave.initialFraction shouldBe (0.5f plusOrMinus 0.001f)
        midWave.remainingDelayMillis shouldBe 0L
        midWave.remainingDurationMillis shouldBe 350
    }

    @Test
    fun `resolveCascadeReveal returns zero delay non-initial reveal for index 12 and above during initial wave`() {
        val start = 1000L

        listOf(-1, 12, 13, 24).forEach { index ->
            val plan = resolveCascadeReveal(
                index = index,
                startUptimeMillis = start,
                currentUptimeMillis = start,
            )
            plan.isInitialCascade shouldBe false
            plan.initialFraction shouldBe 0f
            plan.remainingDelayMillis shouldBe 0L
            plan.remainingDurationMillis shouldBe 700
        }
    }

    @Test
    fun `resolveCascadeReveal returns zero delay across columns after initial wave completes`() {
        val start = 1000L
        val afterWave = start + 2000L

        listOf(0, 1, 2, 12, 13, 14).forEach { index ->
            val plan = resolveCascadeReveal(
                index = index,
                startUptimeMillis = start,
                currentUptimeMillis = afterWave,
            )
            plan.isInitialCascade shouldBe false
            plan.initialFraction shouldBe 0f
            plan.remainingDelayMillis shouldBe 0L
            plan.remainingDurationMillis shouldBe 700
        }
    }

    @Test
    fun `scroll connection switches direction past threshold and ignores jitter`() {
        val directions = mutableListOf<CascadeDirection>()
        val connection = CascadeScrollConnection { directions += it }

        connection.onPreScroll(Offset(0f, -8f), NestedScrollSource.UserInput) shouldBe Offset.Zero
        connection.onPreScroll(Offset(0f, 0f), NestedScrollSource.UserInput) shouldBe Offset.Zero
        connection.onPreScroll(Offset(0f, 8f), NestedScrollSource.UserInput) shouldBe Offset.Zero
        directions shouldBe emptyList()

        connection.onPreScroll(Offset(0f, -8.1f), NestedScrollSource.UserInput) shouldBe Offset.Zero
        connection.onPreScroll(Offset(0f, 5f), NestedScrollSource.UserInput) shouldBe Offset.Zero
        directions shouldBe listOf(CascadeDirection.FromBottom)

        connection.onPreScroll(Offset(0f, 8.1f), NestedScrollSource.UserInput) shouldBe Offset.Zero
        directions shouldBe listOf(CascadeDirection.FromBottom, CascadeDirection.FromTop)
    }

    @Test
    fun `scroll direction state starts from bottom and tracks bi-directional scroll`() {
        val state = CascadeScrollDirectionState()
        state.direction shouldBe CascadeDirection.FromBottom

        state.connection.onPreScroll(Offset(0f, 5f), NestedScrollSource.UserInput)
        state.direction shouldBe CascadeDirection.FromBottom

        state.connection.onPreScroll(Offset(0f, 12f), NestedScrollSource.UserInput)
        state.direction shouldBe CascadeDirection.FromTop

        state.connection.onPreScroll(Offset(0f, -4f), NestedScrollSource.UserInput)
        state.direction shouldBe CascadeDirection.FromTop

        state.connection.onPreScroll(Offset(0f, -12f), NestedScrollSource.UserInput)
        state.direction shouldBe CascadeDirection.FromBottom
    }
}
