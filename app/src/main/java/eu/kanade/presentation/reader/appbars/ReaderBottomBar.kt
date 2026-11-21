package eu.kanade.presentation.reader.appbars

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.reader.components.ChapterNavigator
import eu.kanade.presentation.reader.components.ChapterNavigatorType
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.FormatListBulleted
import mihon.icons.materialsymbols.rounded.Settings
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun ReaderBottomBar(
    readingMode: ReadingMode,
    onClickDisplayLayout: () -> Unit,
    onClickSettings: () -> Unit,
    onClickChapterList: (() -> Unit)? = null,
    chapterNavigatorType: ChapterNavigatorType = ChapterNavigatorType.HORIZONTAL_LTR,
    onNextChapter: () -> Unit = {},
    enabledNext: Boolean = false,
    onPreviousChapter: () -> Unit = {},
    enabledPrevious: Boolean = false,
    currentPage: Int = 0,
    totalPages: Int = 0,
    onPageIndexChange: (Int) -> Unit = {},
    onPageIndexChangeFinished: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (chapterNavigatorType.isHorizontal()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {},
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF121316),
                border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    ChapterNavigator(
                        type = chapterNavigatorType,
                        onNextChapter = onNextChapter,
                        enabledNext = enabledNext,
                        onPreviousChapter = onPreviousChapter,
                        enabledPrevious = enabledPrevious,
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPageIndexChange = onPageIndexChange,
                        onPageIndexChangeFinished = onPageIndexChangeFinished,
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x0EFFFFFF)),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (onClickChapterList != null) {
                            IconButton(onClick = onClickChapterList) {
                                Icon(
                                    imageVector = MaterialSymbols.AutoMirroredRounded.FormatListBulleted,
                                    contentDescription = stringResource(MR.strings.chapters),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        IconButton(onClick = onClickDisplayLayout) {
                            Icon(
                                imageVector = readingMode.icon,
                                contentDescription = stringResource(MR.strings.pref_category_display),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        IconButton(onClick = onClickSettings) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Settings,
                                contentDescription = stringResource(MR.strings.action_settings),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp)
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.pointerInput(Unit) {},
                shape = CircleShape,
                color = Color(0xFF121316),
                border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onClickChapterList != null) {
                        IconButton(onClick = onClickChapterList) {
                            Icon(
                                imageVector = MaterialSymbols.AutoMirroredRounded.FormatListBulleted,
                                contentDescription = stringResource(MR.strings.chapters),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    IconButton(onClick = onClickDisplayLayout) {
                        Icon(
                            imageVector = readingMode.icon,
                            contentDescription = stringResource(MR.strings.pref_category_display),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    IconButton(onClick = onClickSettings) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Settings,
                            contentDescription = stringResource(MR.strings.action_settings),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
