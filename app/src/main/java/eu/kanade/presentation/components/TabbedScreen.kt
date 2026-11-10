package eu.kanade.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ArrowBack
import mihon.icons.materialsymbols.rounded.Close
import mihon.icons.materialsymbols.rounded.Search
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.clearFocusOnSoftKeyboardHide
import tachiyomi.presentation.core.util.runOnEnterKeyPressed
import tachiyomi.presentation.core.util.showSoftKeyboard

@Composable
fun TabbedScreen(
    titleRes: StringResource,
    tabs: List<TabContent>,
    state: PagerState = rememberPagerState { tabs.size },
    searchQuery: String? = null,
    onChangeSearchQuery: (String?) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        var headerHeightPx by remember { mutableIntStateOf(0) }
        val headerHeight = if (headerHeightPx > 0) {
            LocalDensity.current.run { headerHeightPx.toDp() }
        } else {
            96.dp
        }

        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = state,
                verticalAlignment = Alignment.Top,
            ) { page ->
                tabs[page].content(
                    PaddingValues(
                        top = headerHeight,
                        bottom = contentPadding.calculateBottomPadding(),
                    ),
                    snackbarHostState,
                )
            }

            val tab = tabs[state.currentPage]
            val searchEnabled = tab.searchEnabled
            val isSearching = searchEnabled && searchQuery != null

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(10f)
                    .onSizeChanged { headerHeightPx = it.height },
                contentAlignment = Alignment.TopCenter,
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF121316),
                    border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        if (isSearching) {
                            val focusRequester = remember { FocusRequester() }
                            val searchFieldState = rememberTextFieldState(searchQuery.orEmpty())
                            val currentOnChangeSearchQuery by rememberUpdatedState(onChangeSearchQuery)

                            LaunchedEffect(searchFieldState) {
                                snapshotFlow { searchFieldState.text.toString() }
                                    .drop(1)
                                    .collect { currentOnChangeSearchQuery(it) }
                            }
                            LaunchedEffect(searchQuery) {
                                if (searchQuery != searchFieldState.text.toString()) {
                                    searchFieldState.setTextAndPlaceCursorAtEnd(searchQuery.orEmpty())
                                }
                            }

                            val keyboardController = LocalSoftwareKeyboardController.current
                            val focusManager = LocalFocusManager.current

                            IconButton(onClick = { onChangeSearchQuery(null) }) {
                                Icon(
                                    imageVector = MaterialSymbols.AutoMirroredRounded.ArrowBack,
                                    contentDescription = stringResource(MR.strings.action_cancel),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            BasicTextField(
                                state = searchFieldState,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester)
                                    .runOnEnterKeyPressed {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                    .showSoftKeyboard(searchQuery.isNullOrEmpty())
                                    .clearFocusOnSoftKeyboardHide(),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 16.sp,
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                onKeyboardAction = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                },
                                lineLimits = TextFieldLineLimits.SingleLine,
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorator = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        if (searchFieldState.text.isEmpty()) {
                                            Text(
                                                text = stringResource(MR.strings.action_search_hint),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    .copy(alpha = 0.6f),
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchFieldState.text.isNotEmpty()) {
                                    IconButton(onClick = { searchFieldState.clearText() }) {
                                        Icon(
                                            imageVector = MaterialSymbols.Rounded.Close,
                                            contentDescription = stringResource(MR.strings.action_reset),
                                            tint = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                                AppBarActions(tab.actions)
                            }
                        } else {
                            // Left: Sleek integrated segmented tabs
                            Surface(
                                shape = CircleShape,
                                color = Color(0x14FFFFFF),
                                border = BorderStroke(1.dp, Color(0x10FFFFFF)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    tabs.forEachIndexed { index, tabItem ->
                                        val selected = state.currentPage == index
                                        SegmentedTabButton(
                                            selected = selected,
                                            onClick = { scope.launch { state.animateScrollToPage(index) } },
                                            title = stringResource(tabItem.titleRes),
                                            badgeCount = tabItem.badgeNumber,
                                        )
                                    }
                                }
                            }

                            // Right: Action buttons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchEnabled) {
                                    IconButton(onClick = { onChangeSearchQuery("") }) {
                                        Icon(
                                            imageVector = MaterialSymbols.Rounded.Search,
                                            contentDescription = stringResource(MR.strings.action_search),
                                            tint = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                                AppBarActions(tab.actions)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SegmentedTabButton(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    badgeCount: Int? = null,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) Color(0x28FFFFFF) else Color.Transparent,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "tabBg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0x88FFFFFF),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "tabContent",
    )

    Surface(
        selected = selected,
        onClick = onClick,
        shape = CircleShape,
        color = backgroundColor,
        contentColor = contentColor,
        border = if (selected) BorderStroke(1.dp, Color(0x24FFFFFF)) else null,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    letterSpacing = 0.2.sp,
                ),
                color = contentColor,
            )
            if (badgeCount != null && badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = if (selected) Color(0x33FFFFFF) else Color(0x1FFFFFFF),
                ) {
                    Text(
                        text = "$badgeCount",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        ),
                        color = if (selected) Color.White else Color(0xAAFFFFFF),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    )
                }
            }
        }
    }
}

data class TabContent(
    val titleRes: StringResource,
    val badgeNumber: Int? = null,
    val searchEnabled: Boolean = false,
    val actions: List<AppBar.AppBarAction> = listOf(),
    val content: @Composable (contentPadding: PaddingValues, snackbarHostState: SnackbarHostState) -> Unit,
)
