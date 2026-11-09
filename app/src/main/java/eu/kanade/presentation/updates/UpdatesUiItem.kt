package eu.kanade.presentation.updates

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.relativeDateText
import eu.kanade.presentation.manga.components.ChapterDownloadAction
import eu.kanade.presentation.manga.components.ChapterDownloadIndicator
import eu.kanade.presentation.manga.components.DotSeparatorText
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.util.relativeTimeSpanString
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.ui.updates.UpdatesItem
import kotlinx.datetime.LocalDate
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.KeyboardArrowDown
import mihon.icons.materialsymbols.roundedfilled.Bookmark
import mihon.icons.materialsymbols.roundedfilled.Circle
import tachiyomi.domain.updates.model.UpdatesWithRelations
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ListGroupHeader
import tachiyomi.presentation.core.components.material.DISABLED_ALPHA
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.borderHairline
import tachiyomi.presentation.core.theme.surfaceInset
import tachiyomi.presentation.core.util.selectedBackground

internal fun LazyListScope.updatesLastUpdatedItem(
    lastUpdated: Long,
) {
    item(key = "updates-lastUpdated") {
        UpdatesLastUpdatedHeader(
            lastUpdated = lastUpdated,
            modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
        )
    }
}

@Composable
internal fun UpdatesLastUpdatedHeader(
    lastUpdated: Long,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(horizontal = MaterialTheme.padding.medium, vertical = MaterialTheme.padding.small),
    ) {
        Text(
            text = stringResource(MR.strings.updates_last_update_info, relativeTimeSpanString(lastUpdated)),
            fontStyle = FontStyle.Italic,
            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
        )
    }
}

