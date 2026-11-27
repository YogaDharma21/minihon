package eu.kanade.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import eu.kanade.presentation.theme.LocalScrollChromeState
import eu.kanade.presentation.theme.rememberScrollChromeState
import eu.kanade.presentation.theme.topAppBarEnterTransition
import eu.kanade.presentation.theme.topAppBarExitTransition
import tachiyomi.presentation.core.components.LocalScrollChromeActive
import tachiyomi.presentation.core.components.LocalScrollChromeObserver
import tachiyomi.presentation.core.components.material.Scaffold

private var cachedScrollChromeHeaderHeightPx: Int = 0
private val AlwaysActive: () -> Boolean = { true }

@Composable
fun ScrollChromeScaffold(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isTopBarPinned: Boolean = false,
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (contentPadding: PaddingValues) -> Unit,
) {
    var baseHeaderHeightPx by rememberSaveable { mutableIntStateOf(cachedScrollChromeHeaderHeightPx) }
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val fallbackHeight = statusBarTop + 76.dp
    val headerHeight = if (baseHeaderHeightPx > 0) {
        with(LocalDensity.current) { baseHeaderHeightPx.toDp() }
    } else {
        fallbackHeight
    }

    val currentPinned by rememberUpdatedState(isTopBarPinned)
    val chromeState = rememberScrollChromeState(isPinned = { currentPinned })

    LaunchedEffect(isTopBarPinned) {
        if (!isTopBarPinned) {
            chromeState.notifyBoundaryChanged()
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
    ) { contentPadding ->
        val layoutDirection = LocalLayoutDirection.current
        val effectivePadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection),
            top = headerHeight,
            end = contentPadding.calculateEndPadding(layoutDirection),
            bottom = contentPadding.calculateBottomPadding(),
        )

        CompositionLocalProvider(
            LocalScrollChromeObserver provides chromeState,
            LocalScrollChromeState provides chromeState,
            LocalScrollChromeActive provides AlwaysActive,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(chromeState.nestedScrollConnection),
            ) {
                content(effectivePadding)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .zIndex(10f),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    AnimatedVisibility(
                        visible = chromeState.isVisible || isTopBarPinned,
                        enter = topAppBarEnterTransition,
                        exit = topAppBarExitTransition,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onSizeChanged {
                                    if (it.height > 0) {
                                        baseHeaderHeightPx = it.height
                                        cachedScrollChromeHeaderHeightPx = it.height
                                    }
                                },
                        ) {
                            topBar()
                        }
                    }
                }
            }
        }
    }
}
