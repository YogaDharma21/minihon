package tachiyomi.presentation.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

val LocalIsFastScrolling = compositionLocalOf { false }

class ScrollVelocityGate(
    private val scope: CoroutineScope,
    private val thresholdPxPerSec: Float = 2200f,
) : NestedScrollConnection {

    var isFastScrolling by mutableStateOf(false)
        private set

    private var resetJob: Job? = null
    private var lastScrollTime = 0L

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (source == NestedScrollSource.UserInput) {
            val now = System.currentTimeMillis()
            val dt = now - lastScrollTime
            if (dt in 1..100) {
                val speed = abs(available.y) / (dt / 1000f)
                if (speed >= thresholdPxPerSec) {
                    if (!isFastScrolling) isFastScrolling = true
                    resetJob?.cancel()
                    resetJob = scope.launch {
                        delay(250)
                        isFastScrolling = false
                    }
                }
            }
            lastScrollTime = now
        }
        return Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (abs(available.y) >= thresholdPxPerSec) {
            isFastScrolling = true
            resetJob?.cancel()
            resetJob = scope.launch {
                delay(300)
                isFastScrolling = false
            }
        }
        return Velocity.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        resetJob?.cancel()
        resetJob = scope.launch {
            delay(150)
            isFastScrolling = false
        }
        return Velocity.Zero
    }
}

@Composable
fun ProvideScrollVelocityGate(
    thresholdPxPerSec: Float = 2200f,
    content: @Composable (Modifier) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val gate = remember(scope) { ScrollVelocityGate(scope, thresholdPxPerSec) }

    CompositionLocalProvider(LocalIsFastScrolling provides gate.isFastScrolling) {
        content(Modifier.nestedScroll(gate))
    }
}
