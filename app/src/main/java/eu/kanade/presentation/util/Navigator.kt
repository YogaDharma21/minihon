package eu.kanade.presentation.util

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransitionContent
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance

/**
 * For invoking back press to the parent activity
 */
val LocalBackPress: ProvidableCompositionLocal<(() -> Unit)?> = staticCompositionLocalOf { null }

interface Tab : cafe.adriel.voyager.navigator.tab.Tab {
    suspend fun onReselect(navigator: Navigator) {}
}

abstract class Screen : Screen {

    override val key: ScreenKey = uniqueScreenKey
}

interface AssistContentScreen {
    fun onProvideAssistUrl(): String?
}

@Composable
fun DefaultNavigatorScreenTransition(
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val slideDistance = rememberSlideDistance()
    ScreenTransition(
        navigator = navigator,
        transition = {
            val isPushingManga = targetState is MangaScreen && navigator.lastEvent != StackEvent.Pop
            val isPoppingManga = initialState is MangaScreen && navigator.lastEvent == StackEvent.Pop

            when {
                isPushingManga -> {
                    // Variant C: the detail screen stages its own toolbar, info card, and
                    // chapter list entrance. Keep only a short background crossfade here so
                    // the origin does not cut and the staged motion is not scaled as one block.
                    val enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 260,
                            easing = LinearOutSlowInEasing,
                        ),
                    )
                    val exit = scaleOut(
                        targetScale = 0.98f,
                        animationSpec = tween(
                            durationMillis = 260,
                            easing = LinearOutSlowInEasing,
                        ),
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = 220,
                            easing = FastOutLinearInEasing,
                        ),
                    )
                    (enter togetherWith exit).apply {
                        targetContentZIndex = 1f
                    }
                }
                isPoppingManga -> {
                    // Variant B: Shared Axis Z Bloom Exit (0.5x slow-mo ~650ms)
                    val enter = scaleIn(
                        initialScale = 0.94f,
                        animationSpec = tween(
                            durationMillis = 650,
                            easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f),
                        ),
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = LinearOutSlowInEasing,
                        ),
                    )
                    val exit = scaleOut(
                        targetScale = 0.90f,
                        animationSpec = tween(
                            durationMillis = 650,
                            easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f),
                        ),
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = 450,
                            easing = FastOutLinearInEasing,
                        ),
                    )
                    (enter togetherWith exit).apply {
                        targetContentZIndex = -1f
                    }
                }
                else -> {
                    materialSharedAxisX(
                        forward = navigator.lastEvent != StackEvent.Pop,
                        slideDistance = slideDistance,
                    )
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
fun ScreenTransition(
    navigator: Navigator,
    transition: AnimatedContentTransitionScope<Screen>.() -> ContentTransform,
    modifier: Modifier = Modifier,
    content: ScreenTransitionContent = { it.Content() },
) {
    AnimatedContent(
        targetState = navigator.lastItem,
        transitionSpec = transition,
        modifier = modifier,
        label = "transition",
    ) { screen ->
        navigator.saveableState("transition", screen) {
            content(screen)
        }
    }

    BackHandler(enabled = navigator.canPop, onBack = navigator::pop)
}
