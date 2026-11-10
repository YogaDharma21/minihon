package eu.kanade.presentation.reader.appbars

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
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
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp)
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF121316),
            border = BorderStroke(1.dp, Color(0x12FFFFFF)),
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .pointerInput(Unit) {},
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
