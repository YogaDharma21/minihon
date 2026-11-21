package eu.kanade.presentation.reader.appbars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.reader.components.ChapterNavigator
import eu.kanade.presentation.reader.components.ChapterNavigatorType
import eu.kanade.presentation.theme.appBarFadeAnimationSpec
import eu.kanade.presentation.theme.appBarSlideAnimationSpec
import eu.kanade.presentation.theme.bottomAppBarEnterTransition
import eu.kanade.presentation.theme.bottomAppBarExitTransition
import eu.kanade.presentation.theme.topAppBarEnterTransition
import eu.kanade.presentation.theme.topAppBarExitTransition
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import tachiyomi.presentation.core.components.material.padding

@Composable
fun ReaderAppBars(
    visible: Boolean,

    mangaTitle: String?,
    chapterTitle: String?,
    navigateUp: () -> Unit,
    onClickTopAppBar: () -> Unit,
    bookmarked: Boolean,
    onToggleBookmarked: () -> Unit,
    onOpenInWebView: (() -> Unit)?,
    onOpenInBrowser: (() -> Unit)?,
    onShare: (() -> Unit)?,

    chapterNavigatorType: ChapterNavigatorType,
    verticalNavigatorHeight: Float,
    onNextChapter: () -> Unit,
    enabledNext: Boolean,
    onPreviousChapter: () -> Unit,
    enabledPrevious: Boolean,
    currentPage: Int,
    totalPages: Int,
    onPageIndexChange: (Int) -> Unit,
    onPageIndexChangeFinished: () -> Unit,

    readingMode: ReadingMode,
    onClickDisplayLayout: () -> Unit,
    onClickSettings: () -> Unit,
    onClickChapterList: (() -> Unit)? = null,
) {
    val backgroundColor = Color(0xFF121316)

    Column(modifier = Modifier.fillMaxHeight()) {
        AnimatedVisibility(
            visible = visible,
            enter = topAppBarEnterTransition,
            exit = topAppBarExitTransition,
        ) {
            ReaderTopBar(
                modifier = Modifier
                    .clickable(onClick = onClickChapterList ?: onClickTopAppBar),
                mangaTitle = mangaTitle,
                chapterTitle = chapterTitle,
                navigateUp = navigateUp,
                bookmarked = bookmarked,
                onToggleBookmarked = onToggleBookmarked,
                onOpenInWebView = onOpenInWebView,
                onOpenInBrowser = onOpenInBrowser,
                onShare = onShare,
                onClickChapterList = onClickChapterList,
            )
        }

        if (!chapterNavigatorType.isHorizontal()) {
            val sliderOnLeft = chapterNavigatorType == ChapterNavigatorType.VERTICAL_LEFT
            CompositionLocalProvider(
                LocalLayoutDirection provides if (sliderOnLeft) LayoutDirection.Ltr else LayoutDirection.Rtl,
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInHorizontally(appBarSlideAnimationSpec) { if (sliderOnLeft) -it else it } +
                            fadeIn(appBarFadeAnimationSpec),
                        exit = slideOutHorizontally(appBarSlideAnimationSpec) { if (sliderOnLeft) -it else it } +
                            fadeOut(appBarFadeAnimationSpec),
                    ) {
                        Row {
                            Spacer(modifier = Modifier.width(MaterialTheme.padding.small))
                            Box(
                                modifier = Modifier.fillMaxHeight(),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                ChapterNavigator(
                                    modifier = Modifier.fillMaxHeight(verticalNavigatorHeight),
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
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        AnimatedVisibility(
            visible = visible,
            enter = bottomAppBarEnterTransition,
            exit = bottomAppBarExitTransition,
        ) {
            ReaderBottomBar(
                modifier = Modifier.fillMaxWidth(),
                readingMode = readingMode,
                onClickDisplayLayout = onClickDisplayLayout,
                onClickSettings = onClickSettings,
                onClickChapterList = onClickChapterList,
                chapterNavigatorType = chapterNavigatorType,
                onNextChapter = onNextChapter,
                enabledNext = enabledNext,
                onPreviousChapter = onPreviousChapter,
                enabledPrevious = enabledPrevious,
                currentPage = currentPage,
                totalPages = totalPages,
                onPageIndexChange = onPageIndexChange,
                onPageIndexChangeFinished = onPageIndexChangeFinished,
            )
        }
    }
}
