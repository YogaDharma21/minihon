package eu.kanade.presentation.reader

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.components.AdaptiveSheet
import eu.kanade.tachiyomi.ui.reader.model.ReaderChapter
import eu.kanade.tachiyomi.ui.reader.model.ReaderPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ArrowBack
import mihon.icons.materialsymbols.automirroredrounded.ArrowForward
import mihon.icons.materialsymbols.automirroredrounded.FormatListBulleted
import mihon.icons.materialsymbols.rounded.Bookmark
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.rounded.Close
import mihon.icons.materialsymbols.rounded.KeyboardArrowDown
import mihon.icons.materialsymbols.rounded.Search
import mihon.icons.materialsymbols.roundedfilled.Bookmark
import mihon.icons.materialsymbols.roundedfilled.ViewModule
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.borderHairline
import tachiyomi.presentation.core.theme.surfaceInset

import androidx.compose.ui.window.Dialog

data class ReaderChapterGroup(
    val primary: ReaderChapter,
    val versions: List<ReaderChapter> = emptyList(),
    val isSubChapter: Boolean = false,
    val isFirstSubChapter: Boolean = false,
    val isLastSubChapter: Boolean = false,
    val hasParentAbove: Boolean = false,
    val hasSubChaptersBelow: Boolean = false,
) {
    val id: Long = primary.chapter.id
}

