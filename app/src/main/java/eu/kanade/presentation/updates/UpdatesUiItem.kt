package eu.kanade.presentation.updates

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import eu.kanade.presentation.components.relativeDateText
import eu.kanade.presentation.manga.components.ChapterDownloadAction
import eu.kanade.presentation.manga.components.ChapterDownloadIndicator
import eu.kanade.presentation.manga.components.DotSeparatorText
import eu.kanade.presentation.util.cascadeItemMotion
import eu.kanade.presentation.util.relativeTimeSpanString
import eu.kanade.presentation.util.rememberResourceBitmapPainter
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.ui.updates.UpdatesItem
import kotlinx.datetime.LocalDate
import mihon.app.di.appGraph
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

internal val UpdatesTrayContainerColor = Color(0xFF0D0E11)

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

    var itemIndex = 0
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
                                modifier = Modifier.cascadeItemMotion(index = itemIndex++),
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
                            val hasUnread = mangaGroup.any { !it.update.read }
                            val allSelected = mangaGroup.all { it.selected }
                            UpdatesMangaGroupHeader(
                                modifier = Modifier.cascadeItemMotion(index = itemIndex++),
                                update = firstItem.update,
                                chapterCount = mangaGroup.size,
                                isCollapsed = isCollapsed,
                                hasUnread = hasUnread,
                                selected = allSelected,
                                onToggleCollapse = { onToggleMangaCollapse(mangaId) },
                                onClickCover = { onClickCover(firstItem) }.takeIf { !selectionMode },
                            )
                            if (!isCollapsed) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = UpdatesTrayContainerColor,
                                    border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                                ) {
                                    Column {
                                        mangaGroup.forEachIndexed { index, updatesItem ->
                                            UpdatesChapterUiItem(
                                                update = updatesItem.update,
                                                isLast = index == mangaGroup.lastIndex,
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
                                            if (index < mangaGroup.lastIndex) {
                                                HorizontalDivider(
                                                    color = Color(0x0AFFFFFF),
                                                    thickness = 0.5.dp,
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
    update: UpdatesWithRelations,
    chapterCount: Int,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onClickCover: (() -> Unit)?,
    modifier: Modifier = Modifier,
    hasUnread: Boolean = false,
    selected: Boolean = false,
) {
    val context = LocalContext.current
    val textAlpha = if (!hasUnread) DISABLED_ALPHA else 1f

    val relativeTime = relativeTimeSpanString(update.dateFetch.toEpochMilliseconds())
    val sourceName by produceState(initialValue = "", update.sourceId) {
        value = runCatching { context.appGraph.sourceManager.getOrStub(update.sourceId).name }.getOrDefault("")
    }

    val rotation by animateFloatAsState(
        targetValue = if (isCollapsed) -90f else 0f,
        label = "collapse_chevron_rotation",
    )

    Row(
        modifier = modifier
            .selectedBackground(selected)
            .clickable(
                role = Role.Button,
                onClick = onToggleCollapse,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UpdatesShowcaseCover(
            coverData = update.coverData,
            onClick = onClickCover,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = update.mangaTitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color(0xFFF4F4F2).copy(alpha = textAlpha),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                ),
            )

            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = MaterialTheme.colorScheme.surfaceInset,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.borderHairline),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (hasUnread) {
                        Icon(
                            imageVector = MaterialSymbols.RoundedFilled.Circle,
                            contentDescription = stringResource(MR.strings.unread),
                            modifier = Modifier.size(6.dp),
                            tint = Color(0xFFA9BFA9),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = pluralStringResource(MR.plurals.manga_num_chapters, count = chapterCount, chapterCount),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFeatureSettings = "tnum",
                            fontWeight = FontWeight.Medium,
                        ),
                        color = Color(0xFFF4F4F2).copy(alpha = textAlpha),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (sourceName.isNotBlank()) {
                    Text(
                        text = sourceName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8A8C92).copy(alpha = textAlpha),
                    )
                    DotSeparatorText()
                }
                Text(
                    text = relativeTime,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFeatureSettings = "tnum",
                    ),
                    color = Color(0xFF8A8C92).copy(alpha = textAlpha),
                )
            }
        }

        Icon(
            imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(end = 4.dp)
                .size(24.dp)
                .rotate(rotation),
        )
    }
}

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
    isLast: Boolean = true,
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
            .height(40.dp)
            .padding(horizontal = 12.dp),
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
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val textAlpha = if (update.read) DISABLED_ALPHA else 1f

    val relativeTime = relativeTimeSpanString(update.dateFetch.toEpochMilliseconds())
    val sourceName by produceState(initialValue = "", update.sourceId) {
        value = runCatching { context.appGraph.sourceManager.getOrStub(update.sourceId).name }.getOrDefault("")
    }

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
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UpdatesShowcaseCover(
            coverData = update.coverData,
            onClick = onClickCover,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = update.mangaTitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color(0xFFF4F4F2).copy(alpha = textAlpha),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                ),
            )

            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = MaterialTheme.colorScheme.surfaceInset,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.borderHairline),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!update.read) {
                        Icon(
                            imageVector = MaterialSymbols.RoundedFilled.Circle,
                            contentDescription = stringResource(MR.strings.unread),
                            modifier = Modifier.size(6.dp),
                            tint = Color(0xFFA9BFA9),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (update.bookmark) {
                        Icon(
                            imageVector = MaterialSymbols.RoundedFilled.Bookmark,
                            contentDescription = stringResource(MR.strings.action_filter_bookmarked),
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFFD2BD93),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    Text(
                        text = update.chapterName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFeatureSettings = "tnum",
                            fontWeight = FontWeight.Medium,
                        ),
                        color = Color(0xFFF4F4F2).copy(alpha = textAlpha),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (sourceName.isNotBlank()) {
                    Text(
                        text = sourceName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8A8C92).copy(alpha = textAlpha),
                    )
                    DotSeparatorText()
                }
                Text(
                    text = relativeTime,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFeatureSettings = "tnum",
                    ),
                    color = Color(0xFF8A8C92).copy(alpha = textAlpha),
                )
                if (readProgress != null) {
                    DotSeparatorText()
                    Text(
                        text = readProgress,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFeatureSettings = "tnum",
                        ),
                        color = Color(0xFF8A8C92).copy(alpha = textAlpha),
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

@Composable
private fun UpdatesShowcaseCover(
    coverData: tachiyomi.domain.manga.model.MangaCover,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = 84.dp, height = 116.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(10.dp),
                clip = false,
            )
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        AsyncImage(
            model = coverData,
            placeholder = ColorPainter(Color(0x1F888888)),
            error = rememberResourceBitmapPainter(id = R.drawable.cover_error),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Book crease along spine
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x40000000),
                            Color(0x14000000),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 5.dp)
                .width(1.dp)
                .background(Color(0x1FFFFFFF)),
        )
    }
}
