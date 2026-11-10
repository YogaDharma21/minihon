package eu.kanade.tachiyomi.ui.updates

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.TabContent
import eu.kanade.presentation.updates.UpdatesDeleteConfirmationDialog
import eu.kanade.presentation.updates.UpdatesFilterDialog
import eu.kanade.presentation.updates.UpdatesScreen
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.download.DownloadQueueScreen
import eu.kanade.tachiyomi.ui.home.HomeScreen
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.more.MoreTab
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.ui.updates.UpdatesViewModel.Event
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.FilterList
import mihon.icons.materialsymbols.rounded.Refresh
import mihon.icons.materialsymbols.rounded.Settings
import mihon.icons.materialsymbols.rounded.UnfoldLess
import mihon.icons.materialsymbols.rounded.UnfoldMore
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

data object UpdatesTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            val image = AnimatedImageVector.animatedVectorResource(R.drawable.animated_updates)
            return TabOptions(
                index = 1u,
                title = stringResource(MR.strings.label_recent_updates),
                icon = rememberAnimatedVectorPainter(image, isSelected),
            )
        }

    override suspend fun onReselect(navigator: Navigator) {
        navigator.push(DownloadQueueScreen)
    }

    @Composable
    override fun Content() {
        val viewModel = metroViewModel<UpdatesViewModel>()
        val tab = updatesTab(viewModel)
        val snackbarHostState = remember { SnackbarHostState() }
        tab.content(PaddingValues(), snackbarHostState)
    }
}

@Composable
fun Screen.updatesTab(viewModel: UpdatesViewModel): TabContent {
    val context = LocalContext.current
    val navigator = LocalNavigator.currentOrThrow
    val settingsViewModel = metroViewModel<UpdatesSettingsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val multiChapterMangaIds = remember(state.items) {
        state.items.groupingBy { it.update.mangaId }
            .eachCount()
            .filterValues { it > 1 }
            .keys
    }
    val allCollapsed = multiChapterMangaIds.isNotEmpty() && multiChapterMangaIds.all { it in state.collapsedMangaIds }

    return TabContent(
        titleRes = MR.strings.label_recent_updates,
        actions = listOf(
            AppBar.Action(
                title = stringResource(MR.strings.action_update_library),
                icon = MaterialSymbols.Rounded.Refresh,
                onClick = { scope.launch { viewModel.updateLibrary() } },
            ),
            AppBar.Action(
                title = stringResource(MR.strings.action_filter),
                icon = MaterialSymbols.Rounded.FilterList,
                onClick = viewModel::showFilterDialog,
            ),
            AppBar.Action(
                title = "Collapse/Expand all",
                icon = if (allCollapsed) MaterialSymbols.Rounded.UnfoldMore else MaterialSymbols.Rounded.UnfoldLess,
                onClick = viewModel::toggleCollapseAll,
            ),
        ),
        content = { contentPadding, snackbarHostState ->
            UpdatesScreen(
                state = state,
                snackbarHostState = snackbarHostState,
                lastUpdated = viewModel.lastUpdated,
                onClickCover = { item -> navigator.push(MangaScreen(item.update.mangaId)) },
                onSelectAll = viewModel::toggleAllSelection,
                onInvertSelection = viewModel::invertSelection,
                onUpdateLibrary = viewModel::updateLibrary,
                onDownloadChapter = viewModel::downloadChapters,
                onMultiBookmarkClicked = viewModel::bookmarkUpdates,
                onMultiMarkAsReadClicked = viewModel::markUpdatesRead,
                onMultiDeleteClicked = viewModel::showConfirmDeleteChapters,
                onUpdateSelected = viewModel::toggleSelection,
                onOpenChapter = {
                    val intent = ReaderActivity.newIntent(context, it.update.mangaId, it.update.chapterId)
                    context.startActivity(intent)
                },
                onFilterClicked = viewModel::showFilterDialog,
                hasActiveFilters = state.hasActiveFilters,
                collapsedMangaIds = state.collapsedMangaIds,
                onToggleMangaCollapse = viewModel::toggleMangaCollapse,
                onToggleCollapseAll = viewModel::toggleCollapseAll,
                onClickSettings = null,
                showTopBar = false,
                contentPadding = contentPadding,
            )

            val onDismissDialog = { viewModel.setDialog(null) }
            when (val dialog = state.dialog) {
                is UpdatesViewModel.Dialog.DeleteConfirmation -> {
                    UpdatesDeleteConfirmationDialog(
                        onDismissRequest = onDismissDialog,
                        onConfirm = { viewModel.deleteChapters(dialog.toDelete) },
                    )
                }
                is UpdatesViewModel.Dialog.FilterSheet -> {
                    UpdatesFilterDialog(
                        onDismissRequest = onDismissDialog,
                        viewModel = settingsViewModel,
                    )
                }
                null -> {}
            }

            LaunchedEffect(Unit) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        Event.InternalError -> snackbarHostState.showSnackbar(
                            context.stringResource(MR.strings.internal_error),
                        )
                        is Event.LibraryUpdateTriggered -> {
                            val msg = if (event.started) {
                                MR.strings.updating_library
                            } else {
                                MR.strings.update_already_running
                            }
                            snackbarHostState.showSnackbar(context.stringResource(msg))
                        }
                    }
                }
            }

            LaunchedEffect(state.selectionMode) {
                HomeScreen.showBottomNav(!state.selectionMode)
            }

            LaunchedEffect(state.isLoading) {
                if (!state.isLoading) {
                    (context as? MainActivity)?.ready = true
                }
            }
            DisposableEffect(Unit) {
                viewModel.resetNewUpdatesCount()

                onDispose {
                    viewModel.resetNewUpdatesCount()
                }
            }
        },
    )
}
