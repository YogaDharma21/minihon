package eu.kanade.tachiyomi.ui.activity

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.components.TabbedScreen
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.download.DownloadQueueScreen
import eu.kanade.tachiyomi.ui.history.HistoryViewModel
import eu.kanade.tachiyomi.ui.history.historyTab
import eu.kanade.tachiyomi.ui.home.HomeScreen
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.updates.UpdatesViewModel
import eu.kanade.tachiyomi.ui.updates.updatesTab
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

data object ActivityTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            val image = AnimatedImageVector.animatedVectorResource(R.drawable.animated_updates)
            return TabOptions(
                index = 1u,
                title = stringResource(MR.strings.label_activity),
                icon = rememberAnimatedVectorPainter(image, isSelected),
            )
        }

    override suspend fun onReselect(navigator: Navigator) {
        navigator.push(DownloadQueueScreen)
    }

    private val switchTabChannel = Channel<Int>(1, BufferOverflow.DROP_OLDEST)
    private var requestedPage by mutableIntStateOf(0)

    fun showUpdates() {
        requestedPage = 0
        switchTabChannel.trySend(0)
    }

    fun showHistory() {
        requestedPage = 1
        switchTabChannel.trySend(1)
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val updatesViewModel = metroViewModel<UpdatesViewModel>()
        val historyViewModel = metroViewModel<HistoryViewModel>()
        val historyState by historyViewModel.state.collectAsStateWithLifecycle()

        val tabs = listOf(
            updatesTab(updatesViewModel),
            historyTab(historyViewModel),
        )

        val state = rememberPagerState(initialPage = requestedPage) { tabs.size }

        TabbedScreen(
            titleRes = MR.strings.label_activity,
            tabs = tabs,
            state = state,
            searchQuery = historyState.searchQuery,
            onChangeSearchQuery = historyViewModel::updateSearchQuery,
            onSwipePastStart = {
                scope.launch { HomeScreen.openTab(HomeScreen.Tab.Library()) }
            },
            onSwipePastEnd = {
                scope.launch { HomeScreen.openTab(HomeScreen.Tab.Browse(toExtensions = false)) }
            },
        )

        LaunchedEffect(state) {
            switchTabChannel.receiveAsFlow().collectLatest { targetPage ->
                if (state.currentPage != targetPage) {
                    state.animateScrollToPage(targetPage)
                }
            }
        }
        LaunchedEffect(state.currentPage) {
            requestedPage = state.currentPage
        }

        LaunchedEffect(Unit) {
            (context as? MainActivity)?.ready = true
        }
    }
}
