package eu.kanade.presentation.reader

import eu.kanade.tachiyomi.ui.reader.model.ReaderChapter
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.domain.chapter.model.Chapter

class ReaderChapterListSheetTest {

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

    private fun createReaderChapter(
        id: Long,
        chapterNumber: Double,
        name: String = "Chapter $chapterNumber",
        scanlator: String? = "Scanlator $id",
        read: Boolean = false,
        lastPageRead: Int = 0,
    ): ReaderChapter {
        return ReaderChapter(
            createChapter(
                id = id,
                chapterNumber = chapterNumber,
                name = name,
                scanlator = scanlator,
                read = read,
                lastPageRead = lastPageRead,
            ),
        )
    }

    // 1. groupReaderChapters grouping single chapters correctly

    @Test
    fun `empty list returns empty list`() {
        groupReaderChapters(emptyList()) shouldBe emptyList()
    }

    @Test
    fun `single chapter without duplicates or subchapters is grouped correctly`() {
        val ch1 = createReaderChapter(id = 1L, chapterNumber = 1.0)
        val result = groupReaderChapters(listOf(ch1))

        result.size shouldBe 1
        result[0].primary.chapter.id shouldBe 1L
        result[0].versions shouldBe emptyList()
        result[0].isSubChapter shouldBe false
        result[0].isFirstSubChapter shouldBe false
        result[0].isLastSubChapter shouldBe true
        result[0].hasParentAbove shouldBe false
        result[0].hasSubChaptersBelow shouldBe false
    }

    @Test
    fun `distinct integer chapters in descending order preserve order and default flags`() {
        val chapters = listOf(
            createReaderChapter(id = 3L, chapterNumber = 3.0),
            createReaderChapter(id = 2L, chapterNumber = 2.0),
            createReaderChapter(id = 1L, chapterNumber = 1.0),
        )

        val result = groupReaderChapters(chapters)

        result.size shouldBe 3
        result.map { it.primary.chapter.chapterNumber } shouldBe listOf(3.0, 2.0, 1.0)
        result.all { it.versions.isEmpty() } shouldBe true
        result.all { !it.isSubChapter && !it.hasParentAbove && !it.hasSubChaptersBelow } shouldBe true
    }

    @Test
    fun `distinct integer chapters in ascending order preserve order and default flags`() {
        val chapters = listOf(
            createReaderChapter(id = 1L, chapterNumber = 1.0),
            createReaderChapter(id = 2L, chapterNumber = 2.0),
            createReaderChapter(id = 3L, chapterNumber = 3.0),
        )

        val result = groupReaderChapters(chapters)

        result.size shouldBe 3
        result.map { it.primary.chapter.chapterNumber } shouldBe listOf(1.0, 2.0, 3.0)
        result.all { it.versions.isEmpty() } shouldBe true
        result.all { !it.isSubChapter && !it.hasParentAbove && !it.hasSubChaptersBelow } shouldBe true
    }

    // 2. groupReaderChapters grouping sub-chapters and tracking flags

    @Test
    fun `descending sub-chapters reorder parent above sub-chapters and set hierarchy flags`() {
        val chapters = listOf(
            createReaderChapter(id = 1L, chapterNumber = 11.0),
            createReaderChapter(id = 2L, chapterNumber = 10.2),
            createReaderChapter(id = 3L, chapterNumber = 10.1),
            createReaderChapter(id = 4L, chapterNumber = 10.0),
            createReaderChapter(id = 5L, chapterNumber = 9.0),
        )

        val result = groupReaderChapters(chapters)

        result.size shouldBe 5
        result.map { it.primary.chapter.chapterNumber } shouldBe listOf(11.0, 10.0, 10.2, 10.1, 9.0)

        // Chapter 11.0
        result[0].isSubChapter shouldBe false
        result[0].hasParentAbove shouldBe false
        result[0].hasSubChaptersBelow shouldBe false

        // Chapter 10.0 (parent placed above sub-chapters)
        result[1].isSubChapter shouldBe false
        result[1].hasParentAbove shouldBe false
        result[1].hasSubChaptersBelow shouldBe true

        // Chapter 10.2 (first sub-chapter under parent)
        result[2].isSubChapter shouldBe true
        result[2].isFirstSubChapter shouldBe false
        result[2].isLastSubChapter shouldBe false
        result[2].hasParentAbove shouldBe true
        result[2].hasSubChaptersBelow shouldBe false

        // Chapter 10.1 (last sub-chapter of group)
        result[3].isSubChapter shouldBe true
        result[3].isFirstSubChapter shouldBe false
        result[3].isLastSubChapter shouldBe true
        result[3].hasParentAbove shouldBe false
        result[3].hasSubChaptersBelow shouldBe false

        // Chapter 9.0
        result[4].isSubChapter shouldBe false
        result[4].hasParentAbove shouldBe false
        result[4].hasSubChaptersBelow shouldBe false
    }

