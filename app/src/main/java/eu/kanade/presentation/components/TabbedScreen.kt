package eu.kanade.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.focus.FocusDirection
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Close
import mihon.icons.materialsymbols.rounded.Search
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.TabText
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.clearFocusOnSoftKeyboardHide
import tachiyomi.presentation.core.util.runOnEnterKeyPressed
import tachiyomi.presentation.core.util.secondaryItemAlpha
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

    val tab = tabs[state.currentPage]
    val searchEnabled = tab.searchEnabled
    val effectiveSearchQuery = if (searchEnabled) searchQuery else null

    val focusRequester = remember { FocusRequester() }
    val searchFieldState = rememberTextFieldState(effectiveSearchQuery.orEmpty())
    val currentOnChangeSearchQuery by rememberUpdatedState(onChangeSearchQuery)
    val searchOpen by rememberUpdatedState(effectiveSearchQuery != null)
    val interactionSource = remember { MutableInteractionSource() }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val searchAndClearFocus: () -> Unit = {
        focusManager.clearFocus()
        keyboardController?.hide()
        focusManager.moveFocus(FocusDirection.Next)
    }

    LaunchedEffect(searchFieldState) {
        snapshotFlow { searchFieldState.text.toString() }
            .drop(1)
            .collect { if (searchOpen) currentOnChangeSearchQuery(it) }
    }
    LaunchedEffect(effectiveSearchQuery) {
        when {
            effectiveSearchQuery == null -> searchFieldState.clearText()
            effectiveSearchQuery != searchFieldState.text.toString() -> {
                searchFieldState.setTextAndPlaceCursorAtEnd(effectiveSearchQuery)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        var headerHeightPx by remember { mutableIntStateOf(0) }
        val headerHeight = if (headerHeightPx > 0) {
            LocalDensity.current.run { headerHeightPx.toDp() }
        } else {
            120.dp
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(10f)
                    .onSizeChanged { headerHeightPx = it.height },
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF121316),
                    border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        // Row 1: The title and action icons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                if (effectiveSearchQuery == null) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 4.dp),
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        ProvideTextStyle(
                                            MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            ),
                                        ) {
                                            AppBarTitle(stringResource(titleRes))
                                        }
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            onChangeSearchQuery(null)
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        },
                                    ) {
                                        UpIcon()
                                    }
                                    BasicTextField(
                                        state = searchFieldState,
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(focusRequester)
                                            .runOnEnterKeyPressed(action = searchAndClearFocus)
                                            .showSoftKeyboard(effectiveSearchQuery.isEmpty())
                                            .clearFocusOnSoftKeyboardHide(),
                                        textStyle = MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 18.sp,
                                        ),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        onKeyboardAction = { searchAndClearFocus() },
                                        lineLimits = TextFieldLineLimits.SingleLine,
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                                        interactionSource = interactionSource,
                                        decorator = TextFieldDefaults.decorator(
                                            state = searchFieldState,
                                            enabled = true,
                                            lineLimits = TextFieldLineLimits.SingleLine,
                                            outputTransformation = null,
                                            interactionSource = interactionSource,
                                            placeholder = {
                                                Text(
                                                    modifier = Modifier.secondaryItemAlpha(),
                                                    text = stringResource(MR.strings.action_search_hint),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Normal,
                                                    ),
                                                )
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            container = {},
                                        ),
                                    )
                                }

                                if (searchEnabled) {
                                    if (effectiveSearchQuery == null) {
                                        TooltipBox(
                                            positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                                            tooltip = {
                                                PlainTooltip {
                                                    Text(stringResource(MR.strings.action_search))
                                                }
                                            },
                                            state = rememberTooltipState(),
                                            focusable = false,
                                        ) {
                                            IconButton(
                                                onClick = { onChangeSearchQuery("") },
                                            ) {
                                                Icon(
                                                    MaterialSymbols.Rounded.Search,
                                                    contentDescription = stringResource(MR.strings.action_search),
                                                )
                                            }
                                        }
                                    } else if (effectiveSearchQuery.isNotEmpty()) {
                                        TooltipBox(
                                            positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                                            tooltip = {
                                                PlainTooltip {
                                                    Text(stringResource(MR.strings.action_reset))
                                                }
                                            },
                                            state = rememberTooltipState(),
                                            focusable = false,
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    searchFieldState.clearText()
                                                    onChangeSearchQuery("")
                                                    focusRequester.requestFocus()
                                                },
                                            ) {
                                                Icon(
                                                    MaterialSymbols.Rounded.Close,
                                                    contentDescription = stringResource(MR.strings.action_reset),
                                                )
                                            }
                                        }
                                    }
                                }

                                AppBarActions(tab.actions)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Row 2: Inset compact segmented tab switcher row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0x0EFFFFFF),
                                border = BorderStroke(1.dp, Color(0x0AFFFFFF)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(2.dp),
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
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "tabBg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "tabContent",
    )

    Surface(
        selected = selected,
        onClick = onClick,
        shape = CircleShape,
        color = backgroundColor,
        contentColor = contentColor,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                ProvideTextStyle(
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                ) {
                    TabText(text = title, badgeCount = badgeCount)
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
