package eu.kanade.presentation.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.manga.components.MangaCover
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.roundedfilled.PlayArrow
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.BadgeGroup
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.selectedBackground
import tachiyomi.domain.manga.model.MangaCover as MangaCoverModel

object CommonMangaItemDefaults {
    val GridHorizontalSpacer = 8.dp
    val GridVerticalSpacer = 8.dp

    @Suppress("ConstPropertyName")
    const val BrowseFavoriteCoverAlpha = 0.34f
}

private val ContinueReadingButtonSizeSmall = 28.dp
private val ContinueReadingButtonSizeLarge = 32.dp

private val ContinueReadingButtonIconSizeSmall = 16.dp
private val ContinueReadingButtonIconSizeLarge = 20.dp

private val ContinueReadingButtonGridPadding = 6.dp
private val ContinueReadingButtonListSpacing = 8.dp

private const val GRID_SELECTED_COVER_ALPHA = 0.76f

private class NotchedRibbonShape(private val notchDepth: Dp = 4.dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val notchPx = with(density) { notchDepth.toPx() }
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(size.width / 2f, (size.height - notchPx).coerceAtLeast(0f))
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

private val DefaultNotchedRibbonShape = NotchedRibbonShape(notchDepth = 4.dp)

@Composable
private fun VerticalSpineTag(
    text: String,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier,
        content = {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFFA6B0BD),
                    fontFeatureSettings = "tnum",
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
            )
        },
    ) { measurables, constraints ->
        if (measurables.isEmpty()) {
            layout(constraints.minWidth, constraints.minHeight) {}
        } else {
            val targetMaxWidth = if (constraints.hasBoundedHeight) constraints.maxHeight else 300
            val targetMaxHeight = if (constraints.hasBoundedWidth) constraints.maxWidth else 60
            val placeable = measurables.first().measure(
                constraints.copy(
                    minWidth = 0,
                    maxWidth = targetMaxWidth,
                    minHeight = 0,
                    maxHeight = targetMaxHeight,
                ),
            )
            val parentWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else placeable.height
            val parentHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else placeable.width
            layout(parentWidth, parentHeight) {
                val x = (parentWidth - placeable.width) / 2
                val y = (parentHeight - placeable.height) / 2
                placeable.placeRelativeWithLayer(x = x, y = y) {
                    rotationZ = -90f
                }
            }
        }
    }
}

@Composable
private fun TankobonSpine(
    tag: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(20.dp)
            .fillMaxHeight(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF0121316)),
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .align(Alignment.CenterStart)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0x40000000), Color.Transparent),
                    ),
                ),
        )
        VerticalSpineTag(
            text = tag.uppercase(),
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .align(Alignment.CenterEnd)
                .background(Color(0x66000000)),
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .align(Alignment.CenterEnd)
                .padding(end = 1.dp)
                .background(Color(0x1FFFFFFF)),
        )
    }
}

@Composable
private fun NotchedRibbonBookmark(
    count: Long,
    modifier: Modifier = Modifier,
) {
    val countText = if (count > 999) "999+" else count.toString()
    Box(
        modifier = modifier
            .clip(DefaultNotchedRibbonShape)
            .background(Color(0xFFC99A90))
            .padding(horizontal = 6.dp)
            .padding(top = 3.dp, bottom = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = countText,
            color = Color(0xFF08090B),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFeatureSettings = "tnum",
            ),
        )
    }
}

@Composable
private fun ReadingProgressBar(
    progress: Float?,
    modifier: Modifier = Modifier,
) {
    if (progress == null || progress <= 0f) return
    val clampedProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Color(0x14FFFFFF)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(clampedProgress)
                .background(Color(0xFFA9BFA9)),
        )
    }
}

/**
 * Layout of grid list item with title overlaying the cover.
 * Accepts null [title] for a cover-only view.
 */
@Composable
fun MangaCompactGridItem(
    coverData: MangaCoverModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean = false,
    title: String? = null,
    onClickContinueReading: (() -> Unit)? = null,
    coverAlpha: Float = 1f,
    spineTag: String? = null,
    unreadCount: Long = 0L,
    readProgress: Float? = null,
    coverBadgeStart: @Composable (RowScope.() -> Unit)? = null,
    coverBadgeEnd: @Composable (RowScope.() -> Unit)? = null,
) {
    GridItemSelectable(
        isSelected = isSelected,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        MangaGridCover(
            cover = {
                MangaCover.Book(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (isSelected) GRID_SELECTED_COVER_ALPHA else coverAlpha),
                    data = coverData,
                )
            },
            spineTag = spineTag,
            unreadCount = unreadCount,
            badgesStart = coverBadgeStart,
            badgesEnd = coverBadgeEnd,
            content = {
                if (title != null) {
                    CompactPedestalShelf(
                        title = title,
                        readProgress = readProgress,
                        onClickContinueReading = onClickContinueReading,
                    )
                } else {
                    if (readProgress != null && readProgress > 0f) {
                        ReadingProgressBar(
                            progress = readProgress,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                        )
                    }
                    if (onClickContinueReading != null) {
                        ContinueReadingButton(
                            size = ContinueReadingButtonSizeLarge,
                            iconSize = ContinueReadingButtonIconSizeLarge,
                            onClick = onClickContinueReading,
                            modifier = Modifier
                                .padding(ContinueReadingButtonGridPadding)
                                .align(Alignment.BottomEnd),
                        )
                    }
                }
            },
        )
    }
}

/**
 * Bottom solid matte pedestal shelf for [MangaCompactGridItem]
 */
