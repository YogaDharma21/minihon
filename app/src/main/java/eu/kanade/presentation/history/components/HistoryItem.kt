package eu.kanade.presentation.history.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import eu.kanade.presentation.manga.components.DotSeparatorText
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.presentation.util.formatChapterNumber
import eu.kanade.presentation.util.rememberResourceBitmapPainter
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.util.lang.toTimestampString
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Delete
import mihon.icons.materialsymbols.rounded.Favorite
import mihon.icons.materialsymbols.roundedfilled.PlayArrow
import tachiyomi.domain.history.model.HistoryWithRelations
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.borderHairline
import tachiyomi.presentation.core.theme.surfaceInset
import tachiyomi.presentation.core.util.selectedBackground
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HistoryItem(
    history: HistoryWithRelations,
    timeFormatter: DateTimeFormatter,
    onClickCover: () -> Unit,
    onClickResume: () -> Unit,
    onClickDelete: () -> Unit,
    onClickFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    readProgress: Float? = null,
) {
    val haptic = LocalHapticFeedback.current
    val readAt = remember(timeFormatter, history.readAt) {
        history.readAt?.toTimestampString(timeFormatter) ?: ""
    }

    Row(
        modifier = modifier
            .selectedBackground(selected)
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = onClickResume,
                        onLongClick = {
                            onLongClick()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                    )
                } else {
                    Modifier.clickable(onClick = onClickResume)
                },
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HistoryShowcaseCover(
            coverData = history.coverData,
            onClick = onClickCover,
            progress = readProgress,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = history.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color(0xFFF4F4F2),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                ),
            )

            if (history.chapterNumber > -1) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = MaterialTheme.colorScheme.surfaceInset,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.borderHairline),
                ) {
                    Text(
                        text = "Ch. ${formatChapterNumber(history.chapterNumber)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFeatureSettings = "tnum",
                            fontWeight = FontWeight.Medium,
                        ),
                        color = Color(0xFFF4F4F2),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (readAt.isNotBlank()) {
                    Text(
                        text = readAt,
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        color = Color(0xFF8A8C92),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (readProgress != null && readProgress > 0f) {
                    if (readAt.isNotBlank()) {
                        DotSeparatorText()
                    }
                    Text(
                        text = "${(readProgress.coerceIn(0f, 1f) * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        color = Color(0xFFA9BFA9),
                        maxLines = 1,
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (!history.coverData.isMangaFavorite) {
                IconButton(
                    onClick = onClickFavorite,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Favorite,
                        contentDescription = stringResource(MR.strings.add_to_library),
                        tint = Color(0xFF8A8C92),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            IconButton(
                onClick = onClickDelete,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Delete,
                    contentDescription = stringResource(MR.strings.action_delete),
                    tint = Color(0xFF8A8C92),
                    modifier = Modifier.size(18.dp),
                )
            }

            FilledIconButton(
                onClick = onClickResume,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(36.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFFF2F1EC),
                    contentColor = Color(0xFF0A0A0B),
                ),
            ) {
                Icon(
                    imageVector = MaterialSymbols.RoundedFilled.PlayArrow,
                    contentDescription = stringResource(MR.strings.action_resume),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun HistoryShowcaseCover(
    coverData: tachiyomi.domain.manga.model.MangaCover,
    onClick: () -> Unit,
    progress: Float?,
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
            .clickable(
                role = Role.Button,
                onClick = onClick,
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

        // Progress bar overlay
        if (progress != null && progress > 0f) {
            val clampedProgress = progress.coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0x66000000)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(clampedProgress)
                        .background(Color(0xFFA9BFA9)),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HistoryItemPreviews(
    @PreviewParameter(HistoryWithRelationsProvider::class)
    historyWithRelations: HistoryWithRelations,
) {
    TachiyomiPreviewTheme {
        Surface {
            HistoryItem(
                history = historyWithRelations,
                timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT),
                onClickCover = {},
                onClickResume = {},
                onClickDelete = {},
                onClickFavorite = {},
                readProgress = 0.72f,
            )
        }
    }
}
