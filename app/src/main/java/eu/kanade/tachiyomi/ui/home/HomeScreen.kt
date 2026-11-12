package eu.kanade.tachiyomi.ui.home

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabNavigator
import eu.kanade.presentation.util.CascadeMotionTrigger
import eu.kanade.presentation.util.LocalCascadeMotionTrigger
import eu.kanade.presentation.util.Screen
import eu.kanade.presentation.util.rememberAnimatedVectorPainter
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.activity.ActivityTab
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.download.DownloadQueueScreen
import eu.kanade.tachiyomi.ui.history.HistoryTab
import eu.kanade.tachiyomi.ui.library.LibraryTab
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.more.MoreTab
import eu.kanade.tachiyomi.ui.updates.UpdatesTab
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Settings
import soup.compose.material.motion.animation.materialFadeThroughIn
import soup.compose.material.motion.animation.materialFadeThroughOut
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.LocalFloatingDockBottomPadding
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

object HomeScreen : Screen() {

    private val librarySearchEvent = Channel<String>()
    private val openTabEvent = Channel<Tab>()
    private val showBottomNavEvent = Channel<Boolean>()

    @Suppress("ConstPropertyName")
    private const val TabFadeDuration = 120

    @Suppress("ConstPropertyName")
    private const val TabNavigatorKey = "HomeTabs"

