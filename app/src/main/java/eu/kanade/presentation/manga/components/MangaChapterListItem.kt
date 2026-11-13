package eu.kanade.presentation.manga.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.relativeDateText
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.ui.manga.ChapterList
import me.saket.swipe.SwipeableActionsBox
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.BookmarkAdd
import mihon.icons.materialsymbols.rounded.BookmarkRemove
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.rounded.Delete
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.FileDownloadOff
import mihon.icons.materialsymbols.rounded.KeyboardArrowDown
import mihon.icons.materialsymbols.rounded.RemoveDone
import mihon.icons.materialsymbols.roundedfilled.Bookmark
import mihon.icons.materialsymbols.roundedfilled.Circle
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.DISABLED_ALPHA
import tachiyomi.presentation.core.components.material.SECONDARY_ALPHA
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.borderHairline
import tachiyomi.presentation.core.theme.surfaceInset
import tachiyomi.presentation.core.util.selectedBackground

@Composable
fun MangaChapterListItem(
    title: String,
    date: String?,
    readProgress: String?,
    scanlator: String?,
    read: Boolean,
    bookmark: Boolean,
    selected: Boolean,
    downloadIndicatorEnabled: Boolean,
    downloadStateProvider: () -> Download.State,
    downloadProgressProvider: () -> Int,
    chapterSwipeStartAction: LibraryPreferences.ChapterSwipeAction,
    chapterSwipeEndAction: LibraryPreferences.ChapterSwipeAction,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onDownloadClick: ((ChapterDownloadAction) -> Unit)?,
    onChapterSwipe: (LibraryPreferences.ChapterSwipeAction) -> Unit,
    modifier: Modifier = Modifier,
    versions: List<ChapterList.Item> = emptyList(),
    isExpanded: Boolean = false,
    onToggleExpand: (() -> Unit)? = null,
    onVersionClick: ((ChapterList.Item) -> Unit)? = null,
    onVersionLongClick: ((ChapterList.Item) -> Unit)? = null,
    onVersionDownloadClick: ((ChapterList.Item, ChapterDownloadAction) -> Unit)? = null,
    versionDownloadProgressProvider: ((Long) -> Int)? = null,
    isSubChapter: Boolean = false,
    isFirstSubChapter: Boolean = false,
    isLastSubChapter: Boolean = true,
    hasParentAbove: Boolean = false,
    hasSubChaptersBelow: Boolean = false,
) {
    val start = getSwipeAction(
        action = chapterSwipeStartAction,
        read = read,
        bookmark = bookmark,
        downloadState = downloadStateProvider(),
        background = MaterialTheme.colorScheme.primaryContainer,
        onSwipe = { onChapterSwipe(chapterSwipeStartAction) },
    )
    val end = getSwipeAction(
        action = chapterSwipeEndAction,
        read = read,
        bookmark = bookmark,
        downloadState = downloadStateProvider(),
        background = MaterialTheme.colorScheme.primaryContainer,
        onSwipe = { onChapterSwipe(chapterSwipeEndAction) },
    )

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        SwipeableActionsBox(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds(),
            startActions = listOfNotNull(start),
            endActions = listOfNotNull(end),
            swipeThreshold = swipeActionThreshold,
            backgroundUntilSwipeThreshold = MaterialTheme.colorScheme.surfaceContainerLowest,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectedBackground(selected)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                    )
                    .padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            ) {
                if (isSubChapter) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = if (read) DISABLED_ALPHA else 0.7f,
                                    ),
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        var textHeight by remember { mutableIntStateOf(0) }
                        if (!read) {
                            Icon(
                                imageVector = MaterialSymbols.RoundedFilled.Circle,
                                contentDescription = stringResource(MR.strings.unread),
                                modifier = Modifier
                                    .height(8.dp)
                                    .padding(end = 4.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        if (bookmark) {
                            Icon(
                                imageVector = MaterialSymbols.RoundedFilled.Bookmark,
                                contentDescription = stringResource(MR.strings.action_filter_bookmarked),
                                modifier = Modifier
                                    .sizeIn(maxHeight = with(LocalDensity.current) { textHeight.toDp() - 2.dp }),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { textHeight = it.size.height },
                            color = LocalContentColor.current.copy(
                                alpha = if (read) {
                                    DISABLED_ALPHA
                                } else if (isSubChapter) {
                                    0.85f
                                } else {
                                    1f
                                },
                            ),
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val subtitleStyle = MaterialTheme.typography.bodySmall
                            .merge(
                                color = LocalContentColor.current
                                    .copy(alpha = if (read) DISABLED_ALPHA else SECONDARY_ALPHA),
                            )
                            .copy(fontFeatureSettings = "tnum")
                        ProvideTextStyle(value = subtitleStyle) {
                            if (date != null) {
                                Text(
                                    text = date,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (readProgress != null || scanlator != null) DotSeparatorText()
                            }
                            if (readProgress != null) {
                                Text(
                                    text = readProgress,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = LocalContentColor.current.copy(alpha = DISABLED_ALPHA),
                                )
                                if (scanlator != null) DotSeparatorText()
                            }
                            if (scanlator != null) {
                                Text(
                                    text = scanlator,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                            }
                        }
                        if (versions.size > 1 && onToggleExpand != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            ChapterVersionChip(
                                count = versions.size,
                                isExpanded = isExpanded,
                                onClick = onToggleExpand,
                            )
                        }
                    }
                }

                ChapterDownloadIndicator(
                    enabled = downloadIndicatorEnabled,
                    modifier = Modifier.padding(start = 4.dp),
                    downloadStateProvider = downloadStateProvider,
                    downloadProgressProvider = downloadProgressProvider,
                    onClick = { onDownloadClick?.invoke(it) },
                )
            }
        }

        if (isExpanded && versions.size > 1) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceInset,
                border = BorderStroke(1.dp, Color(0x12FFFFFF)),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    versions.forEachIndexed { index, version ->
                        MangaChapterVersionItem(
                            version = version,
                            isLast = index == versions.lastIndex,
                            downloadIndicatorEnabled = downloadIndicatorEnabled,
                            downloadProgressProvider = { versionDownloadProgressProvider?.invoke(version.id) ?: 0 },
                            onClick = { onVersionClick?.invoke(version) },
                            onLongClick = { onVersionLongClick?.invoke(version) },
                            onDownloadClick = onVersionDownloadClick?.let { callback ->
                                { action -> callback(version, action) }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterVersionChip(
    count: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "version_chip_rotation",
    )
    Surface(
        shape = RoundedCornerShape(9999.dp),
        color = MaterialTheme.colorScheme.surfaceInset,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.borderHairline),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "$count versions",
                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(14.dp)
                    .rotate(rotation),
            )
        }
    }
}

@Composable
private fun MangaChapterVersionItem(
    version: ChapterList.Item,
    downloadIndicatorEnabled: Boolean,
    downloadProgressProvider: () -> Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDownloadClick: ((ChapterDownloadAction) -> Unit)?,
    modifier: Modifier = Modifier,
    isLast: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectedBackground(version.selected)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!version.chapter.read) {
                    Icon(
                        imageVector = MaterialSymbols.RoundedFilled.Circle,
                        contentDescription = stringResource(MR.strings.unread),
                        modifier = Modifier
                            .height(6.dp)
                            .padding(end = 2.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                if (version.chapter.bookmark) {
                    Icon(
                        imageVector = MaterialSymbols.RoundedFilled.Bookmark,
                        contentDescription = stringResource(MR.strings.action_filter_bookmarked),
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                val scanlatorText = version.chapter.scanlator.takeIf { !it.isNullOrBlank() }
                    ?: stringResource(MR.strings.unknown)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color(0x14FFFFFF)),
                ) {
                    Text(
                        text = scanlatorText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = LocalContentColor.current.copy(
                                alpha = if (version.chapter.read) DISABLED_ALPHA else 1f,
                            ),
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            val date = relativeDateText(version.chapter.dateUpload)
            val readProgress = version.chapter.lastPageRead
                .takeIf { !version.chapter.read && it > 0L }
                ?.let { stringResource(MR.strings.chapter_progress, it + 1) }

            if (date.isNotBlank() || readProgress != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val subtitleStyle = MaterialTheme.typography.labelSmall
                        .merge(
                            color = LocalContentColor.current.copy(
                                alpha = if (version.chapter.read) DISABLED_ALPHA else SECONDARY_ALPHA,
                            ),
                        )
                        .copy(fontFeatureSettings = "tnum")
                    ProvideTextStyle(value = subtitleStyle) {
                        if (date.isNotBlank()) {
                            Text(
                                text = date,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (readProgress != null) DotSeparatorText()
                        }
                        if (readProgress != null) {
                            Text(
                                text = readProgress,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = LocalContentColor.current.copy(alpha = DISABLED_ALPHA),
                            )
                        }
                    }
                }
            }
        }

        ChapterDownloadIndicator(
            enabled = downloadIndicatorEnabled,
            modifier = Modifier.padding(start = 4.dp),
            downloadStateProvider = { version.downloadState },
            downloadProgressProvider = downloadProgressProvider,
            onClick = { onDownloadClick?.invoke(it) },
        )
    }
}

private fun getSwipeAction(
    action: LibraryPreferences.ChapterSwipeAction,
    read: Boolean,
    bookmark: Boolean,
    downloadState: Download.State,
    background: Color,
    onSwipe: () -> Unit,
): me.saket.swipe.SwipeAction? {
    return when (action) {
        LibraryPreferences.ChapterSwipeAction.ToggleRead -> swipeAction(
            icon = if (!read) MaterialSymbols.Rounded.Check else MaterialSymbols.Rounded.RemoveDone,
            background = background,
            isUndo = read,
            onSwipe = onSwipe,
        )
        LibraryPreferences.ChapterSwipeAction.ToggleBookmark -> swipeAction(
            icon = if (!bookmark) MaterialSymbols.Rounded.BookmarkAdd else MaterialSymbols.Rounded.BookmarkRemove,
            background = background,
            isUndo = bookmark,
            onSwipe = onSwipe,
        )
        LibraryPreferences.ChapterSwipeAction.Download -> swipeAction(
            icon = when (downloadState) {
                Download.State.NOT_DOWNLOADED, Download.State.ERROR -> MaterialSymbols.Rounded.Download
                Download.State.QUEUE, Download.State.DOWNLOADING -> MaterialSymbols.Rounded.FileDownloadOff
                Download.State.DOWNLOADED -> MaterialSymbols.Rounded.Delete
            },
            background = background,
            onSwipe = onSwipe,
        )
        LibraryPreferences.ChapterSwipeAction.Disabled -> null
    }
}

private fun swipeAction(
    onSwipe: () -> Unit,
    icon: ImageVector,
    background: Color,
    isUndo: Boolean = false,
): me.saket.swipe.SwipeAction {
    return me.saket.swipe.SwipeAction(
        icon = {
            Icon(
                modifier = Modifier.padding(16.dp),
                imageVector = icon,
                tint = contentColorFor(background),
                contentDescription = null,
            )
        },
        background = background,
        onSwipe = onSwipe,
        isUndo = isUndo,
    )
}

private val swipeActionThreshold = 56.dp

val SUB_CHAPTER_NODE_UNREAD_SIZE: Dp = 6.5.dp
val SUB_CHAPTER_NODE_READ_SIZE: Dp = 6.dp
val SUB_CHAPTER_NODE_READ_BORDER_WIDTH: Dp = 1.5.dp

fun shouldShowInnerUnreadDot(read: Boolean, isSubChapter: Boolean): Boolean = !read && !isSubChapter

data class SubChapterNodeProperties(
    val size: Dp,
    val isSolidFill: Boolean,
    val isHollowOutline: Boolean = !isSolidFill,
    val borderWidth: Dp = if (isHollowOutline) SUB_CHAPTER_NODE_READ_BORDER_WIDTH else 0.dp,
)

fun getSubChapterNodeProperties(read: Boolean): SubChapterNodeProperties {
    return if (read) {
        SubChapterNodeProperties(
            size = SUB_CHAPTER_NODE_READ_SIZE,
            isSolidFill = false,
            isHollowOutline = true,
            borderWidth = SUB_CHAPTER_NODE_READ_BORDER_WIDTH,
        )
    } else {
        SubChapterNodeProperties(
            size = SUB_CHAPTER_NODE_UNREAD_SIZE,
            isSolidFill = true,
            isHollowOutline = false,
            borderWidth = 0.dp,
        )
    }
}

object SubChapterNode {
    val UNREAD_SIZE: Dp = SUB_CHAPTER_NODE_UNREAD_SIZE
    val READ_SIZE: Dp = SUB_CHAPTER_NODE_READ_SIZE
    val READ_BORDER_WIDTH: Dp = SUB_CHAPTER_NODE_READ_BORDER_WIDTH

    fun properties(read: Boolean): SubChapterNodeProperties = getSubChapterNodeProperties(read)
}

