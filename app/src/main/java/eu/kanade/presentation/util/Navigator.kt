package eu.kanade.presentation.util

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
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
                    val enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 280,
                            easing = LinearOutSlowInEasing,
                        ),
                    ) + scaleIn(
                        initialScale = 0.98f,
                        animationSpec = tween(
                            durationMillis = 280,
                            easing = LinearOutSlowInEasing,
                        ),
                    )
                    val exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = FastOutLinearInEasing,
                        ),
                    )
                    (enter togetherWith exit).apply {
                        targetContentZIndex = 1f
                    }
                }
                isPoppingManga -> {
                    val enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = LinearOutSlowInEasing,
                        ),
                    )
                    val exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = FastOutLinearInEasing,
                        ),
                    ) + scaleOut(
                        targetScale = 0.98f,
                        animationSpec = tween(
                            durationMillis = 250,
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
        val isCurrentScreen = screen == navigator.lastItem
        navigator.saveableState("transition", screen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isCurrentScreen) {
                            Modifier
                        } else {
                            // The outgoing screen stays composed during the transition. Consume
                            // pointers so taps and drags cannot reach controls that are leaving.
                            Modifier.pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent(PointerEventPass.Initial)
                                            .changes
                                            .forEach { it.consume() }
                                    }
                                }
                            }
                        },
                    ),
            ) {
                content(screen)
            }
        }
    }

    BackHandler(enabled = navigator.canPop, onBack = navigator::pop)
}
