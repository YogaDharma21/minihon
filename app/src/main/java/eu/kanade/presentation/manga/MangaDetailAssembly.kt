package eu.kanade.presentation.manga

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
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

val DETAIL_ASSEMBLY_EASING: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
const val DETAIL_ASSEMBLY_DURATION: Int = 700

val TOP_BAR_START_TRANSLATION_Y: Dp = (-36).dp
val COVER_START_TRANSLATION_X: Dp = (-40).dp
const val COVER_START_SCALE: Float = 0.92f
val TITLES_START_TRANSLATION_X: Dp = 36.dp
val ACTION_BUTTONS_START_TRANSLATION_Y: Dp = 24.dp
const val ACTION_BUTTONS_START_SCALE: Float = 0.95f
val DESCRIPTION_START_TRANSLATION_Y: Dp = 20.dp
val CHAPTERS_START_TRANSLATION_Y: Dp = 28.dp

const val TOP_BAR_DELAY: Int = 0
const val TOP_BAR_DURATION: Int = 450

const val COVER_DELAY: Int = 50
const val COVER_DURATION: Int = 550

const val TITLES_DELAY: Int = 100
const val TITLES_DURATION: Int = 550

const val ACTION_BUTTONS_DELAY: Int = 150
const val ACTION_BUTTONS_DURATION: Int = 500

const val DESCRIPTION_DELAY: Int = 200
const val DESCRIPTION_DURATION: Int = 500

const val CHAPTERS_DELAY: Int = 250
const val CHAPTERS_DURATION: Int = 450

const val FAB_DELAY: Int = 280
const val FAB_ALPHA_DURATION: Int = 420

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
        return StaticMangaDetailAssemblyState
    }

    val animProgress = remember(key) { Animatable(0f) }

    LaunchedEffect(key) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = DETAIL_ASSEMBLY_DURATION.toFloat(),
            animationSpec = tween(
                durationMillis = DETAIL_ASSEMBLY_DURATION,
                easing = LinearEasing,
            ),
        )
    }

    return remember(key) {
        MangaDetailAssemblyState(
            topBarModifier = Modifier.graphicsLayer {
                val progress = calculateAssemblyProgress(animProgress.value, TOP_BAR_DELAY, TOP_BAR_DURATION)
                alpha = progress
                translationY = TOP_BAR_START_TRANSLATION_Y.toPx() * (1f - progress)
            },
            coverModifier = Modifier.graphicsLayer {
                val progress = calculateAssemblyProgress(animProgress.value, COVER_DELAY, COVER_DURATION)
                alpha = progress
                translationX = COVER_START_TRANSLATION_X.toPx() * (1f - progress)
                val scale = COVER_START_SCALE + (1f - COVER_START_SCALE) * progress
                scaleX = scale
                scaleY = scale
            },
            titlesModifier = Modifier.graphicsLayer {
                val progress = calculateAssemblyProgress(animProgress.value, TITLES_DELAY, TITLES_DURATION)
                alpha = progress
                translationX = TITLES_START_TRANSLATION_X.toPx() * (1f - progress)
            },
            actionButtonsModifier = Modifier.graphicsLayer {
                val progress = calculateAssemblyProgress(animProgress.value, ACTION_BUTTONS_DELAY, ACTION_BUTTONS_DURATION)
                alpha = progress
                translationY = ACTION_BUTTONS_START_TRANSLATION_Y.toPx() * (1f - progress)
                val scale = ACTION_BUTTONS_START_SCALE + (1f - ACTION_BUTTONS_START_SCALE) * progress
                scaleX = scale
                scaleY = scale
            },
            descriptionModifier = Modifier.graphicsLayer {
                val progress = calculateAssemblyProgress(animProgress.value, DESCRIPTION_DELAY, DESCRIPTION_DURATION)
                alpha = progress
                translationY = DESCRIPTION_START_TRANSLATION_Y.toPx() * (1f - progress)
            },
            chaptersModifier = Modifier.graphicsLayer {
                val progress = calculateAssemblyProgress(animProgress.value, CHAPTERS_DELAY, CHAPTERS_DURATION)
                alpha = progress
                translationY = CHAPTERS_START_TRANSLATION_Y.toPx() * (1f - progress)
            },
            fabModifier = Modifier.graphicsLayer {
                val progress = calculateAssemblyProgress(animProgress.value, FAB_DELAY, FAB_ALPHA_DURATION)
                alpha = progress
                val scale = 0.88f + 0.12f * progress
                scaleX = scale
                scaleY = scale
            },
        )
    }
}
