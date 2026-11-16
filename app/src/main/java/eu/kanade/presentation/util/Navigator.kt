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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransitionContent
import eu.kanade.presentation.theme.springLowStiffness
import eu.kanade.tachiyomi.ui.manga.MangaScreen

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
                            durationMillis = 220,
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
                            durationMillis = 220,
                            easing = LinearOutSlowInEasing,
                        ),
                    )
                    val exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutLinearInEasing,
                        ),
                    ) + scaleOut(
                        targetScale = 0.98f,
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutLinearInEasing,
                        ),
                    )
                    (enter togetherWith exit).apply {
                        targetContentZIndex = -1f
                    }
                }
                else -> {
                    val (enter, exit) = if (navigator.lastEvent != StackEvent.Pop) {
                        val enter = slideInHorizontally(
                            animationSpec = springLowStiffness(),
                            initialOffsetX = { it / 4 },
                        ) + fadeIn(
                            animationSpec = springLowStiffness(),
                        ) + scaleIn(
                            animationSpec = springLowStiffness(),
                            initialScale = 0.97f,
                        )
                        val exit = slideOutHorizontally(
                            animationSpec = springLowStiffness(),
                            targetOffsetX = { -it / 4 },
                        ) + fadeOut(
                            animationSpec = springLowStiffness(),
                        ) + scaleOut(
                            animationSpec = springLowStiffness(),
                            targetScale = 0.97f,
                        )
                        enter to exit
                    } else {
                        val enter = slideInHorizontally(
                            animationSpec = springLowStiffness(),
                            initialOffsetX = { -it / 4 },
                        ) + fadeIn(
                            animationSpec = springLowStiffness(),
                        ) + scaleIn(
                            animationSpec = springLowStiffness(),
                            initialScale = 0.97f,
                        )
                        val exit = slideOutHorizontally(
                            animationSpec = springLowStiffness(),
                            targetOffsetX = { it / 4 },
                        ) + fadeOut(
                            animationSpec = springLowStiffness(),
                        ) + scaleOut(
                            animationSpec = springLowStiffness(),
                            targetScale = 0.97f,
                        )
                        enter to exit
                    }
                    enter togetherWith exit
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
