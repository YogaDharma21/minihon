package eu.kanade.tachiyomi.ui.manga

import eu.kanade.tachiyomi.data.download.model.Download
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.domain.chapter.model.Chapter

class ChapterListGroupingTest {

    private fun createChapter(
        id: Long,
        chapterNumber: Double,
        name: String = "Chapter $chapterNumber",
        scanlator: String? = "Scanlator $id",
        read: Boolean = false,
        lastPageRead: Int = 0,
    ): Chapter {
        return Chapter.create().copy(
            id = id,
            mangaId = 1L,
            read = read,
            lastPageRead = lastPageRead,
            name = name,
            chapterNumber = chapterNumber,
            scanlator = scanlator,
        )
    }

    private fun createItem(
        chapter: Chapter,
        downloadState: Download.State = Download.State.NOT_DOWNLOADED,
        selected: Boolean = false,
    ): ChapterList.Item {
        return ChapterList.Item(
            chapter = chapter,
            downloadState = downloadState,
            selected = selected,
        )
    }

    @Test
    fun `empty list returns empty list`() {
        emptyList<ChapterList.Item>().groupByDuplicateChapterNumber() shouldBe emptyList()
    }

    @Test
    fun `chapters with non positive chapter numbers are not grouped`() {
        val extra1 = createItem(createChapter(id = 1L, chapterNumber = -1.0, name = "Special 1"))
        val extra2 = createItem(createChapter(id = 2L, chapterNumber = -1.0, name = "Special 2"))

        val result = listOf(extra1, extra2).groupByDuplicateChapterNumber()

        result.size shouldBe 2
        result[0].versions shouldBe emptyList()
        result[1].versions shouldBe emptyList()
    }

    @Test
    fun `unique chapter numbers remain unchanged`() {
        val ch1 = createItem(createChapter(id = 1L, chapterNumber = 1.0))
        val ch2 = createItem(createChapter(id = 2L, chapterNumber = 2.0))

        val result = listOf(ch1, ch2).groupByDuplicateChapterNumber()

        result.size shouldBe 2
        result[0].id shouldBe 1L
        result[0].versions shouldBe emptyList()
        result[1].id shouldBe 2L
        result[1].versions shouldBe emptyList()
    }

    @Test
    fun `groups duplicates sharing same chapter number and orders versions with primary first`() {
        val ch61a = createItem(createChapter(id = 101L, chapterNumber = 61.0, scanlator = "Official"))
        val ch61b = createItem(createChapter(id = 102L, chapterNumber = 61.0, scanlator = "Unofficial"))
        val ch60 = createItem(createChapter(id = 103L, chapterNumber = 60.0))

        val result = listOf(ch61a, ch61b, ch60).groupByDuplicateChapterNumber()

        result.size shouldBe 2
        result[0].id shouldBe 101L
        result[0].versions.size shouldBe 2
        result[0].versions[0].id shouldBe 101L
        result[0].versions[1].id shouldBe 102L
        result[1].id shouldBe 103L
        result[1].versions shouldBe emptyList()
    }

    @Test
    fun `prefers read version over unread version`() {
        val ch61Unread = createItem(createChapter(id = 101L, chapterNumber = 61.0, read = false))
        val ch61Read = createItem(createChapter(id = 102L, chapterNumber = 61.0, read = true))

        val result = listOf(ch61Unread, ch61Read).groupByDuplicateChapterNumber()

        result.size shouldBe 1
        result[0].id shouldBe 102L
        result[0].chapter.read shouldBe true
        result[0].versions.size shouldBe 2
        result[0].versions[0].id shouldBe 102L
        result[0].versions[1].id shouldBe 101L
    }

    @Test
    fun `prefers in progress version over unstarted unread version`() {
        val chUnread = createItem(createChapter(id = 101L, chapterNumber = 61.0, read = false, lastPageRead = 0))
        val chInProgress = createItem(createChapter(id = 102L, chapterNumber = 61.0, read = false, lastPageRead = 5))

        val result = listOf(chUnread, chInProgress).groupByDuplicateChapterNumber()

        result.size shouldBe 1
        result[0].id shouldBe 102L
        result[0].versions[0].id shouldBe 102L
    }

    @Test
    fun `prefers downloaded version over not downloaded when both unread`() {
        val chNotDownloaded = createItem(
            chapter = createChapter(id = 101L, chapterNumber = 61.0),
            downloadState = Download.State.NOT_DOWNLOADED,
        )
        val chDownloaded = createItem(
            chapter = createChapter(id = 102L, chapterNumber = 61.0),
            downloadState = Download.State.DOWNLOADED,
        )

        val result = listOf(chNotDownloaded, chDownloaded).groupByDuplicateChapterNumber()

        result.size shouldBe 1
        result[0].id shouldBe 102L
        result[0].versions[0].id shouldBe 102L
    }

    @Test
    fun `breaks ties by preserving first item in list order`() {
        val ch1 = createItem(createChapter(id = 101L, chapterNumber = 61.0, scanlator = "First"))
        val ch2 = createItem(createChapter(id = 102L, chapterNumber = 61.0, scanlator = "Second"))

        val result = listOf(ch1, ch2).groupByDuplicateChapterNumber()

        result.size shouldBe 1
        result[0].id shouldBe 101L
        result[0].versions[0].id shouldBe 101L
        result[0].versions[1].id shouldBe 102L
    }

    @Test
    fun `groupSubChaptersWithParent positions sub-chapters under parent in descending sort`() {
        val ch26 = createItem(createChapter(id = 1L, chapterNumber = 26.0))
        val ch25Part2 = createItem(createChapter(id = 2L, chapterNumber = 25.2))
        val ch25Part1 = createItem(createChapter(id = 3L, chapterNumber = 25.1))
        val ch25 = createItem(createChapter(id = 4L, chapterNumber = 25.0))
        val ch24 = createItem(createChapter(id = 5L, chapterNumber = 24.0))

        val input = listOf(ch26, ch25Part2, ch25Part1, ch25, ch24)
        val result = input.groupSubChaptersWithParent(sortDescending = true)

        result.map { it.chapter.chapterNumber } shouldBe listOf(26.0, 25.0, 25.1, 25.2, 24.0)
    }

    @Test
    fun `groupSubChaptersWithParent preserves ascending sort`() {
        val ch24 = createItem(createChapter(id = 1L, chapterNumber = 24.0))
        val ch25 = createItem(createChapter(id = 2L, chapterNumber = 25.0))
        val ch25Part1 = createItem(createChapter(id = 3L, chapterNumber = 25.1))
        val ch25Part2 = createItem(createChapter(id = 4L, chapterNumber = 25.2))
        val ch26 = createItem(createChapter(id = 5L, chapterNumber = 26.0))

        val input = listOf(ch24, ch25, ch25Part1, ch25Part2, ch26)
        val result = input.groupSubChaptersWithParent(sortDescending = false)

        result.map { it.chapter.chapterNumber } shouldBe listOf(24.0, 25.0, 25.1, 25.2, 26.0)
    }
}
