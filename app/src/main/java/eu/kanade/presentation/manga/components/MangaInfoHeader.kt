package eu.kanade.presentation.manga.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownAnnotatorConfig
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.system.copyToClipboard
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.AttachMoney
import mihon.icons.materialsymbols.rounded.Block
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.rounded.Close
import mihon.icons.materialsymbols.rounded.DoneAll
import mihon.icons.materialsymbols.rounded.Favorite
import mihon.icons.materialsymbols.rounded.Person
import mihon.icons.materialsymbols.rounded.Public
import mihon.icons.materialsymbols.rounded.Schedule
import mihon.icons.materialsymbols.rounded.Sync
import mihon.icons.materialsymbols.roundedfilled.Brush
import mihon.icons.materialsymbols.roundedfilled.Favorite
import mihon.icons.materialsymbols.roundedfilled.Pause
import mihon.icons.materialsymbols.roundedfilled.Warning
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.findChildOfType
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.DISABLED_ALPHA
import tachiyomi.presentation.core.components.material.TextButton
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.amber
import tachiyomi.presentation.core.theme.borderHairline
import tachiyomi.presentation.core.theme.crimson
import tachiyomi.presentation.core.theme.emerald
import tachiyomi.presentation.core.theme.steelBlue
import tachiyomi.presentation.core.theme.subtle
import tachiyomi.presentation.core.theme.surfaceInset
import tachiyomi.presentation.core.util.clickableNoIndication
import tachiyomi.presentation.core.util.secondaryItemAlpha
import kotlin.math.roundToInt
import tachiyomi.presentation.core.components.material.Surface as TachiyomiSurface

@Composable
fun MangaInfoBox(
    isTabletUi: Boolean,
    appBarPadding: Dp,
    manga: Manga,
    source: Source,
    sourceName: String,
    isStubSource: Boolean,
    onCoverClick: () -> Unit,
    doSearch: (query: String, global: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    chapterCount: Int = 0,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        val canvasColor = MaterialTheme.colorScheme.background
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(manga)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .blur(8.dp)
                .alpha(0.45f)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.0f to canvasColor.copy(alpha = 0.2f),
                            0.35f to Color.Transparent,
                            0.75f to canvasColor.copy(alpha = 0.85f),
                            1.0f to canvasColor,
                        ),
                    )
                },
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = appBarPadding + 8.dp, bottom = 8.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(24.dp),
                    clip = false,
                    ambientColor = Color(0xE6000000),
                    spotColor = Color(0xE6000000),
                ),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.borderHairline),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                if (!isTabletUi) {
                    MangaCardContentSmall(
                        manga = manga,
                        source = source,
                        sourceName = sourceName,
                        isStubSource = isStubSource,
                        chapterCount = chapterCount,
                        onCoverClick = onCoverClick,
                        doSearch = doSearch,
                    )
                } else {
                    MangaCardContentLarge(
                        manga = manga,
                        source = source,
                        sourceName = sourceName,
                        isStubSource = isStubSource,
                        chapterCount = chapterCount,
                        onCoverClick = onCoverClick,
                        doSearch = doSearch,
                    )
                }
            }
        }
    }
}

@Composable
fun MangaActionRow(
    favorite: Boolean,
    trackingCount: Int,
    onAddToLibraryClicked: () -> Unit,
    onWebViewClicked: (() -> Unit)?,
    onWebViewLongClicked: (() -> Unit)?,
    onTrackingClicked: () -> Unit,
    onEditCategory: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MangaActionButton(
            title = if (favorite) {
                stringResource(MR.strings.in_library)
            } else {
                stringResource(MR.strings.add_to_library)
            },
            icon = if (favorite) MaterialSymbols.RoundedFilled.Favorite else MaterialSymbols.Rounded.Favorite,
            active = favorite,
            onClick = onAddToLibraryClicked,
            onLongClick = onEditCategory,
        )
        MangaActionButton(
            title = if (trackingCount == 0) {
                stringResource(MR.strings.manga_tracking_tab)
            } else {
                pluralStringResource(MR.plurals.num_trackers, count = trackingCount, trackingCount)
            },
            icon = if (trackingCount == 0) MaterialSymbols.Rounded.Sync else MaterialSymbols.Rounded.Check,
            active = trackingCount > 0,
            onClick = onTrackingClicked,
        )
        MangaActionButton(
            title = stringResource(MR.strings.action_web_view),
            icon = MaterialSymbols.Rounded.Public,
            active = false,
            enabled = onWebViewClicked != null,
            onClick = onWebViewClicked ?: {},
            onLongClick = onWebViewLongClicked,
        )
    }
}