    internal val TABS = listOf(
        LibraryTab,
        ActivityTab,
        BrowseTab,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        TabNavigator(
            tab = LibraryTab,
            key = TabNavigatorKey,
        ) { tabNavigator ->
            var showBottomNav by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                showBottomNavEvent.receiveAsFlow().collectLatest { show ->
                    showBottomNav = show
                }
            }

            var cascadeTrigger by remember { mutableStateOf(CascadeMotionTrigger()) }
            var wasHomeScreen by remember { mutableStateOf(navigator.lastItem == HomeScreen) }

            LaunchedEffect(tabNavigator.current) {
                cascadeTrigger = CascadeMotionTrigger(
                    id = cascadeTrigger.id + 1,
                    startUptimeMillis = SystemClock.uptimeMillis(),
                )
            }

            LaunchedEffect(navigator.lastItem) {
                val isHome = navigator.lastItem == HomeScreen
                if (isHome && !wasHomeScreen) {
                    cascadeTrigger = CascadeMotionTrigger(
                        id = cascadeTrigger.id + 1,
                        startUptimeMillis = SystemClock.uptimeMillis(),
                    )
                }
                wasHomeScreen = isHome
            }

            val context = LocalContext.current
            val graph = remember { context.appGraph }
            val updatesBadgeCount by remember(graph) {
                combine(
                    graph.libraryPreferences.newShowUpdatesCount.changes(),
                    graph.libraryPreferences.newUpdatesCount.changes(),
                ) { show, count ->
                    if (show) count else 0
                }
            }.collectAsState(initial = 0)
            val extensionBadgeCount by remember(graph) {
                graph.sourcePreferences.extensionUpdatesCount.changes()
            }.collectAsState(initial = 0)

            val libraryImage = AnimatedImageVector.animatedVectorResource(R.drawable.animated_library)
            val activityImage = AnimatedImageVector.animatedVectorResource(R.drawable.animated_updates)
            val browseEnter = AnimatedImageVector.animatedVectorResource(R.drawable.animated_browse_enter)
            val browseExit = AnimatedImageVector.animatedVectorResource(R.drawable.animated_browse_exit)

            val libraryTitle = stringResource(MR.strings.label_library)
            val activityTitle = stringResource(MR.strings.label_activity)
            val browseTitle = stringResource(MR.strings.browse)

            // Provide usable navigator to content screen
            CompositionLocalProvider(
                LocalNavigator provides navigator,
                LocalFloatingDockBottomPadding provides if (showBottomNav) 80.dp else 0.dp,
                LocalCascadeMotionTrigger provides cascadeTrigger,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AnimatedContent(
                        targetState = tabNavigator.current,
                        transitionSpec = {
                            fadeIn(
                                animationSpec = tween(TabFadeDuration),
                            ) togetherWith fadeOut(animationSpec = tween(TabFadeDuration))
                        },
                        modifier = Modifier.fillMaxSize(),
                        label = "tabContent",
                    ) {
                        tabNavigator.saveableState(key = it.key, it) {
                            it.Content()
                        }
                    }

                    AnimatedVisibility(
                        visible = showBottomNav,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF121316),
                                border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                                shadowElevation = 8.dp,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    TABS.fastForEach { tab ->
                                        val selected = tabNavigator.current::class == tab::class
                                        val badgeCount = when (tab) {
                                            is ActivityTab -> updatesBadgeCount
                                            is UpdatesTab -> updatesBadgeCount
                                            is BrowseTab -> extensionBadgeCount
                                            else -> 0
                                        }
                                        val (title, painter) = when (tab) {
                                            is LibraryTab ->
                                                libraryTitle to
                                                    rememberAnimatedVectorPainter(libraryImage, selected)
                                            is ActivityTab ->
                                                activityTitle to
                                                    rememberAnimatedVectorPainter(activityImage, selected)
                                            is BrowseTab ->
                                                browseTitle to
                                                    rememberAnimatedVectorPainter(browseEnter, browseExit, selected)
                                            else -> tab.options.title to tab.options.icon!!
                                        }
                                        FloatingDockItem(
                                            title = title,
                                            painter = painter,
                                            tab = tab,
                                            badgeCount = badgeCount,
                                            selected = selected,
                                            onClick = {
                                                if (!selected) {
                                                    tabNavigator.current = tab
                                                } else {
                                                    scope.launch { tab.onReselect(navigator) }
                                                }
                                            },
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF121316),
                                border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                                shadowElevation = 8.dp,
                                onClick = { navigator.push(MoreTab) },
                            ) {
                                Box(
                                    modifier = Modifier.size(46.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Settings,
                                        contentDescription = stringResource(MR.strings.label_settings),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val goToLibraryTab = { tabNavigator.current = LibraryTab }

            BackHandler(enabled = tabNavigator.current != LibraryTab, onBack = goToLibraryTab)

            LaunchedEffect(Unit) {
                launch {
                    librarySearchEvent.receiveAsFlow().collectLatest {
                        goToLibraryTab()
                        LibraryTab.search(it)
                    }
                }
                launch {
                    openTabEvent.receiveAsFlow().collectLatest {
                        when (it) {
                            is Tab.Library -> {
                                tabNavigator.current = LibraryTab
                                if (it.mangaIdToOpen != null) {
                                    navigator.push(MangaScreen(it.mangaIdToOpen))
                                }
                            }
                            Tab.Updates -> {
                                tabNavigator.current = ActivityTab
                                ActivityTab.showUpdates()
                            }
                            Tab.History -> {
                                tabNavigator.current = ActivityTab
                                ActivityTab.showHistory()
                            }
                            is Tab.Browse -> {
                                if (it.toExtensions) {
                                    BrowseTab.showExtension()
                                }
                                tabNavigator.current = BrowseTab
                            }
                            is Tab.More -> {
                                if (it.toDownloads) {
                                    navigator.push(DownloadQueueScreen)
                                } else {
                                    navigator.push(MoreTab)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun FloatingDockItem(
        title: String,
        painter: androidx.compose.ui.graphics.painter.Painter,
        tab: eu.kanade.presentation.util.Tab,
        badgeCount: Int,
        selected: Boolean,
        onClick: () -> Unit,
    ) {
        val backgroundColor by animateColorAsState(
            targetValue = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            },
            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            label = "dockItemBg",
        )
        val contentColor by animateColorAsState(
            targetValue = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            label = "dockItemContent",
        )

        Surface(
            selected = selected,
            onClick = onClick,
            shape = CircleShape,
            color = backgroundColor,
            contentColor = contentColor,
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                val badge = tabBadge(tab, badgeCount)
                if (badge != null) {
                    BadgedBox(
                        badge = { badge() },
                    ) {
                        Icon(
                            painter = painter,
                            contentDescription = title,
                            tint = contentColor,
                        )
                    }
                } else {
                    Icon(
                        painter = painter,
                        contentDescription = title,
                        tint = contentColor,
                    )
                }

                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn(animationSpec = tween(280, delayMillis = 60)) + expandHorizontally(
                        animationSpec = tween(380, easing = FastOutSlowInEasing),
                        expandFrom = Alignment.Start,
                    ),
                    exit = fadeOut(animationSpec = tween(180)) + shrinkHorizontally(
                        animationSpec = tween(340, easing = FastOutSlowInEasing),
                        shrinkTowards = Alignment.Start,
                    ),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun tabBadge(tab: eu.kanade.presentation.util.Tab, count: Int): (@Composable () -> Unit)? {
        if (count <= 0) return null
        return {
            Badge {
                val desc = when (tab) {
                    is ActivityTab -> pluralStringResource(
                        MR.plurals.notification_chapters_generic,
                        count = count,
                        count,
                    )

                    is UpdatesTab -> pluralStringResource(
                        MR.plurals.notification_chapters_generic,
                        count = count,
                        count,
                    )

                    is BrowseTab -> pluralStringResource(
                        MR.plurals.update_check_notification_ext_updates,
                        count = count,
                        count,
                    )

                    else -> null
                }
                Text(
                    text = count.toString(),
                    modifier = Modifier.semantics {
                        if (desc != null) contentDescription = desc
                    },
                )
            }
        }
    }

    suspend fun search(query: String) {
        librarySearchEvent.send(query)
    }

    suspend fun openTab(tab: Tab) {
        openTabEvent.send(tab)
    }

    suspend fun showBottomNav(show: Boolean) {
        showBottomNavEvent.send(show)
    }

    sealed interface Tab {
        data class Library(val mangaIdToOpen: Long? = null) : Tab
        data object Updates : Tab
        data object History : Tab
        data class Browse(val toExtensions: Boolean = false) : Tab
        data class More(val toDownloads: Boolean = false) : Tab
    }
}