@Composable
private fun BoxScope.CompactPedestalShelf(
    title: String,
    readProgress: Float?,
    onClickContinueReading: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(Color(0xFF121316)),
    ) {
        ReadingProgressBar(progress = readProgress)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GridItemTitle(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = if (onClickContinueReading != null) 4.dp else 0.dp),
                title = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color(0xFFF4F4F2),
                    fontWeight = FontWeight.Medium,
                ),
                minLines = 1,
                maxLines = 2,
            )
            if (onClickContinueReading != null) {
                ContinueReadingButton(
                    size = ContinueReadingButtonSizeSmall,
                    iconSize = ContinueReadingButtonIconSizeSmall,
                    onClick = onClickContinueReading,
                )
            }
        }
    }
}

/**
 * Layout of grid list item with title below the cover.
 */
@Composable
fun MangaComfortableGridItem(
    coverData: MangaCoverModel,
    title: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean = false,
    titleMaxLines: Int = 2,
    coverAlpha: Float = 1f,
    spineTag: String? = null,
    unreadCount: Long = 0L,
    readProgress: Float? = null,
    coverBadgeStart: (@Composable RowScope.() -> Unit)? = null,
    coverBadgeEnd: (@Composable RowScope.() -> Unit)? = null,
    onClickContinueReading: (() -> Unit)? = null,
) {
    GridItemSelectable(
        isSelected = isSelected,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Column(
            modifier = Modifier.background(Color(0xFF121316)),
        ) {
            MangaGridCover(
                cover = {
                    MangaCover.Book(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isSelected) GRID_SELECTED_COVER_ALPHA else coverAlpha),
                        data = coverData,
                        shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
                    )
                },
                spineTag = spineTag,
                unreadCount = unreadCount,
                badgesStart = coverBadgeStart,
                badgesEnd = coverBadgeEnd,
                content = {
                    if (onClickContinueReading != null) {
                        ContinueReadingButton(
                            size = ContinueReadingButtonSizeLarge,
                            iconSize = ContinueReadingButtonIconSizeLarge,
                            onClick = onClickContinueReading,
                            modifier = Modifier
                                .padding(ContinueReadingButtonGridPadding)
                                .align(Alignment.BottomEnd),
                        )
                    }
                },
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121316)),
            ) {
                ReadingProgressBar(progress = readProgress)
                GridItemTitle(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    title = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color(0xFFF4F4F2),
                        fontWeight = FontWeight.Medium,
                    ),
                    minLines = 2,
                    maxLines = titleMaxLines,
                )
            }
        }
    }
}

/**
 * Common cover layout to add contents to be drawn on top of the cover.
 */
@Composable
private fun MangaGridCover(
    modifier: Modifier = Modifier,
    spineTag: String? = null,
    unreadCount: Long = 0L,
    cover: @Composable BoxScope.() -> Unit = {},
    badgesStart: (@Composable RowScope.() -> Unit)? = null,
    badgesEnd: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable (BoxScope.() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(MangaCover.Book.ratio),
    ) {
        cover()

        TankobonSpine(
            tag = spineTag?.takeIf { it.isNotBlank() } ?: "MANGA",
            modifier = Modifier.align(Alignment.CenterStart),
        )

        if (unreadCount > 0) {
            NotchedRibbonBookmark(
                count = unreadCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp),
            )
        }

        if (badgesStart != null) {
            BadgeGroup(
                modifier = Modifier
                    .padding(start = 24.dp, top = 6.dp)
                    .align(Alignment.TopStart),
                content = badgesStart,
            )
        }

        if (badgesEnd != null) {
            BadgeGroup(
                modifier = Modifier
                    .padding(top = 6.dp, end = if (unreadCount > 0) 36.dp else 6.dp)
                    .align(Alignment.TopEnd),
                content = badgesEnd,
            )
        }

        content?.invoke(this)
    }
}

@Composable
private fun GridItemTitle(
    title: String,
    style: TextStyle,
    minLines: Int,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
) {
    Text(
        modifier = modifier,
        text = title,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        minLines = minLines,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = style,
    )
}

/**
 * Wrapper for grid items to handle selection state, click and long click.
 */
@Composable
private fun GridItemSelectable(
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .selectedOutline(isSelected = isSelected, color = MaterialTheme.colorScheme.secondary),
    ) {
        val contentColor = if (isSelected) {
            MaterialTheme.colorScheme.onSecondary
        } else {
            LocalContentColor.current
        }
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

/**
 * @see GridItemSelectable
 */
private fun Modifier.selectedOutline(
    isSelected: Boolean,
    color: Color,
) = drawBehind { if (isSelected) drawRect(color = color) }

/**
 * Layout of list item.
 */
@Composable
fun MangaListItem(
    coverData: MangaCoverModel,
    title: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    badge: @Composable (RowScope.() -> Unit),
    isSelected: Boolean = false,
    coverAlpha: Float = 1f,
    onClickContinueReading: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .selectedBackground(isSelected)
            .height(56.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MangaCover.Square(
            modifier = Modifier
                .fillMaxHeight()
                .alpha(coverAlpha),
            data = coverData,
            shape = RoundedCornerShape(12.dp),
        )
        Text(
            text = title,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
        )
        BadgeGroup(content = badge)
        if (onClickContinueReading != null) {
            ContinueReadingButton(
                size = ContinueReadingButtonSizeSmall,
                iconSize = ContinueReadingButtonIconSizeSmall,
                onClick = onClickContinueReading,
                modifier = Modifier.padding(start = ContinueReadingButtonListSpacing),
            )
        }
    }
}

@Composable
private fun ContinueReadingButton(
    size: Dp,
    iconSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        FilledIconButton(
            onClick = onClick,
            shape = MaterialTheme.shapes.small,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                contentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer),
            ),
            modifier = Modifier.size(size),
        ) {
            Icon(
                imageVector = MaterialSymbols.RoundedFilled.PlayArrow,
                contentDescription = stringResource(MR.strings.action_resume),
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
