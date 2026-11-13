package eu.kanade.presentation.manga

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
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

@Composable
fun rememberMangaDetailAssemblyState(
    key: Any? = Unit,
): MangaDetailAssemblyState {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current
    val isReducedMotion = remember(context, isInspection) {
        if (isInspection) return@remember true
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f,
            ) == 0f
        } catch (_: Throwable) {
            false
        }
    }

    if (isReducedMotion) {
        return remember {
            MangaDetailAssemblyState(
                topBarModifier = Modifier,
                coverModifier = Modifier,
                titlesModifier = Modifier,
                actionButtonsModifier = Modifier,
                descriptionModifier = Modifier,
                chaptersModifier = Modifier,
                fabModifier = Modifier,
            )
        }
    }

    val topBarProgress = remember(key) { Animatable(0f) }
    val coverProgress = remember(key) { Animatable(0f) }
    val titlesProgress = remember(key) { Animatable(0f) }
    val actionButtonsProgress = remember(key) { Animatable(0f) }
    val descriptionProgress = remember(key) { Animatable(0f) }
    val chaptersProgress = remember(key) { Animatable(0f) }
    val fabAlpha = remember(key) { Animatable(0f) }
    val fabScale = remember(key) { Animatable(0f) }

    LaunchedEffect(key) {
        launch {
            topBarProgress.animateTo(
                1f,
                tween(durationMillis = TOP_BAR_DURATION, delayMillis = TOP_BAR_DELAY, easing = DETAIL_ASSEMBLY_EASING),
            )
        }
        launch {
            coverProgress.animateTo(
                1f,
                tween(durationMillis = COVER_DURATION, delayMillis = COVER_DELAY, easing = DETAIL_ASSEMBLY_EASING),
            )
        }
        launch {
            titlesProgress.animateTo(
                1f,
                tween(durationMillis = TITLES_DURATION, delayMillis = TITLES_DELAY, easing = DETAIL_ASSEMBLY_EASING),
            )
        }
        launch {
            actionButtonsProgress.animateTo(
                1f,
                tween(durationMillis = ACTION_BUTTONS_DURATION, delayMillis = ACTION_BUTTONS_DELAY, easing = DETAIL_ASSEMBLY_EASING),
            )
        }
        launch {
            descriptionProgress.animateTo(
                1f,
                tween(durationMillis = DESCRIPTION_DURATION, delayMillis = DESCRIPTION_DELAY, easing = DETAIL_ASSEMBLY_EASING),
            )
        }
        launch {
            chaptersProgress.animateTo(
                1f,
                tween(durationMillis = CHAPTERS_DURATION, delayMillis = CHAPTERS_DELAY, easing = DETAIL_ASSEMBLY_EASING),
            )
        }
        launch {
            fabAlpha.animateTo(
                1f,
                tween(durationMillis = FAB_ALPHA_DURATION, delayMillis = FAB_DELAY, easing = DETAIL_ASSEMBLY_EASING),
            )
        }
        launch {
            delay(FAB_DELAY.toLong())
            fabScale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
    }

    return remember(key) {
        MangaDetailAssemblyState(
            topBarModifier = Modifier.graphicsLayer {
                val p = topBarProgress.value
                if (p < 1f) {
                    alpha = p
                    translationY = TOP_BAR_START_TRANSLATION_Y.toPx() * (1f - p)
                } else {
                    alpha = 1f
                    translationY = 0f
                }
            },
            coverModifier = Modifier.graphicsLayer {
                val p = coverProgress.value
                if (p < 1f) {
                    alpha = p
                    translationX = COVER_START_TRANSLATION_X.toPx() * (1f - p)
                    val s = COVER_START_SCALE + (1f - COVER_START_SCALE) * p
                    scaleX = s
                    scaleY = s
                } else {
                    alpha = 1f
                    translationX = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
            },
            titlesModifier = Modifier.graphicsLayer {
                val p = titlesProgress.value
                if (p < 1f) {
                    alpha = p
                    translationX = TITLES_START_TRANSLATION_X.toPx() * (1f - p)
                } else {
                    alpha = 1f
                    translationX = 0f
                }
            },
            actionButtonsModifier = Modifier.graphicsLayer {
                val p = actionButtonsProgress.value
                if (p < 1f) {
                    alpha = p
                    translationY = ACTION_BUTTONS_START_TRANSLATION_Y.toPx() * (1f - p)
                    val s = ACTION_BUTTONS_START_SCALE + (1f - ACTION_BUTTONS_START_SCALE) * p
                    scaleX = s
                    scaleY = s
                } else {
                    alpha = 1f
                    translationY = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
            },
            descriptionModifier = Modifier.graphicsLayer {
                val p = descriptionProgress.value
                if (p < 1f) {
                    alpha = p
                    translationY = DESCRIPTION_START_TRANSLATION_Y.toPx() * (1f - p)
                } else {
                    alpha = 1f
                    translationY = 0f
                }
            },
            chaptersModifier = Modifier.graphicsLayer {
                val p = chaptersProgress.value
                if (p < 1f) {
                    alpha = p
                    translationY = CHAPTERS_START_TRANSLATION_Y.toPx() * (1f - p)
                } else {
                    alpha = 1f
                    translationY = 0f
                }
            },
            fabModifier = Modifier.graphicsLayer {
                val a = fabAlpha.value
                val s = fabScale.value
                if (a < 1f || s != 1f) {
                    alpha = a.coerceIn(0f, 1f)
                    scaleX = s
                    scaleY = s
                } else {
                    alpha = 1f
                    scaleX = 1f
                    scaleY = 1f
                }
            },
        )
    }
}