@Composable
fun ExpandableMangaDescription(
    defaultExpandState: Boolean,
    description: String?,
    tagsProvider: () -> List<String>?,
    notes: String,
    onTagSearch: (String) -> Unit,
    onCopyTagToClipboard: (tag: String) -> Unit,
    onEditNotes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color(0xFF121316),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0x12FFFFFF)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            val (expanded, onExpanded) = rememberSaveable {
                mutableStateOf(defaultExpandState)
            }
            val desc =
                description.takeIf { !it.isNullOrBlank() } ?: stringResource(MR.strings.description_placeholder)

            MangaSummary(
                description = desc,
                expanded = expanded,
                notes = notes,
                onEditNotesClicked = onEditNotes,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickableNoIndication { onExpanded(!expanded) },
            )
            val tags = tagsProvider()
            if (!tags.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .padding(vertical = 4.dp)
                        .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                        .fillMaxWidth(),
                ) {
                    var showMenu by remember { mutableStateOf(false) }
                    var tagSelected by remember { mutableStateOf("") }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(MR.strings.action_search)) },
                            onClick = {
                                onTagSearch(tagSelected)
                                showMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(MR.strings.action_copy_to_clipboard)) },
                            onClick = {
                                onCopyTagToClipboard(tagSelected)
                                showMenu = false
                            },
                        )
                    }
                    if (expanded) {
                        FlowRow(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
                        ) {
                            tags.forEach {
                                TagsChip(
                                    modifier = DefaultTagChipModifier,
                                    text = it,
                                    onClick = {
                                        tagSelected = it
                                        showMenu = true
                                    },
                                )
                            }
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
                        ) {
                            items(items = tags) {
                                TagsChip(
                                    modifier = DefaultTagChipModifier,
                                    text = it,
                                    onClick = {
                                        tagSelected = it
                                        showMenu = true
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MangaCardContentSmall(
    manga: Manga,
    source: Source,
    sourceName: String,
    isStubSource: Boolean,
    chapterCount: Int,
    onCoverClick: () -> Unit,
    doSearch: (query: String, global: Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            MangaCover.Book(
                modifier = Modifier
                    .sizeIn(maxWidth = 96.dp)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.borderHairline),
                        shape = RoundedCornerShape(12.dp),
                    ),
                data = ImageRequest.Builder(LocalContext.current)
                    .data(manga)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(MR.strings.manga_cover),
                shape = RoundedCornerShape(12.dp),
                onClick = onCoverClick,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MangaHeaderTitles(
                    title = manga.title,
                    author = manga.author,
                    artist = manga.artist,
                    doSearch = doSearch,
                )
            }
        }

        MangaKpiBar(
            chapterCount = chapterCount,
            status = manga.status,
            source = source,
            sourceName = sourceName,
            isStubSource = isStubSource,
            doSearch = doSearch,
        )
    }
}

@Composable
private fun MangaCardContentLarge(
    manga: Manga,
    source: Source,
    sourceName: String,
    isStubSource: Boolean,
    chapterCount: Int,
    onCoverClick: () -> Unit,
    doSearch: (query: String, global: Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MangaCover.Book(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.borderHairline),
                    shape = RoundedCornerShape(16.dp),
                ),
            data = ImageRequest.Builder(LocalContext.current)
                .data(manga)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(MR.strings.manga_cover),
            shape = RoundedCornerShape(16.dp),
            onClick = onCoverClick,
        )

        MangaHeaderTitles(
            title = manga.title,
            author = manga.author,
            artist = manga.artist,
            doSearch = doSearch,
            textAlign = TextAlign.Center,
        )

        MangaKpiBar(
            chapterCount = chapterCount,
            status = manga.status,
            source = source,
            sourceName = sourceName,
            isStubSource = isStubSource,
            doSearch = doSearch,
        )
    }
}

