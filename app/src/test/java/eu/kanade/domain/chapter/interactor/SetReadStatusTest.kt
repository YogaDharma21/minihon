package eu.kanade.domain.chapter.interactor

import eu.kanade.domain.download.interactor.DeleteDownload
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tachiyomi.core.common.preference.InMemoryPreferenceStore
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.model.ChapterUpdate
import tachiyomi.domain.chapter.repository.ChapterRepository
import tachiyomi.domain.download.service.DownloadPreferences
import tachiyomi.domain.manga.repository.MangaRepository

class SetReadStatusTest {

    private lateinit var downloadPreferences: DownloadPreferences
    private lateinit var deleteDownload: DeleteDownload
    private lateinit var mangaRepository: MangaRepository
    private lateinit var chapterRepository: ChapterRepository
    private lateinit var setReadStatus: SetReadStatus

    @BeforeEach
    fun beforeEach() {
        downloadPreferences = DownloadPreferences(InMemoryPreferenceStore())
        deleteDownload = mockk(relaxed = true)
        mangaRepository = mockk(relaxed = true)
        chapterRepository = mockk(relaxed = true)

        setReadStatus = SetReadStatus(
            downloadPreferences = downloadPreferences,
            deleteDownload = deleteDownload,
            mangaRepository = mangaRepository,
            chapterRepository = chapterRepository,
        )
    }

    private fun createChapter(
        id: Long,
        mangaId: Long = 1L,
        chapterNumber: Double = 1.0,
        read: Boolean = false,
        lastPageRead: Int = 0,
        name: String = "Chapter $chapterNumber",
        scanlator: String? = null,
    ): Chapter {
        return Chapter.create().copy(
            id = id,
            mangaId = mangaId,
            chapterNumber = chapterNumber,
            read = read,
            lastPageRead = lastPageRead,
            name = name,
            scanlator = scanlator,
        )
    }

    @Test
    fun `marking chapter as read updates all duplicate versions of that chapterNumber`() = runTest {
        val ch188v1 = createChapter(id = 1L, chapterNumber = 188.0, scanlator = "Group A", read = false)
        val ch188v2 = createChapter(id = 2L, chapterNumber = 188.0, scanlator = "Group B", read = false)
        val ch188v3 = createChapter(id = 3L, chapterNumber = 188.0, scanlator = "Group C", read = false)
        val ch188v4 = createChapter(id = 4L, chapterNumber = 188.0, scanlator = "Group D", read = false)
        val otherCh = createChapter(id = 5L, chapterNumber = 189.0, scanlator = "Group A", read = false)

        coEvery { chapterRepository.getChapterByMangaId(1L) } returns listOf(
            ch188v1, ch188v2, ch188v3, ch188v4, otherCh,
        )

        val updatesSlot = slot<List<ChapterUpdate>>()
        coEvery { chapterRepository.updateAll(capture(updatesSlot)) } returns Unit

        val result = setReadStatus.await(read = true, ch188v1)

        result shouldBe SetReadStatus.Result.Success
        coVerify(exactly = 1) { chapterRepository.getChapterByMangaId(1L) }
        coVerify(exactly = 1) { chapterRepository.updateAll(any()) }

        val updatedIds = updatesSlot.captured.map { it.id }
        updatedIds shouldContainExactlyInAnyOrder listOf(1L, 2L, 3L, 4L)
        updatesSlot.captured.forEach {
            it.read shouldBe true
        }
    }

    @Test
    fun `marking chapter as unread updates all duplicate versions of that chapterNumber`() = runTest {
        val ch188v1 = createChapter(id = 1L, chapterNumber = 188.0, scanlator = "Group A", read = true, lastPageRead = 15)
        val ch188v2 = createChapter(id = 2L, chapterNumber = 188.0, scanlator = "Group B", read = true, lastPageRead = 10)
        val ch188v3 = createChapter(id = 3L, chapterNumber = 188.0, scanlator = "Group C", read = true, lastPageRead = 0)

        coEvery { chapterRepository.getChapterByMangaId(1L) } returns listOf(
            ch188v1, ch188v2, ch188v3,
        )

        val updatesSlot = slot<List<ChapterUpdate>>()
        coEvery { chapterRepository.updateAll(capture(updatesSlot)) } returns Unit

        val result = setReadStatus.await(read = false, ch188v1)

        result shouldBe SetReadStatus.Result.Success
        val updatedIds = updatesSlot.captured.map { it.id }
        updatedIds shouldContainExactlyInAnyOrder listOf(1L, 2L, 3L)
        updatesSlot.captured.forEach {
            it.read shouldBe false
            it.lastPageRead shouldBe 0
        }
    }

    @Test
    fun `marking chapter with non-positive chapterNumber does not query or update siblings`() = runTest {
        val extra1 = createChapter(id = 10L, chapterNumber = -1.0, read = false)

        val updatesSlot = slot<List<ChapterUpdate>>()
        coEvery { chapterRepository.updateAll(capture(updatesSlot)) } returns Unit

        val result = setReadStatus.await(read = true, extra1)

        result shouldBe SetReadStatus.Result.Success
        coVerify(exactly = 0) { chapterRepository.getChapterByMangaId(any()) }
        val updatedIds = updatesSlot.captured.map { it.id }
        updatedIds shouldContainExactlyInAnyOrder listOf(10L)
    }

    @Test
    fun `returns NoChapters if all target chapters and duplicates are already read`() = runTest {
        val ch188v1 = createChapter(id = 1L, chapterNumber = 188.0, read = true)
        val ch188v2 = createChapter(id = 2L, chapterNumber = 188.0, read = true)

        coEvery { chapterRepository.getChapterByMangaId(1L) } returns listOf(ch188v1, ch188v2)

        val result = setReadStatus.await(read = true, ch188v1)

        result shouldBe SetReadStatus.Result.NoChapters
        coVerify(exactly = 0) { chapterRepository.updateAll(any()) }
    }
}