    @Test
    fun `sub-chapters without parent chapter set isFirstSubChapter and isLastSubChapter with hasParentAbove false`() {
        val chapters = listOf(
            createReaderChapter(id = 1L, chapterNumber = 11.0),
            createReaderChapter(id = 2L, chapterNumber = 10.5),
            createReaderChapter(id = 3L, chapterNumber = 10.1),
            createReaderChapter(id = 4L, chapterNumber = 9.0),
        )

        val result = groupReaderChapters(chapters)

        result.size shouldBe 4
        result.map { it.primary.chapter.chapterNumber } shouldBe listOf(11.0, 10.5, 10.1, 9.0)

        // Chapter 10.5
        result[1].isSubChapter shouldBe true
        result[1].isFirstSubChapter shouldBe true
        result[1].isLastSubChapter shouldBe false
        result[1].hasParentAbove shouldBe false

        // Chapter 10.1
        result[2].isSubChapter shouldBe true
        result[2].isFirstSubChapter shouldBe false
        result[2].isLastSubChapter shouldBe true
        result[2].hasParentAbove shouldBe false
    }

    @Test
    fun `single standalone sub-chapter sets both isFirstSubChapter and isLastSubChapter`() {
        val chapters = listOf(
            createReaderChapter(id = 1L, chapterNumber = 12.0),
            createReaderChapter(id = 2L, chapterNumber = 10.5),
            createReaderChapter(id = 3L, chapterNumber = 9.0),
        )

        val result = groupReaderChapters(chapters)

        result.size shouldBe 3
        val sub = result[1]
        sub.primary.chapter.chapterNumber shouldBe 10.5
        sub.isSubChapter shouldBe true
        sub.isFirstSubChapter shouldBe true
        sub.isLastSubChapter shouldBe true
        sub.hasParentAbove shouldBe false
    }

    // 3. groupReaderChapters grouping identical chapter numbers into versions

    @Test
    fun `groups duplicates sharing same chapter number and orders versions with primary first`() {
        val ch10v1 = createReaderChapter(id = 101L, chapterNumber = 10.0, scanlator = "Scanlator A")
        val ch10v2 = createReaderChapter(id = 102L, chapterNumber = 10.0, scanlator = "Scanlator B")
        val ch9 = createReaderChapter(id = 103L, chapterNumber = 9.0)

        val result = groupReaderChapters(listOf(ch10v1, ch10v2, ch9))

        result.size shouldBe 2
        result[0].primary.chapter.id shouldBe 101L
        result[0].versions.size shouldBe 2
        result[0].versions[0].chapter.id shouldBe 101L
        result[0].versions[1].chapter.id shouldBe 102L

        result[1].primary.chapter.id shouldBe 103L
        result[1].versions shouldBe emptyList()
    }

    @Test
    fun `prefers read version over unread or in-progress versions as primary`() {
        val unread = createReaderChapter(id = 101L, chapterNumber = 10.0, read = false, lastPageRead = 0)
        val inProgress = createReaderChapter(id = 102L, chapterNumber = 10.0, read = false, lastPageRead = 15)
        val read = createReaderChapter(id = 103L, chapterNumber = 10.0, read = true, lastPageRead = 0)
        val ch9 = createReaderChapter(id = 104L, chapterNumber = 9.0)

        val result = groupReaderChapters(listOf(unread, inProgress, read, ch9))

        result.size shouldBe 2
        result[0].primary.chapter.id shouldBe 103L
        result[0].versions.map { it.chapter.id } shouldBe listOf(103L, 101L, 102L)
    }

    @Test
    fun `prefers in-progress version with lastPageRead over unread version when none read`() {
        val unread = createReaderChapter(id = 101L, chapterNumber = 10.0, read = false, lastPageRead = 0)
        val inProgress = createReaderChapter(id = 102L, chapterNumber = 10.0, read = false, lastPageRead = 5)

        val result = groupReaderChapters(listOf(unread, inProgress))

        result.size shouldBe 1
        result[0].primary.chapter.id shouldBe 102L
        result[0].versions.map { it.chapter.id } shouldBe listOf(102L, 101L)
    }

