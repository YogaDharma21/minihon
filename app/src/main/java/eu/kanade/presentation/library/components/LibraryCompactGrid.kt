package eu.kanade.presentation.library.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.util.cascadeItemMotion
import eu.kanade.tachiyomi.ui.library.LibraryItem
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.MangaCover

private val ItemShape = RoundedCornerShape(16.dp)
private val NormalBorder = BorderStroke(1.dp, Color(0x12FFFFFF))

@Composable
internal fun LibraryCompactGrid(
    items: List<LibraryItem>,
    showTitle: Boolean,
    columns: Int,
    contentPadding: PaddingValues,
    selection: Set<Long>,
    onClick: (LibraryManga) -> Unit,
    onLongClick: (LibraryManga) -> Unit,
    onClickContinueReading: ((LibraryManga) -> Unit)?,
    searchQuery: String?,
    onGlobalSearchClicked: () -> Unit,
) {
    LazyLibraryGrid(
        modifier = Modifier.fillMaxSize(),
        columns = columns,
        contentPadding = contentPadding,
    ) {
        globalSearchItem(searchQuery, onGlobalSearchClicked)

        itemsIndexed(
            items = items,
            key = { _, libraryItem -> libraryItem.libraryManga.manga.id },
            contentType = { _, _ -> "library_compact_grid_item" },
        ) { index, libraryItem ->
            val manga = libraryItem.libraryManga.manga
            val isSelected = manga.id in selection
            val progress = if (libraryItem.libraryManga.totalChapters > 0) {
                libraryItem.libraryManga.readCount.toFloat() / libraryItem.libraryManga.totalChapters.toFloat()
            } else {
                null
            }
            Surface(
                modifier = Modifier.cascadeItemMotion(index = index),
                shape = ItemShape,
                color = Color(0xFF121316),
                border = if (isSelected) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    NormalBorder
                },
            ) {
                MangaCompactGridItem(
                    isSelected = isSelected,
                    title = manga.title.takeIf { showTitle },
                    coverData = MangaCover(
                        mangaId = manga.id,
                        sourceId = manga.source,
                        isMangaFavorite = manga.favorite,
                        url = manga.thumbnailUrl,
                        lastModified = manga.coverLastModified,
                    ),
                    unreadCount = libraryItem.badges.unreadCount,
                    readProgress = progress,
                    coverBadgeStart = {
                        DownloadsBadge(count = libraryItem.badges.downloadCount)
                    },
                    coverBadgeEnd = {
                        LanguageBadge(
                            isLocal = libraryItem.badges.isLocal,
                            sourceLanguage = libraryItem.badges.sourceLanguage,
                        )
                    },
                    onLongClick = { onLongClick(libraryItem.libraryManga) },
                    onClick = { onClick(libraryItem.libraryManga) },
                    onClickContinueReading = if (onClickContinueReading != null && libraryItem.unreadCount > 0) {
                        { onClickContinueReading(libraryItem.libraryManga) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}
