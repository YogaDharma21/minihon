package eu.kanade.presentation.reader.appbars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import mihon.icons.custommaterialsymbols.CustomMaterialSymbols
import mihon.icons.custommaterialsymbols.rounded.CropOff
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.FormatListBulleted
import mihon.icons.materialsymbols.rounded.Crop
import mihon.icons.materialsymbols.rounded.Settings
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun ReaderBottomBar(
    readingMode: ReadingMode,
    onClickDisplayLayout: () -> Unit,
    onClickSettings: () -> Unit,
    onClickChapterList: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Color(0xFF121316),
        modifier = modifier.drawBehind {
            val strokeWidth = 1.dp.toPx()
            drawLine(
                color = Color(0x12FFFFFF),
                start = Offset(0f, strokeWidth / 2),
                end = Offset(size.width, strokeWidth / 2),
                strokeWidth = strokeWidth,
            )
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 6.dp)
                .pointerInput(Unit) {},
            horizontalArrangement = Arrangement.SpaceAround,
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
