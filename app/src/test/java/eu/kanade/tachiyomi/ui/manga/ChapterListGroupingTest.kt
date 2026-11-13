package eu.kanade.tachiyomi.ui.manga

import eu.kanade.presentation.reader.ReaderChapterGroup
import eu.kanade.presentation.reader.ReaderChapterListItem
import eu.kanade.presentation.reader.insertReaderEncapsulatedGroupHeaders
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.ui.reader.model.ReaderChapter
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

        result.map { it.chapter.chapterNumber } shouldBe listOf(26.0, 25.0, 25.2, 25.1, 24.0)
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

    @Test
    fun `groupSubChaptersWithParent with parent orders parent first then sub-chapters by direction`() {
        val ch20 = createItem(createChapter(id = 1L, chapterNumber = 20.0))
        val ch20Part1 = createItem(createChapter(id = 2L, chapterNumber = 20.1))
        val ch20Part2 = createItem(createChapter(id = 3L, chapterNumber = 20.2))

        val inputDescending = listOf(ch20Part2, ch20Part1, ch20)
        val resultDescending = inputDescending.groupSubChaptersWithParent(sortDescending = true)
        resultDescending.map { it.chapter.chapterNumber } shouldBe listOf(20.0, 20.2, 20.1)

        val inputAscending = listOf(ch20, ch20Part1, ch20Part2)
        val resultAscending = inputAscending.groupSubChaptersWithParent(sortDescending = false)
        resultAscending.map { it.chapter.chapterNumber } shouldBe listOf(20.0, 20.1, 20.2)
    }

    @Test
    fun `groupSubChaptersWithParent without parent orders orphan split sub-chapters by direction`() {
        val ch23Part1 = createItem(createChapter(id = 1L, chapterNumber = 23.1))
        val ch23Part2 = createItem(createChapter(id = 2L, chapterNumber = 23.2))

        val inputDescending = listOf(ch23Part1, ch23Part2)
        val resultDescending = inputDescending.groupSubChaptersWithParent(sortDescending = true)
        resultDescending.map { it.chapter.chapterNumber } shouldBe listOf(23.2, 23.1)

        val inputAscending = listOf(ch23Part2, ch23Part1)
        val resultAscending = inputAscending.groupSubChaptersWithParent(sortDescending = false)
        resultAscending.map { it.chapter.chapterNumber } shouldBe listOf(23.1, 23.2)
    }

    @Test
    fun `insertEncapsulatedGroupHeaders injects GroupHeader with accurate flags and counts`() {
        val ch20 = createItem(createChapter(id = 1L, chapterNumber = 20.0))
        val ch20Part1 = createItem(createChapter(id = 2L, chapterNumber = 20.1))
        val ch20Part2 = createItem(createChapter(id = 3L, chapterNumber = 20.2))
        val ch19 = createItem(createChapter(id = 4L, chapterNumber = 19.0))
        val ch23Part1 = createItem(createChapter(id = 5L, chapterNumber = 23.1))
        val ch23Part2 = createItem(createChapter(id = 6L, chapterNumber = 23.2))

        val input = listOf(ch23Part2, ch23Part1, ch20Part2, ch20Part1, ch20, ch19)
        val result = input.insertEncapsulatedGroupHeaders(sortDescending = true)

        result.size shouldBe 8

        val header23 = result[0] as ChapterList.GroupHeader
        header23.baseNumber shouldBe 23.0
        header23.label shouldBe "Chapter 23"
        header23.partCount shouldBe 2
        header23.isSplit shouldBe true
        (result[1] as ChapterList.Item).chapter.chapterNumber shouldBe 23.2
        (result[2] as ChapterList.Item).chapter.chapterNumber shouldBe 23.1

        val header20 = result[3] as ChapterList.GroupHeader
        header20.baseNumber shouldBe 20.0
        header20.label shouldBe "Chapter 20"
        header20.partCount shouldBe 3
        header20.isSplit shouldBe true
        (result[4] as ChapterList.Item).chapter.chapterNumber shouldBe 20.0
        (result[5] as ChapterList.Item).chapter.chapterNumber shouldBe 20.2
        (result[6] as ChapterList.Item).chapter.chapterNumber shouldBe 20.1

        (result[7] as ChapterList.Item).chapter.chapterNumber shouldBe 19.0
        result.none { it is ChapterList.GroupHeader && it.baseNumber == 19.0 } shouldBe true
    }

    @Test
    fun `insertEncapsulatedGroupHeaders on List of ChapterList preserves MissingCount separators`() {
        val ch20Part1 = createItem(createChapter(id = 1L, chapterNumber = 20.1))
        val ch20 = createItem(createChapter(id = 2L, chapterNumber = 20.0))
        val missing = ChapterList.MissingCount(id = "17-20", count = 2)
        val ch17 = createItem(createChapter(id = 3L, chapterNumber = 17.0))

        val input: List<ChapterList> = listOf(ch20Part1, ch20, missing, ch17)
        val result = input.insertEncapsulatedGroupHeaders(sortDescending = true)

        result.size shouldBe 5
        val header20 = result[0] as ChapterList.GroupHeader
        header20.baseNumber shouldBe 20.0
        header20.partCount shouldBe 2
        header20.isSplit shouldBe true
        (result[1] as ChapterList.Item).chapter.chapterNumber shouldBe 20.1
        (result[2] as ChapterList.Item).chapter.chapterNumber shouldBe 20.0

        (result[3] as ChapterList.MissingCount).count shouldBe 2

        (result[4] as ChapterList.Item).chapter.chapterNumber shouldBe 17.0
        result.none { it is ChapterList.GroupHeader && it.baseNumber == 17.0 } shouldBe true
    }

    @Test
    fun `insertEncapsulatedGroupHeaders never injects headers for single chapter releases`() {
        val ch24 = createItem(createChapter(id = 1L, chapterNumber = 24.0))
        val ch23 = createItem(createChapter(id = 2L, chapterNumber = 23.0))

        val input = listOf(ch24, ch23)
        val result = input.insertEncapsulatedGroupHeaders(sortDescending = true)

        result.size shouldBe 2
        result.none { it is ChapterList.GroupHeader } shouldBe true
    }

    @Test
    fun `insertEncapsulatedGroupHeaders injects header for orphan split sub-chapter`() {
        val ch246Part1 = createItem(createChapter(id = 1L, chapterNumber = 246.1))

        val input = listOf(ch246Part1)
        val result = input.insertEncapsulatedGroupHeaders(sortDescending = true)

        result.size shouldBe 2
        val header = result[0] as ChapterList.GroupHeader
        header.baseNumber shouldBe 246.0
        header.label shouldBe "Chapter 246"
        header.partCount shouldBe 1
        header.isSplit shouldBe true
        (result[1] as ChapterList.Item).chapter.chapterNumber shouldBe 246.1
    }

    @Test
    fun `insertReaderEncapsulatedGroupHeaders clusters groups by base number`() {
        val rc20 = ReaderChapter(createChapter(id = 1L, chapterNumber = 20.0))
        val rc20Part1 = ReaderChapter(createChapter(id = 2L, chapterNumber = 20.1))
        val rc19 = ReaderChapter(createChapter(id = 3L, chapterNumber = 19.0))

        val groups = listOf(
            ReaderChapterGroup(primary = rc20),
            ReaderChapterGroup(primary = rc20Part1),
            ReaderChapterGroup(primary = rc19),
        )
        val result = insertReaderEncapsulatedGroupHeaders(groups)

        result.size shouldBe 4
        val header20 = result[0] as ReaderChapterListItem.Header
        header20.baseNumber shouldBe 20.0
        header20.partCount shouldBe 2
        header20.isSplit shouldBe true
        (result[1] as ReaderChapterListItem.Chapter).group.primary.chapter.chapterNumber shouldBe 20.0
        (result[2] as ReaderChapterListItem.Chapter).group.primary.chapter.chapterNumber shouldBe 20.1

        (result[3] as ReaderChapterListItem.Chapter).group.primary.chapter.chapterNumber shouldBe 19.0
        result.none { it is ReaderChapterListItem.Header && it.baseNumber == 19.0 } shouldBe true
    }
}