    @Test
    fun `chapters with non positive chapter numbers are not grouped into duplicate versions`() {
        val extra1 = createReaderChapter(id = 1L, chapterNumber = -1.0, name = "Special 1")
        val extra2 = createReaderChapter(id = 2L, chapterNumber = -1.0, name = "Special 2")

        val result = groupReaderChapters(listOf(extra1, extra2))

        result.size shouldBe 2
        result[0].versions shouldBe emptyList()
        result[1].versions shouldBe emptyList()
    }

    // 4. insertReaderEncapsulatedGroupHeaders inserting group headers properly

    @Test
    fun `insertReaderEncapsulatedGroupHeaders returns empty list when input is empty`() {
        insertReaderEncapsulatedGroupHeaders(emptyList()) shouldBe emptyList()
    }

    @Test
    fun `groups without sub-chapters do not produce group headers`() {
        val groups = listOf(
            ReaderChapterGroup(primary = createReaderChapter(id = 1L, chapterNumber = 3.0)),
            ReaderChapterGroup(primary = createReaderChapter(id = 2L, chapterNumber = 2.0)),
        )

        val result = insertReaderEncapsulatedGroupHeaders(groups)

        result.size shouldBe 2
        result.all { it is ReaderChapterListItem.Chapter } shouldBe true
    }

    @Test
    fun `cluster containing sub-chapters inserts header with correct properties`() {
        val groups = listOf(
            ReaderChapterGroup(primary = createReaderChapter(id = 1L, chapterNumber = 10.0)),
            ReaderChapterGroup(primary = createReaderChapter(id = 2L, chapterNumber = 10.2)),
            ReaderChapterGroup(primary = createReaderChapter(id = 3L, chapterNumber = 10.1)),
            ReaderChapterGroup(primary = createReaderChapter(id = 4L, chapterNumber = 9.0)),
        )

        val result = insertReaderEncapsulatedGroupHeaders(groups)

        result.size shouldBe 5

        val header = result[0] as ReaderChapterListItem.Header
        header.id shouldBe "reader-group-10"
        header.baseNumber shouldBe 10.0
        header.label shouldBe "Chapter 10"
        header.partCount shouldBe 3
        header.isSplit shouldBe true

        (result[1] as ReaderChapterListItem.Chapter).group.primary.chapter.chapterNumber shouldBe 10.0
        (result[2] as ReaderChapterListItem.Chapter).group.primary.chapter.chapterNumber shouldBe 10.2
        (result[3] as ReaderChapterListItem.Chapter).group.primary.chapter.chapterNumber shouldBe 10.1
        (result[4] as ReaderChapterListItem.Chapter).group.primary.chapter.chapterNumber shouldBe 9.0
    }

    @Test
    fun `standalone sub-chapters without parent chapter insert header`() {
        val groups = listOf(
            ReaderChapterGroup(primary = createReaderChapter(id = 1L, chapterNumber = 5.5)),
            ReaderChapterGroup(primary = createReaderChapter(id = 2L, chapterNumber = 4.0)),
        )

        val result = insertReaderEncapsulatedGroupHeaders(groups)

        result.size shouldBe 3

        val header = result[0] as ReaderChapterListItem.Header
        header.id shouldBe "reader-group-5"
        header.baseNumber shouldBe 5.0
        header.partCount shouldBe 1
        header.isSplit shouldBe true

        (result[1] as ReaderChapterListItem.Chapter).group.primary.chapter.chapterNumber shouldBe 5.5
        (result[2] as ReaderChapterListItem.Chapter).group.primary.chapter.chapterNumber shouldBe 4.0
    }

    @Test
    fun `chapters with non positive chapter numbers do not insert headers`() {
        val groups = listOf(
            ReaderChapterGroup(primary = createReaderChapter(id = 1L, chapterNumber = -1.0, name = "Special")),
            ReaderChapterGroup(primary = createReaderChapter(id = 2L, chapterNumber = 0.0, name = "Prologue")),
        )

        val result = insertReaderEncapsulatedGroupHeaders(groups)

        result.size shouldBe 2
        result.all { it is ReaderChapterListItem.Chapter } shouldBe true
    }
}
