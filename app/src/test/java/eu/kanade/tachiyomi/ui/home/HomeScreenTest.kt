package eu.kanade.tachiyomi.ui.home

import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.history.HistoryTab
import eu.kanade.tachiyomi.ui.library.LibraryTab
import eu.kanade.tachiyomi.ui.more.MoreTab
import eu.kanade.tachiyomi.ui.updates.UpdatesTab
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import org.junit.jupiter.api.Test

class HomeScreenTest {

    @Test
    fun `home screen tabs contains 4 primary tabs`() {
        HomeScreen.TABS shouldBe listOf(
            LibraryTab,
            UpdatesTab,
            HistoryTab,
            BrowseTab,
        )
    }

    @Test
    fun `home screen tabs does not contain more tab`() {
        HomeScreen.TABS.contains(MoreTab) shouldBe false
    }

    @Test
    fun `home screen tab types support all navigation targets`() {
        HomeScreen.Tab.Library(1L).mangaIdToOpen shouldBe 1L
        HomeScreen.Tab.Updates shouldBe HomeScreen.Tab.Updates
        HomeScreen.Tab.History shouldBe HomeScreen.Tab.History
        HomeScreen.Tab.Browse(toExtensions = true).toExtensions shouldBe true
        HomeScreen.Tab.More(toDownloads = true).toDownloads shouldBe true
    }

    @Test
    fun `more tab is a valid screen destination for top bar navigation`() {
        (MoreTab is cafe.adriel.voyager.core.screen.Screen) shouldBe true
    }
}
