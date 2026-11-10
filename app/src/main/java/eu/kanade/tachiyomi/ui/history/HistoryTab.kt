package eu.kanade.tachiyomi.ui.history

import android.content.Context
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.category.components.ChangeCategoryDialog
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.TabContent
import eu.kanade.presentation.history.HistoryScreen
import eu.kanade.presentation.history.components.HistoryDeleteAllDialog
import eu.kanade.presentation.history.components.HistoryDeleteDialog
import eu.kanade.presentation.manga.DuplicateMangaDialog
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.category.CategoryScreen
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.more.MoreTab
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import mihon.feature.migration.dialog.MigrateMangaDialog
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.DeleteSweep
import mihon.icons.materialsymbols.rounded.Settings
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

data object HistoryTab : Tab {

    internal val resumeLastChapterReadEvent = Channel<Unit>()

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            val image = AnimatedImageVector.animatedVectorResource(R.drawable.animated_history)
            return TabOptions(
                index = 2u,
                title = stringResource(MR.strings.label_recent_manga),
                icon = rememberAnimatedVectorPainter(image, isSelected),
            )
        }

    override suspend fun onReselect(navigator: Navigator) {
        resumeLastChapterReadEvent.send(Unit)
    }

    @Composable
    override fun Content() {
        val viewModel = metroViewModel<HistoryViewModel>()
        val tab = historyTab(viewModel)
        val snackbarHostState = remember { SnackbarHostState() }
        tab.content(PaddingValues(), snackbarHostState)
    }

    internal suspend fun openChapter(context: Context, chapter: Chapter?, snackbarHostState: SnackbarHostState) {
        if (chapter != null) {
            val intent = ReaderActivity.newIntent(context, chapter.mangaId, chapter.id)
            context.startActivity(intent)
        } else {
            snackbarHostState.showSnackbar(context.stringResource(MR.strings.no_next_chapter))
        }
    }
}

@Composable
fun Screen.historyTab(viewModel: HistoryViewModel): TabContent {
    val navigator = LocalNavigator.currentOrThrow
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    return TabContent(
        titleRes = MR.strings.label_recent_manga,
        searchEnabled = true,
        actions = listOf(
            AppBar.Action(
                title = stringResource(MR.strings.pref_clear_history),
                icon = MaterialSymbols.Rounded.DeleteSweep,
                onClick = { viewModel.setDialog(HistoryViewModel.Dialog.DeleteAll) },
            ),
        ),
        content = { contentPadding, snackbarHostState ->
            HistoryScreen(
                state = state,
                snackbarHostState = snackbarHostState,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onClickCover = { navigator.push(MangaScreen(it)) },
                onClickResume = viewModel::getNextChapterForManga,
                onDialogChange = viewModel::setDialog,
                onClickFavorite = viewModel::addFavorite,
                onClickSettings = null,
                showTopBar = false,
                contentPadding = contentPadding,
            )

            val onDismissRequest = { viewModel.setDialog(null) }
            when (val dialog = state.dialog) {
                is HistoryViewModel.Dialog.Delete -> {
                    HistoryDeleteDialog(
                        onDismissRequest = onDismissRequest,
                        onDelete = { all ->
                            if (all) {
                                viewModel.removeAllFromHistory(dialog.history.mangaId)
                            } else {
                                viewModel.removeFromHistory(dialog.history)
                            }
                        },
                    )
                }
                is HistoryViewModel.Dialog.DeleteAll -> {
                    HistoryDeleteAllDialog(
                        onDismissRequest = onDismissRequest,
                        onDelete = viewModel::removeAllHistory,
                    )
                }
                is HistoryViewModel.Dialog.DuplicateManga -> {
                    DuplicateMangaDialog(
                        duplicates = dialog.duplicates,
                        onDismissRequest = onDismissRequest,
                        onConfirm = { viewModel.addFavorite(dialog.manga) },
                        onOpenManga = { navigator.push(MangaScreen(it.id)) },
                        onMigrate = { viewModel.showMigrateDialog(dialog.manga, it) },
                    )
                }
                is HistoryViewModel.Dialog.ChangeCategory -> {
                    ChangeCategoryDialog(
                        initialSelection = dialog.initialSelection,
                        onDismissRequest = onDismissRequest,
                        onEditCategories = { navigator.push(CategoryScreen()) },
                        onConfirm = { include, _ ->
                            viewModel.moveMangaToCategoriesAndAddToLibrary(dialog.manga, include)
                        },
                    )
                }
                is HistoryViewModel.Dialog.Migrate -> {
                    MigrateMangaDialog(
                        current = dialog.current,
                        target = dialog.target,
                        onClickTitle = { navigator.push(MangaScreen(dialog.current.id)) },
                        onDismissRequest = onDismissRequest,
                    )
                }
                null -> {}
            }

            LaunchedEffect(state.list) {
                if (state.list != null) {
                    (context as? MainActivity)?.ready = true
                }
            }

            LaunchedEffect(Unit) {
                viewModel.events.collectLatest { e ->
                    when (e) {
                        HistoryViewModel.Event.InternalError ->
                            snackbarHostState.showSnackbar(context.stringResource(MR.strings.internal_error))
                        HistoryViewModel.Event.HistoryCleared ->
                            snackbarHostState.showSnackbar(context.stringResource(MR.strings.clear_history_completed))
                        is HistoryViewModel.Event.OpenChapter -> HistoryTab.openChapter(
                            context,
                            e.chapter,
                            snackbarHostState,
                        )
                    }
                }
            }

            LaunchedEffect(Unit) {
                HistoryTab.resumeLastChapterReadEvent.receiveAsFlow().collectLatest {
                    HistoryTab.openChapter(context, viewModel.getNextChapter(), snackbarHostState)
                }
            }
        },
    )
}
