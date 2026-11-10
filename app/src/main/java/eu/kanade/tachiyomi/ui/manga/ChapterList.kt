package eu.kanade.tachiyomi.ui.manga

import androidx.compose.runtime.Immutable
import eu.kanade.tachiyomi.data.download.model.Download
import tachiyomi.domain.chapter.model.Chapter

@Immutable
sealed class ChapterList {
    @Immutable
    data class MissingCount(
        val id: String,
        val count: Int,
    ) : ChapterList()

    @Immutable
    data class Item(
        val chapter: Chapter,
        val downloadState: Download.State,
        val selected: Boolean = false,
        val versions: List<ChapterList.Item> = emptyList(),
    ) : ChapterList() {
        val id = chapter.id
        val isDownloaded = downloadState == Download.State.DOWNLOADED
    }
}

fun List<ChapterList.Item>.groupByDuplicateChapterNumber(): List<ChapterList.Item> {
    val duplicatesByNumber = filter { it.chapter.chapterNumber > 0.0 }
        .groupBy { it.chapter.chapterNumber }
        .filterValues { it.size > 1 }

    if (duplicatesByNumber.isEmpty()) return this

    val handledNumbers = HashSet<Double>()
    val result = ArrayList<ChapterList.Item>(size)

    for (item in this) {
        val number = item.chapter.chapterNumber
        if (number <= 0.0 || number !in duplicatesByNumber) {
            result.add(item)
            continue
        }

        if (handledNumbers.add(number)) {
            val group = duplicatesByNumber.getValue(number)
            val primary = group.maxByOrNull { it.preferenceScore() } ?: item
            val orderedVersions = listOf(primary) + group.filter { it.id != primary.id }
            result.add(primary.copy(versions = orderedVersions))
        }
    }

    return result
}

private fun ChapterList.Item.preferenceScore(): Int {
    var score = 0
    if (chapter.read) {
        score += 4
    } else if (chapter.lastPageRead > 0) {
        score += 2
    }
    if (isDownloaded) {
        score += 1
    }
    return score
}

fun List<ChapterList.Item>.groupSubChaptersWithParent(sortDescending: Boolean): List<ChapterList.Item> {
    if (!sortDescending || isEmpty()) return this

    val result = ArrayList<ChapterList.Item>(size)
    var i = 0
    while (i < size) {
        val current = this[i]
        val num = current.chapter.chapterNumber
        if (num > 0.0 && num % 1.0 != 0.0) {
            val baseNum = kotlin.math.floor(num)
            val group = mutableListOf<ChapterList.Item>()
            var j = i
            while (j < size) {
                val item = this[j]
                val itemNum = item.chapter.chapterNumber
                if (itemNum > 0.0 && kotlin.math.floor(itemNum) == baseNum) {
                    group.add(item)
                    j++
                } else {
                    break
                }
            }
            val parentItem = group.firstOrNull { it.chapter.chapterNumber == baseNum }
            val subChapters = group.filter { it.chapter.chapterNumber != baseNum }
                .sortedBy { it.chapter.chapterNumber }

            if (parentItem != null) {
                result.add(parentItem)
            }
            result.addAll(subChapters)
            i = j
        } else {
            result.add(current)
            i++
        }
    }
    return result
}
