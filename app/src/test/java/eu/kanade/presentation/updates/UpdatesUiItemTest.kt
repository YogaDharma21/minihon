package eu.kanade.presentation.updates

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.ui.updates.UpdatesItem
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.domain.updates.model.UpdatesWithRelations
import kotlin.time.Instant

class UpdatesUiItemTest {

    private fun createUpdateItem(mangaId: Long, chapterId: Long, mangaTitle: String = "Manga $mangaId"): UpdatesItem {
        return UpdatesItem(
            update = UpdatesWithRelations(
                mangaId = mangaId,
                mangaTitle = mangaTitle,
                chapterId = chapterId,
                chapterName = "Chapter $chapterId",
                scanlator = null,
                chapterUrl = "/chapter/$chapterId",
                read = false,
                bookmark = false,
                lastPageRead = 0,
                sourceId = 1L,
                dateFetch = Instant.fromEpochMilliseconds(0),
                coverData = MangaCover(mangaId, 1L, false, null, null),
            ),
            downloadStateProvider = { Download.State.NOT_DOWNLOADED },
            downloadProgressProvider = { 0 },
        )
    }

    @Test
    fun `updates tray container constants match specification`() {
        UpdatesTrayContainerColor shouldBe Color(0xFF0D0E11)
        UpdatesTrayStartOffset shouldBe 116.dp
    }

    @Test
    fun `groupUpdatesByManga returns empty list when input is empty`() {
        val groups = groupUpdatesByManga(emptyList())
        groups shouldBe emptyList()
    }

    @Test
    fun `groupUpdatesByManga groups consecutive chapters of same manga`() {
        val item1 = createUpdateItem(mangaId = 1L, chapterId = 101L)
        val item2 = createUpdateItem(mangaId = 1L, chapterId = 102L)
        val item3 = createUpdateItem(mangaId = 2L, chapterId = 201L)

        val groups = groupUpdatesByManga(listOf(item1, item2, item3))

        groups.size shouldBe 2
        groups[0] shouldBe listOf(item1, item2)
        groups[1] shouldBe listOf(item3)
    }

    @Test
    fun `groupUpdatesByManga separates non consecutive chapters of same manga`() {
        val item1 = createUpdateItem(mangaId = 1L, chapterId = 101L)
        val item2 = createUpdateItem(mangaId = 2L, chapterId = 201L)
        val item3 = createUpdateItem(mangaId = 1L, chapterId = 102L)

        val groups = groupUpdatesByManga(listOf(item1, item2, item3))

        groups.size shouldBe 3
        groups[0] shouldBe listOf(item1)
        groups[1] shouldBe listOf(item2)
        groups[2] shouldBe listOf(item3)
    }

    @Test
    fun `groupUpdatesByManga preserves all single chapter items as single element groups`() {
        val item1 = createUpdateItem(mangaId = 1L, chapterId = 101L)
        val item2 = createUpdateItem(mangaId = 2L, chapterId = 201L)
        val item3 = createUpdateItem(mangaId = 3L, chapterId = 301L)

        val groups = groupUpdatesByManga(listOf(item1, item2, item3))

        groups.size shouldBe 3
        groups.all { it.size == 1 } shouldBe true
    }

    @Test
    fun `groupUpdatesByManga detects multi chapter groups with size of 2 or more`() {
        val item1 = createUpdateItem(mangaId = 1L, chapterId = 101L)
        val item2 = createUpdateItem(mangaId = 1L, chapterId = 102L)
        val item3 = createUpdateItem(mangaId = 1L, chapterId = 103L)

        val groups = groupUpdatesByManga(listOf(item1, item2, item3))

        groups.size shouldBe 1
        groups.first().size shouldBe 3
    }
}
