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
    data class GroupHeader(
        val id: String,
        val baseNumber: Double,
        val label: String,
        val partCount: Int,
        val isSplit: Boolean,
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
    if (isEmpty()) return this

    val result = ArrayList<ChapterList.Item>(size)
    var i = 0
    while (i < size) {
        val current = this[i]
        val num = current.chapter.chapterNumber
        if (num > 0.0) {
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
            val parentItems = group.filter { it.chapter.chapterNumber == baseNum }
            val subChapters = group.filter { it.chapter.chapterNumber != baseNum }.let { subs ->
                if (sortDescending) {
                    subs.sortedByDescending { it.chapter.chapterNumber }
                } else {
                    subs.sortedBy { it.chapter.chapterNumber }
                }
            }

            result.addAll(parentItems)
            result.addAll(subChapters)
            i = j
        } else {
            result.add(current)
            i++
        }
    }
    return result
}

@JvmName("insertEncapsulatedGroupHeadersForChapterList")
fun List<ChapterList>.insertEncapsulatedGroupHeaders(sortDescending: Boolean): List<ChapterList> {
    if (isEmpty()) return emptyList()

    val result = ArrayList<ChapterList>(size * 2)
    var i = 0
    while (i < size) {
        val current = this[i]
        if (current is ChapterList.Item && current.chapter.chapterNumber > 0.0) {
            val baseNum = kotlin.math.floor(current.chapter.chapterNumber)
            val group = mutableListOf<ChapterList.Item>()
            var j = i
            while (j < size) {
                val item = this[j]
                if (item is ChapterList.Item && item.chapter.chapterNumber > 0.0 &&
                    kotlin.math.floor(item.chapter.chapterNumber) == baseNum
                ) {
                    group.add(item)
                    j++
                } else {
                    break
                }
            }
            val isSplit = group.any { it.chapter.chapterNumber != baseNum }
            result.add(
                ChapterList.GroupHeader(
                    id = "group-${baseNum.toInt()}",
                    baseNumber = baseNum,
                    label = "Chapter ${baseNum.toInt()}",
                    partCount = group.size,
                    isSplit = isSplit,
                ),
            )
            result.addAll(group)
            i = j
        } else {
            result.add(current)
            i++
        }
    }
    return result
}

fun List<ChapterList.Item>.insertEncapsulatedGroupHeaders(sortDescending: Boolean): List<ChapterList> {
    if (isEmpty()) return emptyList()

    val ordered = groupSubChaptersWithParent(sortDescending)
    return (ordered as List<ChapterList>).insertEncapsulatedGroupHeaders(sortDescending)
}
