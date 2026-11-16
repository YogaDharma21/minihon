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

import androidx.compose.animation.core.LinearEasing

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

@Composable
fun rememberMangaDetailAssemblyState(
    key: Any? = Unit,
): MangaDetailAssemblyState {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current
    val isReducedMotion = remember(isInspection) {
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

    val masterTime = remember(key) { Animatable(0f) }
    val fabScale = remember(key) { Animatable(0f) }

    LaunchedEffect(key) {
        launch {
            masterTime.animateTo(
                targetValue = DETAIL_ASSEMBLY_DURATION.toFloat(),
                animationSpec = tween(
                    durationMillis = DETAIL_ASSEMBLY_DURATION,
                    easing = LinearEasing,
                ),
            )
        }
        launch {
            delay(FAB_DELAY.toLong())
            fabScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
    }

    return remember(key) {
        MangaDetailAssemblyState(
            topBarModifier = Modifier.graphicsLayer {
                val p = calculateAssemblyProgress(masterTime.value, TOP_BAR_DELAY, TOP_BAR_DURATION)
                if (p < 1f) {
                    alpha = p
                    translationY = TOP_BAR_START_TRANSLATION_Y.toPx() * (1f - p)
                } else {
                    alpha = 1f
                    translationY = 0f
                }
            },
            coverModifier = Modifier.graphicsLayer {
                val p = calculateAssemblyProgress(masterTime.value, COVER_DELAY, COVER_DURATION)
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
                val p = calculateAssemblyProgress(masterTime.value, TITLES_DELAY, TITLES_DURATION)
                if (p < 1f) {
                    alpha = p
                    translationX = TITLES_START_TRANSLATION_X.toPx() * (1f - p)
                } else {
                    alpha = 1f
                    translationX = 0f
                }
            },
            actionButtonsModifier = Modifier.graphicsLayer {
                val p = calculateAssemblyProgress(masterTime.value, ACTION_BUTTONS_DELAY, ACTION_BUTTONS_DURATION)
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
                val p = calculateAssemblyProgress(masterTime.value, DESCRIPTION_DELAY, DESCRIPTION_DURATION)
                if (p < 1f) {
                    alpha = p
                    translationY = DESCRIPTION_START_TRANSLATION_Y.toPx() * (1f - p)
                } else {
                    alpha = 1f
                    translationY = 0f
                }
            },
            chaptersModifier = Modifier.graphicsLayer {
                val p = calculateAssemblyProgress(masterTime.value, CHAPTERS_DELAY, CHAPTERS_DURATION)
                if (p < 1f) {
                    alpha = p
                    translationY = CHAPTERS_START_TRANSLATION_Y.toPx() * (1f - p)
                } else {
                    alpha = 1f
                    translationY = 0f
                }
            },
            fabModifier = Modifier.graphicsLayer {
                val a = calculateAssemblyProgress(masterTime.value, FAB_DELAY, FAB_ALPHA_DURATION)
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