@Composable
private fun MangaHeaderTitles(
    title: String,
    author: String?,
    artist: String?,
    doSearch: (query: String, global: Boolean) -> Unit,
    textAlign: TextAlign? = null,
) {
    val context = LocalContext.current
    val rowArrangement = if (textAlign == TextAlign.Center) {
        Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    } else {
        Arrangement.spacedBy(6.dp)
    }

    Text(
        text = title.ifBlank { stringResource(MR.strings.unknown_title) },
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.02).em,
        ),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoIndication(
                onLongClick = {
                    if (title.isNotBlank()) context.copyToClipboard(title, title)
                },
                onClick = { if (title.isNotBlank()) doSearch(title, true) },
            ),
        textAlign = textAlign,
    )

    Spacer(modifier = Modifier.height(2.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .secondaryItemAlpha(),
        horizontalArrangement = rowArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = MaterialSymbols.Rounded.Person,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = author?.takeIf { it.isNotBlank() } ?: stringResource(MR.strings.unknown_author),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickableNoIndication(
                onLongClick = {
                    if (!author.isNullOrBlank()) context.copyToClipboard(author, author)
                },
                onClick = { if (!author.isNullOrBlank()) doSearch(author, true) },
            ),
            textAlign = textAlign,
        )
    }

    if (!artist.isNullOrBlank() && author != artist) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .secondaryItemAlpha(),
            horizontalArrangement = rowArrangement,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MaterialSymbols.RoundedFilled.Brush,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickableNoIndication(
                    onLongClick = { context.copyToClipboard(artist, artist) },
                    onClick = { doSearch(artist, true) },
                ),
                textAlign = textAlign,
            )
        }
    }
}

