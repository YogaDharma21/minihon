package eu.kanade.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import eu.kanade.presentation.theme.ScrollChromeConnection
import eu.kanade.presentation.theme.topAppBarEnterTransition
import eu.kanade.presentation.theme.topAppBarExitTransition
import tachiyomi.presentation.core.components.material.Scaffold

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
    var baseHeaderHeightPx by remember { mutableIntStateOf(0) }
    val headerHeight = if (baseHeaderHeightPx > 0) {
        with(LocalDensity.current) { baseHeaderHeightPx.toDp() }
    } else {
        72.dp
    }

    var showTopBar by remember { mutableStateOf(true) }
    val topBarScrollConnection = remember(isTopBarPinned) {
        ScrollChromeConnection(
            onVisibilityChanged = { show ->
                if (!isTopBarPinned && showTopBar != show) {
                    showTopBar = show
                }
            },
        )
    }

    LaunchedEffect(isTopBarPinned) {
        if (isTopBarPinned) {
            showTopBar = true
            topBarScrollConnection.reset()
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
            top = contentPadding.calculateTopPadding() + headerHeight,
            end = contentPadding.calculateEndPadding(layoutDirection),
            bottom = contentPadding.calculateBottomPadding(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(topBarScrollConnection),
        ) {
            content(effectivePadding)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(10f)
                    .onSizeChanged { if (it.height > baseHeaderHeightPx) baseHeaderHeightPx = it.height },
                contentAlignment = Alignment.TopCenter,
            ) {
                AnimatedVisibility(
                    visible = showTopBar || isTopBarPinned,
                    enter = topAppBarEnterTransition,
                    exit = topAppBarExitTransition,
                ) {
                    topBar()
                }
            }
        }
    }
}
