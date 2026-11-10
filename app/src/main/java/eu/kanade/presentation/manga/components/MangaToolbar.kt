package eu.kanade.presentation.manga.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.AppBarTitle
import eu.kanade.presentation.components.DownloadDropdownMenu
import eu.kanade.presentation.components.SearchToolbar
import eu.kanade.presentation.manga.DownloadAction
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.FilterList
import mihon.icons.materialsymbols.rounded.FlipToBack
import mihon.icons.materialsymbols.rounded.Search
import mihon.icons.materialsymbols.rounded.SelectAll
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.active

@Composable
fun MangaToolbar(
    title: String,
    hasFilters: Boolean,
    navigateUp: () -> Unit,
    onClickFilter: () -> Unit,
    onClickShare: (() -> Unit)?,
    onClickDownload: ((DownloadAction) -> Unit)?,
    onClickEditCategory: (() -> Unit)?,
    onClickRefresh: () -> Unit,
    onClickMigrate: (() -> Unit)?,
    onClickEditNotes: () -> Unit,

    // For action mode
    actionModeCounter: Int,
    onCancelActionMode: () -> Unit,
    onSelectAll: () -> Unit,
    onInvertSelection: () -> Unit,

    titleAlphaProvider: () -> Float,
    backgroundAlphaProvider: () -> Float,
    modifier: Modifier = Modifier,
    searchQuery: String? = null,
    onSearchQueryChange: ((String?) -> Unit)? = null,
) {
    val isActionMode = actionModeCounter > 0

    if (!isActionMode && searchQuery != null) {
        SearchToolbar(
            searchQuery = searchQuery,
            onChangeSearchQuery = { onSearchQueryChange?.invoke(it) },
            onClickCloseSearch = { onSearchQueryChange?.invoke(null) },
            placeholderText = stringResource(MR.strings.action_search),
            navigateUp = { onSearchQueryChange?.invoke(null) },
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .consumeWindowInsets(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppBar(
            titleContent = {
                if (isActionMode) {
                    AppBarTitle(actionModeCounter.toString())
                }
            },
            backgroundColor = Color(0xFF121316),
            navigateUp = navigateUp,
            actions = {
                var downloadExpanded by remember { mutableStateOf(false) }
                if (onClickDownload != null) {
                    val onDismissRequest = { downloadExpanded = false }
                    DownloadDropdownMenu(
                        expanded = downloadExpanded,
                        onDismissRequest = onDismissRequest,
                        onDownloadClicked = onClickDownload,
                    )
                }

                val filterTint = if (hasFilters) MaterialTheme.colorScheme.active else LocalContentColor.current
                AppBarActions(
                    actions = buildList {
                        if (isActionMode) {
                            add(
                                AppBar.Action(
                                    title = stringResource(MR.strings.action_select_all),
                                    icon = MaterialSymbols.Rounded.SelectAll,
                                    onClick = onSelectAll,
                                ),
                            )
                            add(
                                AppBar.Action(
                                    title = stringResource(MR.strings.action_select_inverse),
                                    icon = MaterialSymbols.Rounded.FlipToBack,
                                    onClick = onInvertSelection,
                                ),
                            )
                            return@buildList
                        }
                        if (onClickDownload != null) {
                            add(
                                AppBar.Action(
                                    title = stringResource(MR.strings.manga_download),
                                    icon = MaterialSymbols.Rounded.Download,
                                    onClick = { downloadExpanded = !downloadExpanded },
                                ),
                            )
                        }
                        if (onSearchQueryChange != null) {
                            add(
                                AppBar.Action(
                                    title = stringResource(MR.strings.action_search),
                                    icon = MaterialSymbols.Rounded.Search,
                                    onClick = { onSearchQueryChange("") },
                                ),
                            )
                        }
                        add(
                            AppBar.Action(
                                title = stringResource(MR.strings.action_filter),
                                icon = MaterialSymbols.Rounded.FilterList,
                                iconTint = filterTint,
                                onClick = onClickFilter,
                            ),
                        )
                        add(
                            AppBar.OverflowAction(
                                title = stringResource(MR.strings.action_webview_refresh),
                                onClick = onClickRefresh,
                            ),
                        )
                        if (onClickEditCategory != null) {
                            add(
                                AppBar.OverflowAction(
                                    title = stringResource(MR.strings.action_edit_categories),
                                    onClick = onClickEditCategory,
                                ),
                            )
                        }
                        if (onClickMigrate != null) {
                            add(
                                AppBar.OverflowAction(
                                    title = stringResource(MR.strings.action_migrate),
                                    onClick = onClickMigrate,
                                ),
                            )
                        }
                        if (onClickShare != null) {
                            add(
                                AppBar.OverflowAction(
                                    title = stringResource(MR.strings.action_share),
                                    onClick = onClickShare,
                                ),
                            )
                        }
                        add(
                            AppBar.OverflowAction(
                                title = stringResource(MR.strings.action_notes),
                                onClick = onClickEditNotes,
                            ),
                        )
                    },
                )
            },
            isActionMode = isActionMode,
            onCancelActionMode = onCancelActionMode,
        )

        val titleAlpha = titleAlphaProvider()
        if (!isActionMode && titleAlpha > 0.01f) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = CircleShape,
                color = Color(0xFF121316),
                border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .alpha(titleAlpha)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}
