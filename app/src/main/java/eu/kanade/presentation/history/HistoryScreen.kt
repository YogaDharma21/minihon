package eu.kanade.presentation.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.AppBarTitle
import eu.kanade.presentation.components.SearchToolbar
import eu.kanade.presentation.components.relativeDateText
import eu.kanade.presentation.history.components.HistoryItem
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.presentation.util.rememberIs24HourSystem
import eu.kanade.tachiyomi.ui.history.HistoryViewModel
import kotlinx.datetime.LocalDate
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.DeleteSweep
import mihon.icons.materialsymbols.rounded.Settings
import tachiyomi.domain.history.model.HistoryWithRelations
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.ListGroupHeader
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.collectAsState

@Composable
fun HistoryScreen(
    state: HistoryViewModel.State,
    snackbarHostState: SnackbarHostState,
    onSearchQueryChange: (String?) -> Unit,
    onClickCover: (mangaId: Long) -> Unit,
    onClickResume: (mangaId: Long, chapterId: Long) -> Unit,
    onClickFavorite: (mangaId: Long) -> Unit,
    onDialogChange: (HistoryViewModel.Dialog?) -> Unit,
    onClickSettings: (() -> Unit)? = null,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            SearchToolbar(
                titleContent = { AppBarTitle(stringResource(MR.strings.history)) },
                searchQuery = state.searchQuery,
                onChangeSearchQuery = onSearchQueryChange,
                actions = {
                    AppBarActions(
                        buildList {
                            if (onClickSettings != null) {
                                add(
                                    AppBar.Action(
                                        title = stringResource(MR.strings.label_settings),
                                        icon = MaterialSymbols.Rounded.Settings,
                                        onClick = onClickSettings,
                                    ),
                                )
                            }
                            add(
                                AppBar.Action(
                                    title = stringResource(MR.strings.pref_clear_history),
                                    icon = MaterialSymbols.Rounded.DeleteSweep,
                                    onClick = {
                                        onDialogChange(HistoryViewModel.Dialog.DeleteAll)
                                    },
                                ),
                            )
                        },
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        state.list.let {
            if (it == null) {
                LoadingScreen(Modifier.padding(contentPadding))
            } else if (it.isEmpty()) {
                val msg = if (!state.searchQuery.isNullOrEmpty()) {
                    MR.strings.no_results_found
                } else {
                    MR.strings.information_no_recent_manga
                }
                EmptyScreen(
                    stringRes = msg,
                    modifier = Modifier.padding(contentPadding),
                )
            } else {
                HistoryScreenContent(
                    history = it,
                    contentPadding = contentPadding,
                    onClickCover = { history -> onClickCover(history.mangaId) },
                    onClickResume = { history -> onClickResume(history.mangaId, history.chapterId) },
                    onClickDelete = { item -> onDialogChange(HistoryViewModel.Dialog.Delete(item)) },
                    onClickFavorite = { history -> onClickFavorite(history.mangaId) },
                )
            }
        }
    }
}

@Composable
private fun HistoryScreenContent(
    history: List<HistoryUiModel>,
    contentPadding: PaddingValues,
    onClickCover: (HistoryWithRelations) -> Unit,
    onClickResume: (HistoryWithRelations) -> Unit,
    onClickDelete: (HistoryWithRelations) -> Unit,
    onClickFavorite: (HistoryWithRelations) -> Unit,
) {
    val context = LocalContext.current
    val locale = LocalLocale.current
    val is24HourSystem = rememberIs24HourSystem()
    val timeFormat by remember { context.appGraph.uiPreferences.timeFormat }.collectAsState()
    val timeFormatter = remember(timeFormat, locale, is24HourSystem) {
        UiPreferences.timeFormat(timeFormat, locale.platformLocale, is24HourSystem)
    }

    val groups = remember(history) {
        buildList<Pair<LocalDate?, List<HistoryWithRelations>>> {
            var currentDate: LocalDate? = null
            val currentItems = mutableListOf<HistoryWithRelations>()
            for (uiModel in history) {
                when (uiModel) {
                    is HistoryUiModel.Header -> {
                        if (currentItems.isNotEmpty()) {
                            add(currentDate to currentItems.toList())
                            currentItems.clear()
                        }
                        currentDate = uiModel.date
                    }
                    is HistoryUiModel.Item -> {
                        currentItems.add(uiModel.item)
                    }
                }
            }
            if (currentItems.isNotEmpty()) {
                add(currentDate to currentItems.toList())
            }
        }
    }

    FastScrollLazyColumn(
        contentPadding = contentPadding,
    ) {
        groups.forEach { (date, items) ->
            if (date != null) {
                item(
                    key = "history-header-$date",
                    contentType = "header",
                ) {
                    ListGroupHeader(
                        modifier = Modifier.animateItem(),
                        text = relativeDateText(date),
                    )
                }
            }
            item(
                key = "history-card-${date ?: items.firstOrNull()?.id ?: 0}",
                contentType = "card",
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .animateItem(),
                ) {
                    Column {
                        items.forEach { value ->
                            HistoryItem(
                                history = value,
                                timeFormatter = timeFormatter,
                                onClickCover = { onClickCover(value) },
                                onClickResume = { onClickResume(value) },
                                onClickDelete = { onClickDelete(value) },
                                onClickFavorite = { onClickFavorite(value) },
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed interface HistoryUiModel {
    data class Header(val date: LocalDate) : HistoryUiModel
    data class Item(val item: HistoryWithRelations) : HistoryUiModel
}

@PreviewLightDark
@Composable
internal fun HistoryScreenPreviews(
    @PreviewParameter(HistoryviewModelStateProvider::class)
    historyState: HistoryViewModel.State,
) {
    TachiyomiPreviewTheme {
        HistoryScreen(
            state = historyState,
            snackbarHostState = SnackbarHostState(),
            onSearchQueryChange = {},
            onClickCover = {},
            onClickResume = { _, _ -> run {} },
            onDialogChange = {},
            onClickFavorite = {},
        )
    }
}