internal fun LazyListScope.updatesUiItems(
    uiModels: List<UpdatesUiModel>,
    selectionMode: Boolean,
    onUpdateSelected: (UpdatesItem, Boolean, Boolean) -> Unit,
    onClickCover: (UpdatesItem) -> Unit,
    onClickUpdate: (UpdatesItem) -> Unit,
    onDownloadChapter: (List<UpdatesItem>, ChapterDownloadAction) -> Unit,
    collapsedMangaIds: Set<Long> = emptySet(),
    onToggleMangaCollapse: (Long) -> Unit = {},
) {
    val groups = buildList<Pair<LocalDate?, List<UpdatesItem>>> {
        var currentDate: LocalDate? = null
        val currentItems = mutableListOf<UpdatesItem>()
        for (uiModel in uiModels) {
            when (uiModel) {
                is UpdatesUiModel.Header -> {
                    if (currentItems.isNotEmpty()) {
                        add(currentDate to currentItems.toList())
                        currentItems.clear()
                    }
                    currentDate = uiModel.date
                }
                is UpdatesUiModel.Item -> {
                    currentItems.add(uiModel.item)
                }
            }
        }
        if (currentItems.isNotEmpty()) {
            add(currentDate to currentItems.toList())
        }
    }

    groups.forEach { (date, items) ->
        if (date != null) {
            item(
                key = "updatesHeader-$date",
                contentType = "header",
            ) {
                ListGroupHeader(
                    modifier = Modifier.animateItem(),
                    text = relativeDateText(date),
                )
            }
        }
        item(
            key = "updates-card-${date ?: items.firstOrNull()?.update?.chapterId ?: 0}",
            contentType = "card",
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .animateItem(),
            ) {
                Column {
                    val mangaGroups = groupUpdatesByManga(items)
                    mangaGroups.forEach { mangaGroup ->
                        if (mangaGroup.size == 1) {
                            val updatesItem = mangaGroup.first()
                            UpdatesUiItem(
                                update = updatesItem.update,
                                selected = updatesItem.selected,
                                readProgress = updatesItem.update.lastPageRead
                                    .takeIf { !updatesItem.update.read && it > 0L }
                                    ?.let {
                                        stringResource(
                                            MR.strings.chapter_progress,
                                            it + 1,
                                        )
                                    },
                                onLongClick = {
                                    onUpdateSelected(updatesItem, !updatesItem.selected, true)
                                },
                                onClick = {
                                    when {
                                        selectionMode -> onUpdateSelected(updatesItem, !updatesItem.selected, false)
                                        else -> onClickUpdate(updatesItem)
                                    }
                                },
                                onClickCover = { onClickCover(updatesItem) }.takeIf { !selectionMode },
                                onDownloadChapter = { action: ChapterDownloadAction ->
                                    onDownloadChapter(listOf(updatesItem), action)
                                }.takeIf { !selectionMode },
                                downloadStateProvider = updatesItem.downloadStateProvider,
                                downloadProgressProvider = updatesItem.downloadProgressProvider,
                            )
                        } else {
                            val firstItem = mangaGroup.first()
                            val mangaId = firstItem.update.mangaId
                            val isCollapsed = mangaId in collapsedMangaIds
                            UpdatesMangaGroupHeader(
                                coverData = firstItem.update.coverData,
                                title = firstItem.update.mangaTitle,
                                chapterCount = mangaGroup.size,
                                isCollapsed = isCollapsed,
                                onToggleCollapse = { onToggleMangaCollapse(mangaId) },
                                onClickCover = { onClickCover(firstItem) }.takeIf { !selectionMode },
                            )
                            if (!isCollapsed) {
                                mangaGroup.forEach { updatesItem ->
                                    UpdatesChapterUiItem(
                                        update = updatesItem.update,
                                        selected = updatesItem.selected,
                                        readProgress = updatesItem.update.lastPageRead
                                            .takeIf { !updatesItem.update.read && it > 0L }
                                            ?.let {
                                                stringResource(
                                                    MR.strings.chapter_progress,
                                                    it + 1,
                                                )
                                            },
                                        onLongClick = {
                                            onUpdateSelected(updatesItem, !updatesItem.selected, true)
                                        },
                                        onClick = {
                                            when {
                                                selectionMode -> onUpdateSelected(
                                                    updatesItem,
                                                    !updatesItem.selected,
                                                    false,
                                                )
                                                else -> onClickUpdate(updatesItem)
                                            }
                                        },
                                        onDownloadChapter = { action: ChapterDownloadAction ->
                                            onDownloadChapter(listOf(updatesItem), action)
                                        }.takeIf { !selectionMode },
                                        downloadStateProvider = updatesItem.downloadStateProvider,
                                        downloadProgressProvider = updatesItem.downloadProgressProvider,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun groupUpdatesByManga(items: List<UpdatesItem>): List<List<UpdatesItem>> {
    return buildList {
        var currentMangaId: Long? = null
        val currentMangaItems = mutableListOf<UpdatesItem>()
        for (item in items) {
            if (currentMangaId != null && item.update.mangaId != currentMangaId) {
                add(currentMangaItems.toList())
                currentMangaItems.clear()
            }
            currentMangaId = item.update.mangaId
            currentMangaItems.add(item)
        }
        if (currentMangaItems.isNotEmpty()) {
            add(currentMangaItems.toList())
        }
    }
}

@Composable
private fun UpdatesMangaGroupHeader(
    coverData: tachiyomi.domain.manga.model.MangaCover,
    title: String,
    chapterCount: Int,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onClickCover: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isCollapsed) -90f else 0f,
        label = "collapse_chevron_rotation",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleCollapse)
            .padding(horizontal = MaterialTheme.padding.medium, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MangaCover.Square(
            modifier = Modifier.size(40.dp),
            data = coverData,
            onClick = onClickCover,
        )

        Text(
            text = title,
            maxLines = 2,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = MaterialTheme.padding.medium),
        )

        Surface(
            shape = RoundedCornerShape(9999.dp),
            color = MaterialTheme.colorScheme.surfaceInset,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.borderHairline),
        ) {
            Text(
                text = pluralStringResource(MR.plurals.manga_num_chapters, count = chapterCount, chapterCount),
                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .rotate(rotation),
        )
    }
}

@Composable
private fun UpdatesMangaHeader(
    coverData: tachiyomi.domain.manga.model.MangaCover,
    title: String,
    chapterCount: Int,
    onClickCover: (() -> Unit)?,
    modifier: Modifier = Modifier,
) = UpdatesMangaGroupHeader(
    coverData = coverData,
    title = title,
    chapterCount = chapterCount,
    isCollapsed = false,
    onToggleCollapse = {},
    onClickCover = onClickCover,
    modifier = modifier,
)

@Composable
private fun UpdatesChapterUiItem(
    update: UpdatesWithRelations,
    selected: Boolean,
    readProgress: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDownloadChapter: ((ChapterDownloadAction) -> Unit)?,
    // Download Indicator
    downloadStateProvider: () -> Download.State,
    downloadProgressProvider: () -> Int,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val textAlpha = if (update.read) DISABLED_ALPHA else 1f

    Row(
        modifier = modifier
            .selectedBackground(selected)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    onLongClick()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            )
            .height(48.dp)
            .padding(start = 56.dp, end = MaterialTheme.padding.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var textHeight by remember { mutableIntStateOf(0) }
            if (!update.read) {
                Icon(
                    imageVector = MaterialSymbols.RoundedFilled.Circle,
                    contentDescription = stringResource(MR.strings.unread),
                    modifier = Modifier
                        .height(8.dp)
                        .padding(end = 4.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            if (update.bookmark) {
                Icon(
                    imageVector = MaterialSymbols.RoundedFilled.Bookmark,
                    contentDescription = stringResource(MR.strings.action_filter_bookmarked),
                    modifier = Modifier
                        .sizeIn(
                            maxHeight = with(LocalDensity.current) {
                                (textHeight.toDp() - 2.dp).coerceAtLeast(0.dp)
                            },
                        ),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text(
                text = update.chapterName,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                color = LocalContentColor.current.copy(alpha = textAlpha),
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { textHeight = it.size.height },
                modifier = Modifier
                    .weight(weight = 1f, fill = false),
            )
            if (readProgress != null) {
                DotSeparatorText()
                Text(
                    text = readProgress,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                    color = LocalContentColor.current.copy(alpha = DISABLED_ALPHA),
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        ChapterDownloadIndicator(
            enabled = onDownloadChapter != null,
            modifier = Modifier.padding(start = 4.dp),
            downloadStateProvider = downloadStateProvider,
            downloadProgressProvider = downloadProgressProvider,
            onClick = { onDownloadChapter?.invoke(it) },
        )
    }
}

@Composable
private fun UpdatesUiItem(
    update: UpdatesWithRelations,
    selected: Boolean,
    readProgress: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickCover: (() -> Unit)?,
    onDownloadChapter: ((ChapterDownloadAction) -> Unit)?,
    // Download Indicator
    downloadStateProvider: () -> Download.State,
    downloadProgressProvider: () -> Int,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val textAlpha = if (update.read) DISABLED_ALPHA else 1f

    Row(
        modifier = modifier
            .selectedBackground(selected)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    onLongClick()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            )
            .height(56.dp)
            .padding(horizontal = MaterialTheme.padding.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MangaCover.Square(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .fillMaxHeight(),
            data = update.coverData,
            onClick = onClickCover,
        )

        Column(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.padding.medium)
                .weight(1f),
        ) {
            Text(
                text = update.mangaTitle,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalContentColor.current.copy(alpha = textAlpha),
                overflow = TextOverflow.Ellipsis,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                var textHeight by remember { mutableIntStateOf(0) }
                if (!update.read) {
                    Icon(
                        imageVector = MaterialSymbols.RoundedFilled.Circle,
                        contentDescription = stringResource(MR.strings.unread),
                        modifier = Modifier
                            .height(8.dp)
                            .padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                if (update.bookmark) {
                    Icon(
                        imageVector = MaterialSymbols.RoundedFilled.Bookmark,
                        contentDescription = stringResource(MR.strings.action_filter_bookmarked),
                        modifier = Modifier
                            .sizeIn(
                                maxHeight = with(LocalDensity.current) {
                                    (textHeight.toDp() - 2.dp).coerceAtLeast(0.dp)
                                },
                            ),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                Text(
                    text = update.chapterName,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                    color = LocalContentColor.current.copy(alpha = textAlpha),
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { textHeight = it.size.height },
                    modifier = Modifier
                        .weight(weight = 1f, fill = false),
                )
                if (readProgress != null) {
                    DotSeparatorText()
                    Text(
                        text = readProgress,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        color = LocalContentColor.current.copy(alpha = DISABLED_ALPHA),
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        ChapterDownloadIndicator(
            enabled = onDownloadChapter != null,
            modifier = Modifier.padding(start = 4.dp),
            downloadStateProvider = downloadStateProvider,
            downloadProgressProvider = downloadProgressProvider,
            onClick = { onDownloadChapter?.invoke(it) },
        )
    }
}
