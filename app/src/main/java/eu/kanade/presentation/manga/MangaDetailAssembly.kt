package eu.kanade.presentation.manga

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val DETAIL_ASSEMBLY_EASING: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
const val DETAIL_ASSEMBLY_DURATION: Int = 800

val TOP_BAR_START_TRANSLATION_Y: Dp = (-24).dp
val COVER_START_TRANSLATION_X: Dp = (-40).dp
const val COVER_START_SCALE: Float = 0.92f
val TITLES_START_TRANSLATION_X: Dp = 36.dp
val ACTION_BUTTONS_START_TRANSLATION_Y: Dp = 20.dp
const val ACTION_BUTTONS_START_SCALE: Float = 0.95f
val DESCRIPTION_START_TRANSLATION_Y: Dp = 16.dp
val CHAPTERS_START_TRANSLATION_Y: Dp = 28.dp

const val TOP_BAR_DELAY: Int = 0
const val TOP_BAR_DURATION: Int = 450

const val COVER_DELAY: Int = 60
const val COVER_DURATION: Int = 550

const val TITLES_DELAY: Int = 120
const val TITLES_DURATION: Int = 550

const val ACTION_BUTTONS_DELAY: Int = 180
const val ACTION_BUTTONS_DURATION: Int = 500

const val DESCRIPTION_DELAY: Int = 240
const val DESCRIPTION_DURATION: Int = 500

const val CHAPTERS_DELAY: Int = 300
const val CHAPTERS_DURATION: Int = 500

const val FAB_DELAY: Int = 350
const val FAB_ALPHA_DURATION: Int = 350

@Stable
class MangaDetailAssemblyState internal constructor(
    val topBarModifier: Modifier,
    val coverModifier: Modifier,
    val titlesModifier: Modifier,
    val actionButtonsModifier: Modifier,
    val descriptionModifier: Modifier,
    val chaptersModifier: Modifier,
    val fabModifier: Modifier,
)

internal fun calculateAssemblyProgress(
    elapsedMs: Float,
    delayMs: Int,
    durationMs: Int,
    easing: Easing = DETAIL_ASSEMBLY_EASING,
): Float {
    if (elapsedMs <= delayMs) return 0f
    if (elapsedMs >= delayMs + durationMs) return 1f
    val fraction = (elapsedMs - delayMs) / durationMs.toFloat()
    return easing.transform(fraction)
}

private val StaticMangaDetailAssemblyState = MangaDetailAssemblyState(
    topBarModifier = Modifier,
    coverModifier = Modifier,
    titlesModifier = Modifier,
    actionButtonsModifier = Modifier,
    descriptionModifier = Modifier,
    chaptersModifier = Modifier,
    fabModifier = Modifier,
)

@Composable
fun rememberMangaDetailAssemblyState(
    key: Any? = Unit,
): MangaDetailAssemblyState = StaticMangaDetailAssemblyState