@Composable
private fun MangaKpiBar(
    chapterCount: Int,
    status: Long,
    source: Source,
    sourceName: String,
    isStubSource: Boolean,
    doSearch: (query: String, global: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceInset,
        border = BorderStroke(1.dp, Color(0x0AFFFFFF)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Column 1: CHAPTERS
            Column(
                modifier = Modifier.weight(0.85f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(MR.strings.chapters).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.16.em,
                        color = Color(0xFF8A8C92),
                    ),
                    maxLines = 1,
                )
                Box(
                    modifier = Modifier.height(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = chapterCount.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum",
                            letterSpacing = (-0.03).em,
                            color = Color(0xFFF4F4F2),
                        ),
                        maxLines = 1,
                    )
                }
            }

            // Divider 1
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(Color(0x0FFFFFFF)),
            )

            // Column 2: STATUS
            Column(
                modifier = Modifier.weight(0.95f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(MR.strings.status).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.16.em,
                        color = Color(0xFF8A8C92),
                    ),
                    maxLines = 1,
                )
                Box(
                    modifier = Modifier.height(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val statusConfig = rememberStatusConfig(status)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusConfig.bgColor,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                imageVector = statusConfig.icon,
                                contentDescription = null,
                                tint = statusConfig.color,
                                modifier = Modifier.size(11.dp),
                            )
                            Text(
                                text = statusConfig.text,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = statusConfig.color,
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            // Divider 2
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(Color(0x0FFFFFFF)),
            )

            // Column 3: SOURCE
            Column(
                modifier = Modifier.weight(1.35f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "SOURCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.16.em,
                        color = Color(0xFF8A8C92),
                    ),
                    maxLines = 1,
                )
                Box(
                    modifier = Modifier.height(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val sourceColor = if (isStubSource) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.steelBlue
                    }
                    val sourceBgColor = if (isStubSource) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.steelBlue.copy(alpha = 0.12f)
                    }
                    val sourceIcon = if (isStubSource) {
                        MaterialSymbols.RoundedFilled.Warning
                    } else {
                        MaterialSymbols.Rounded.Public
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = sourceBgColor,
                        modifier = Modifier.clickableNoIndication {
                            doSearch(source.query(), false)
                        },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                imageVector = sourceIcon,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = sourceColor,
                            )
                            Text(
                                text = sourceName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = sourceColor,
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class StatusConfig(
    val text: String,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color,
)

@Composable
private fun rememberStatusConfig(status: Long): StatusConfig {
    val emerald = MaterialTheme.colorScheme.emerald
    val amber = MaterialTheme.colorScheme.amber
    val crimson = MaterialTheme.colorScheme.crimson
    val steelBlue = MaterialTheme.colorScheme.steelBlue
    val subtle = MaterialTheme.colorScheme.subtle

    val text = when (status) {
        SManga.ONGOING.toLong() -> stringResource(MR.strings.ongoing)
        SManga.COMPLETED.toLong() -> stringResource(MR.strings.completed)
        SManga.LICENSED.toLong() -> stringResource(MR.strings.licensed)
        SManga.PUBLISHING_FINISHED.toLong() -> stringResource(MR.strings.publishing_finished)
        SManga.CANCELLED.toLong() -> stringResource(MR.strings.cancelled)
        SManga.ON_HIATUS.toLong() -> stringResource(MR.strings.on_hiatus)
        else -> stringResource(MR.strings.unknown)
    }

    val icon = when (status) {
        SManga.ONGOING.toLong() -> MaterialSymbols.Rounded.Schedule
        SManga.COMPLETED.toLong() -> MaterialSymbols.Rounded.DoneAll
        SManga.LICENSED.toLong() -> MaterialSymbols.Rounded.AttachMoney
        SManga.PUBLISHING_FINISHED.toLong() -> MaterialSymbols.Rounded.Check
        SManga.CANCELLED.toLong() -> MaterialSymbols.Rounded.Close
        SManga.ON_HIATUS.toLong() -> MaterialSymbols.RoundedFilled.Pause
        else -> MaterialSymbols.Rounded.Block
    }

    val (color, bgColor) = when (status) {
        SManga.COMPLETED.toLong(),
        SManga.PUBLISHING_FINISHED.toLong(),
        -> emerald to emerald.copy(alpha = 0.12f)

        SManga.ONGOING.toLong(),
        SManga.ON_HIATUS.toLong(),
        -> amber to amber.copy(alpha = 0.12f)

        SManga.CANCELLED.toLong() -> crimson to crimson.copy(alpha = 0.13f)

        SManga.LICENSED.toLong() -> steelBlue to steelBlue.copy(alpha = 0.12f)

        else -> subtle to subtle.copy(alpha = 0.12f)
    }

    return remember(status, text) {
        StatusConfig(text, icon, color, bgColor)
    }
}

private fun Source.query(): String = """src:"$name" lang:$lang""".takeIf { name.isNotBlank() } ?: "srcid:$id"

@Composable
private fun descriptionAnnotator(loadImages: Boolean, linkStyle: SpanStyle) = remember(loadImages, linkStyle) {
    markdownAnnotator(
        annotate = { content, child ->
            if (!loadImages && child.type == MarkdownElementTypes.IMAGE) {
                val inlineLink = child.findChildOfType(MarkdownElementTypes.INLINE_LINK)

                val url = inlineLink?.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)
                    ?.getUnescapedTextInNode(content)
                    ?: inlineLink?.findChildOfType(MarkdownElementTypes.AUTOLINK)
                        ?.findChildOfType(MarkdownTokenTypes.AUTOLINK)
                        ?.getUnescapedTextInNode(content)
                    ?: return@markdownAnnotator false

                val textNode = inlineLink?.findChildOfType(MarkdownElementTypes.LINK_TITLE)
                    ?: inlineLink?.findChildOfType(MarkdownElementTypes.LINK_TEXT)
                val altText = textNode?.findChildOfType(MarkdownTokenTypes.TEXT)
                    ?.getUnescapedTextInNode(content).orEmpty()

                withLink(LinkAnnotation.Url(url = url)) {
                    pushStyle(linkStyle)
                    appendInlineContent(MARKDOWN_INLINE_IMAGE_TAG)
                    append(altText)
                    pop()
                }

                return@markdownAnnotator true
            }

            if (child.type in DISALLOWED_MARKDOWN_TYPES) {
                append(content.substring(child.startOffset, child.endOffset))
                return@markdownAnnotator true
            }

            false
        },
        config = markdownAnnotatorConfig(
            eolAsNewLine = true,
        ),
    )
}

@Composable
private fun MangaSummary(
    description: String,
    notes: String,
    expanded: Boolean,
    onEditNotesClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preferences = remember { context.appGraph.uiPreferences }
    val loadImages = remember { preferences.imagesInDescription.get() }
    val animProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "summary",
    )
    var infoHeight by remember { mutableIntStateOf(0) }
    Layout(
        modifier = modifier.clipToBounds(),
        contents = listOf(
            {
                Text(
                    // Shows at least 3 lines if no notes
                    // when there are notes show 6
                    text = if (notes.isBlank()) "\n\n" else "\n\n\n\n\n",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            {
                Column(
                    modifier = Modifier.onSizeChanged { size ->
                        infoHeight = size.height
                    },
                ) {
                    MangaNotesSection(
                        content = notes,
                        expanded = expanded,
                        onEditNotes = onEditNotesClicked,
                    )
                    SelectionContainer {
                        MarkdownRender(
                            content = description,
                            modifier = Modifier.secondaryItemAlpha(),
                            annotator = descriptionAnnotator(
                                loadImages = loadImages,
                                linkStyle = getMarkdownLinkStyle().toSpanStyle(),
                            ),
                            loadImages = loadImages,
                        )
                    }
                }
            },
            {
                val colors = listOf(Color.Transparent, Color(0xFF121316))
                Box(
                    modifier = Modifier.background(Brush.verticalGradient(colors = colors)),
                    contentAlignment = Alignment.Center,
                ) {
                    val image = AnimatedImageVector.animatedVectorResource(R.drawable.animated_caret_flip)
                    Icon(
                        painter = rememberAnimatedVectorPainter(image, !expanded),
                        contentDescription = stringResource(
                            if (expanded) MR.strings.manga_info_collapse else MR.strings.manga_info_expand,
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.background(Brush.radialGradient(colors = colors.asReversed())),
                    )
                }
            },
        ),
    ) { (shrunk, actual, scrim), constraints ->
        val shrunkHeight = shrunk.single()
            .measure(constraints)
            .height
        val heightDelta = infoHeight - shrunkHeight
        val scrimHeight = 24.dp.roundToPx()

        val actualPlaceable = actual.single()
            .measure(constraints)
        val scrimPlaceable = scrim.single()
            .measure(Constraints.fixed(width = constraints.maxWidth, height = scrimHeight))

        val currentHeight = shrunkHeight + ((heightDelta + scrimHeight) * animProgress).roundToInt()
        layout(constraints.maxWidth, currentHeight) {
            actualPlaceable.place(0, 0)

            val scrimY = currentHeight - scrimHeight
            scrimPlaceable.place(0, scrimY)
        }
    }
}

private val DefaultTagChipModifier = Modifier.padding(vertical = 4.dp)

@Composable
private fun TagsChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        SuggestionChip(
            modifier = modifier,
            onClick = onClick,
            label = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                )
            },
            shape = CircleShape,
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = Color(0x09FFFFFF),
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            border = BorderStroke(1.dp, Color(0x12FFFFFF)),
        )
    }
}

@Composable
private fun RowScope.MangaActionButton(
    title: String,
    icon: ImageVector,
    active: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "pill_scale",
    )

    val containerColor = if (active) {
        MaterialTheme.colorScheme.primary
    } else {
        Color(0xFF17181C)
    }
    val contentColor = if (active) {
        MaterialTheme.colorScheme.onPrimary
    } else if (!enabled) {
        Color(0xFF5F6065)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val border = if (active) {
        null
    } else {
        BorderStroke(1.dp, Color(0x12FFFFFF))
    }

    TachiyomiSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = border,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = title,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