@Composable
fun ReaderChapterListSheet(
    chapters: List<ReaderChapter>,
    currentChapterId: Long?,
    pages: List<ReaderPage> = emptyList(),
    currentPageIndex: Int = 0,
    onDismissRequest: () -> Unit,
    onChapterClicked: (ReaderChapter) -> Unit,
    onPageClicked: (Int) -> Unit = {},
    onToggleBookmark: (ReaderChapter) -> Unit = {},
    onSelectChapterForPreview: ((ReaderChapter) -> Unit)? = null,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedGroupIds by remember { mutableStateOf(setOf<Long>()) }

    val filteredChapters = remember(chapters, searchQuery) {
        if (searchQuery.isBlank()) {
            chapters
        } else {
            chapters.filter {
                it.chapter.name.contains(searchQuery, ignoreCase = true) ||
                    it.chapter.scanlator?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    val groupedChapters = remember(filteredChapters) {
        groupReaderChapters(filteredChapters)
    }

    val bookmarkedChapters = remember(chapters) {
        chapters.filter { it.chapter.bookmark }
    }

    val initialIndex = remember {
        groupedChapters.indexOfFirst { group ->
            group.primary.chapter.id == currentChapterId ||
                group.versions.any { it.chapter.id == currentChapterId }
        }.coerceAtLeast(0)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    AdaptiveSheet(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color(0xFF121316),
            border = BorderStroke(1.dp, Color(0x12FFFFFF)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF)),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Header with segmented pill tabs and close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Segmented Tab Selector
                    Surface(
                        shape = CircleShape,
                        color = Color(0x14FFFFFF),
                        border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                    ) {
                        Row(
                            modifier = Modifier.padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Tab 0: Chapters
                            SegmentedTabButton(
                                selected = selectedTab == 0,
                                icon = MaterialSymbols.AutoMirroredRounded.FormatListBulleted,
                                contentDescription = stringResource(MR.strings.chapters),
                                onClick = { selectedTab = 0 },
                            )

                            // Tab 1: Pages Grid
                            SegmentedTabButton(
                                selected = selectedTab == 1,
                                icon = MaterialSymbols.RoundedFilled.ViewModule,
                                contentDescription = "Pages",
                                onClick = { selectedTab = 1 },
                            )

                            // Tab 2: Bookmarks
                            SegmentedTabButton(
                                selected = selectedTab == 2,
                                icon = MaterialSymbols.RoundedFilled.Bookmark,
                                contentDescription = "Bookmarks",
                                onClick = { selectedTab = 2 },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val headerTitle = when (selectedTab) {
                        0 -> stringResource(MR.strings.chapters)
                        1 -> "${pages.size} pages"
                        else -> "${bookmarkedChapters.size} bookmarks"
                    }
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Close,
                            contentDescription = stringResource(MR.strings.action_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        // Inset Search Field
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x09FFFFFF))
                                .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = stringResource(MR.strings.action_search_hint),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            )
                                        }
                                        innerTextField()
                                    },
                                )
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(20.dp),
                                    ) {
                                        Icon(
                                            imageVector = MaterialSymbols.Rounded.Close,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chapter List with sub-chapters and version grouping
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(
                                items = groupedChapters,
                                key = { it.id },
                            ) { group ->
                                val isGroupExpanded = group.id in expandedGroupIds
                                val isCurrent = group.primary.chapter.id == currentChapterId ||
                                    group.versions.any { it.chapter.id == currentChapterId }

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    ChapterListItem(
                                        readerChapter = group.primary,
                                        isCurrent = isCurrent,
                                        isSubChapter = group.isSubChapter,
                                        isFirstSubChapter = group.isFirstSubChapter,
                                        isLastSubChapter = group.isLastSubChapter,
                                        hasParentAbove = group.hasParentAbove,
                                        hasSubChaptersBelow = group.hasSubChaptersBelow,
                                        versionCount = group.versions.size,
                                        isExpanded = isGroupExpanded,
                                        onToggleExpand = {
                                            expandedGroupIds = if (isGroupExpanded) {
                                                expandedGroupIds - group.id
                                            } else {
                                                expandedGroupIds + group.id
                                            }
                                        },
                                        onClick = { onChapterClicked(group.primary) },
                                        onToggleBookmark = { onToggleBookmark(group.primary) },
                                    )

                                    if (isGroupExpanded && group.versions.size > 1) {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceInset,
                                            border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                group.versions.forEachIndexed { vIndex, version ->
                                                    val isLastVersion = vIndex == group.versions.lastIndex
                                                    ChapterVersionRow(
                                                        version = version,
                                                        isCurrent = version.chapter.id == currentChapterId,
                                                        isLast = isLastVersion,
                                                        onClick = { onChapterClicked(version) },
                                                        onToggleBookmark = { onToggleBookmark(version) },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Pages Scrub Preview with Chapter Switcher
                        val currentChapterIndex = chapters.indexOfFirst { it.chapter.id == currentChapterId }
                        val currChapter = chapters.getOrNull(currentChapterIndex)
                        val hasPrev = currentChapterIndex > 0
                        val hasNext = currentChapterIndex in 0 until chapters.lastIndex
                        var chapterPickerOpen by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 440.dp),
                        ) {
                            // Chapter Switcher Controls
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                IconButton(
                                    onClick = {
                                        if (hasPrev) {
                                            onSelectChapterForPreview?.invoke(chapters[currentChapterIndex - 1])
                                        }
                                    },
                                    enabled = hasPrev,
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.AutoMirroredRounded.ArrowBack,
                                        contentDescription = "Previous Chapter",
                                        tint = if (hasPrev) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    )
                                }

                                Box {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0x14FFFFFF),
                                        border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                                        onClick = { chapterPickerOpen = true },
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = currChapter?.chapter?.name ?: "Select Chapter",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.widthIn(max = 200.dp),
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }

                                    if (chapterPickerOpen) {
                                        Dialog(onDismissRequest = { chapterPickerOpen = false }) {
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = Color(0xFF16171A),
                                                border = BorderStroke(1.dp, Color(0x18FFFFFF)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                ) {
                                                    Text(
                                                        text = stringResource(MR.strings.chapters),
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(bottom = 12.dp),
                                                    )
                                                    val pickerListState = rememberLazyListState(
                                                        initialFirstVisibleItemIndex = currentChapterIndex.coerceAtLeast(0),
                                                    )
                                                    LazyColumn(
                                                        state = pickerListState,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .heightIn(max = 340.dp),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                                    ) {
                                                        items(chapters, key = { it.chapter.id }) { ch ->
                                                            val isSelected = ch.chapter.id == currentChapterId
                                                            Surface(
                                                                shape = RoundedCornerShape(10.dp),
                                                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0x0AFFFFFF),
                                                                border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                                                                onClick = {
                                                                    chapterPickerOpen = false
                                                                    onSelectChapterForPreview?.invoke(ch)
                                                                },
                                                                modifier = Modifier.fillMaxWidth(),
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                ) {
                                                                    Text(
                                                                        text = ch.chapter.name,
                                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                        ),
                                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis,
                                                                        modifier = Modifier.weight(1f),
                                                                    )
                                                                    if (isSelected) {
                                                                        Icon(
                                                                            imageVector = MaterialSymbols.Rounded.Check,
                                                                            contentDescription = null,
                                                                            tint = MaterialTheme.colorScheme.primary,
                                                                            modifier = Modifier.size(18.dp),
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

                                IconButton(
                                    onClick = {
                                        if (hasNext) {
                                            onSelectChapterForPreview?.invoke(chapters[currentChapterIndex + 1])
                                        }
                                    },
                                    enabled = hasNext,
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.AutoMirroredRounded.ArrowForward,
                                        contentDescription = "Next Chapter",
                                        tint = if (hasNext) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Pages Thumbnail Grid
                            if (pages.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(MR.strings.information_no_recent),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 92.dp),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    items(
                                        items = pages,
                                        key = { it.index },
                                    ) { page ->
                                        PageThumbnailItem(
                                            page = page,
                                            isCurrent = page.index == currentPageIndex,
                                            onClick = { onPageClicked(page.index) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Bookmarked Chapters List
                        if (bookmarkedChapters.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Bookmark,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(40.dp),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No bookmarked chapters",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 420.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                items(
                                    items = bookmarkedChapters,
                                    key = { it.chapter.id },
                                ) { readerChapter ->
                                    ChapterListItem(
                                        readerChapter = readerChapter,
                                        isCurrent = readerChapter.chapter.id == currentChapterId,
                                        onClick = { onChapterClicked(readerChapter) },
                                        onToggleBookmark = { onToggleBookmark(readerChapter) },
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

@Composable
private fun SegmentedTabButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Color(0x28FFFFFF) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ChapterListItem(
    readerChapter: ReaderChapter,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    isSubChapter: Boolean = false,
    isFirstSubChapter: Boolean = false,
    isLastSubChapter: Boolean = true,
    hasParentAbove: Boolean = false,
    hasSubChaptersBelow: Boolean = false,
    versionCount: Int = 1,
    isExpanded: Boolean = false,
    onToggleExpand: (() -> Unit)? = null,
) {
    val chapter = readerChapter.chapter
    var bookmarked by remember(chapter.bookmark) { mutableStateOf(chapter.bookmark) }

    val containerColor = if (isCurrent) {
        Color(0x1CFFFFFF)
    } else {
        Color.Transparent
    }

    val border = if (isCurrent) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
    } else {
        null
    }

    val textColor = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        chapter.read -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(if (border != null) Modifier.border(border, RoundedCornerShape(8.dp)) else Modifier)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(start = if (isSubChapter) 28.dp else 14.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        fontFeatureSettings = "tnum",
                    ),
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (!chapter.scanlator.isNullOrBlank()) {
                    Text(
                        text = chapter.scanlator.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (versionCount > 1 && onToggleExpand != null) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = MaterialTheme.colorScheme.surfaceInset,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.borderHairline),
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clickable(onClick = onToggleExpand),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "$versionCount versions",
                            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    bookmarked = !bookmarked
                    onToggleBookmark()
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = if (bookmarked) {
                        MaterialSymbols.RoundedFilled.Bookmark
                    } else {
                        MaterialSymbols.Rounded.Bookmark
                    },
                    contentDescription = null,
                    tint = if (bookmarked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    },
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ChapterVersionRow(
    version: ReaderChapter,
    isCurrent: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
) {
    var bookmarked by remember(version.chapter.bookmark) { mutableStateOf(version.chapter.bookmark) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = version.chapter.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                ),
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!version.chapter.scanlator.isNullOrBlank()) {
                Text(
                    text = version.chapter.scanlator.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        IconButton(
            onClick = {
                bookmarked = !bookmarked
                onToggleBookmark()
            },
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                imageVector = if (bookmarked) MaterialSymbols.RoundedFilled.Bookmark else MaterialSymbols.Rounded.Bookmark,
                contentDescription = null,
                tint = if (bookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun PageThumbnailItem(
    page: ReaderPage,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = page) {
        val streamFn = page.stream
        if (streamFn != null) {
            withContext(Dispatchers.IO) {
                try {
                    val stream = streamFn()
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 2
                    }
                    val bmp = BitmapFactory.decodeStream(stream, null, options)
                    value = bmp?.asImageBitmap()
                } catch (_: Throwable) {
                    value = null
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(3f / 4.2f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF18191D))
            .then(
                if (isCurrent) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                } else {
                    Modifier.border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(8.dp))
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val bmp = bitmapState.value
        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "${page.index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        }

        // Page number badge at bottom right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xCC000000))
                .padding(horizontal = 5.dp, vertical = 2.dp),
        ) {
            Text(
                text = "${page.index + 1}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                ),
                color = Color.White,
            )
        }
    }
}

internal fun groupReaderChapters(chapters: List<ReaderChapter>): List<ReaderChapterGroup> {
    if (chapters.isEmpty()) return emptyList()

    val isDescending = chapters.size >= 2 && chapters.first().chapter.chapterNumber > chapters.last().chapter.chapterNumber

    val duplicatesByNumber = chapters.filter { it.chapter.chapterNumber > 0.0 }
        .groupBy { it.chapter.chapterNumber }
        .filterValues { it.size > 1 }

    val handledNumbers = HashSet<Double>()
    val deduplicated = ArrayList<ReaderChapterGroup>(chapters.size)

    for (rc in chapters) {
        val number = rc.chapter.chapterNumber
        if (number <= 0.0 || number !in duplicatesByNumber) {
            deduplicated.add(ReaderChapterGroup(primary = rc))
            continue
        }

        if (handledNumbers.add(number)) {
            val group = duplicatesByNumber.getValue(number)
            val primary = group.maxByOrNull {
                var score = 0
                if (it.chapter.read) score += 4
                else if (it.chapter.lastPageRead > 0) score += 2
                score
            } ?: rc
            val orderedVersions = listOf(primary) + group.filter { it.chapter.id != primary.chapter.id }
            deduplicated.add(ReaderChapterGroup(primary = primary, versions = orderedVersions))
        }
    }

    val positioned = if (isDescending) {
        val result = ArrayList<ReaderChapterGroup>(deduplicated.size)
        var i = 0
        while (i < deduplicated.size) {
            val current = deduplicated[i]
            val num = current.primary.chapter.chapterNumber
            if (num > 0.0 && num % 1.0 != 0.0) {
                val baseNum = kotlin.math.floor(num)
                val group = mutableListOf<ReaderChapterGroup>()
                var j = i
                while (j < deduplicated.size) {
                    val item = deduplicated[j]
                    val itemNum = item.primary.chapter.chapterNumber
                    if (itemNum > 0.0 && kotlin.math.floor(itemNum) == baseNum) {
                        group.add(item)
                        j++
                    } else {
                        break
                    }
                }
                val parentItem = group.firstOrNull { it.primary.chapter.chapterNumber == baseNum }
                val subChapters = group.filter { it.primary.chapter.chapterNumber != baseNum }
                    .sortedBy { it.primary.chapter.chapterNumber }

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
        result
    } else {
        deduplicated
    }

    return positioned.mapIndexed { index, group ->
        val num = group.primary.chapter.chapterNumber
        val isSub = num > 0.0 && (num % 1.0 != 0.0)
        val baseNum = if (isSub) kotlin.math.floor(num) else num
        val prevItem = positioned.getOrNull(index - 1)
        val nextItem = positioned.getOrNull(index + 1)
        val prevNum = prevItem?.primary?.chapter?.chapterNumber ?: -1.0
        val nextNum = nextItem?.primary?.chapter?.chapterNumber ?: -1.0

        val isFirst = isSub && (prevItem == null || prevNum <= 0.0 || kotlin.math.floor(prevNum) != baseNum)
        val isLast = !isSub || (nextItem == null || nextNum <= 0.0 || (nextNum % 1.0 == 0.0) || kotlin.math.floor(nextNum) != baseNum)
        val hasParent = isSub && prevItem != null && prevNum == baseNum
        val hasSubBelow = !isSub && nextItem != null && nextNum > 0.0 && (nextNum % 1.0 != 0.0) && kotlin.math.floor(nextNum) == num

        group.copy(
            isSubChapter = isSub,
            isFirstSubChapter = isFirst,
            isLastSubChapter = isLast,
            hasParentAbove = hasParent,
            hasSubChaptersBelow = hasSubBelow,
        )
    }
}
