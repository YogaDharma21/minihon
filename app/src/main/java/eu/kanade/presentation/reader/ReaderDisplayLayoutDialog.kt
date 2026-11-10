package eu.kanade.presentation.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import eu.kanade.domain.manga.model.readerOrientation
import eu.kanade.domain.manga.model.readingMode
import eu.kanade.presentation.components.AdaptiveSheet
import eu.kanade.presentation.reader.components.ModeSelectionDialog
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReaderSettingsViewModel
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import mihon.icons.custommaterialsymbols.CustomMaterialSymbols
import mihon.icons.custommaterialsymbols.rounded.CropOff
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Crop
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.SettingsIconGrid
import tachiyomi.presentation.core.components.material.IconToggleButton
import tachiyomi.presentation.core.i18n.stringResource

private val ReadingModesWithoutDefault = ReadingMode.entries - ReadingMode.DEFAULT
private val ReaderOrientationsWithoutDefault = ReaderOrientation.entries - ReaderOrientation.DEFAULT

@Composable
fun ReaderDisplayLayoutDialog(
    onDismissRequest: () -> Unit,
    viewModel: ReaderSettingsViewModel,
    cropEnabled: Boolean,
    onToggleCropBorder: () -> Unit,
    onChangeReadingModeString: (StringResource) -> Unit,
    onChangeOrientationString: (StringResource) -> Unit,
) {
    val manga by viewModel.mangaFlow.collectAsState()
    val initialReadingMode = remember(manga) {
        ReadingMode.fromPreference(manga?.readingMode?.toInt())
    }
    val initialOrientation = remember(manga) {
        ReaderOrientation.fromPreference(manga?.readerOrientation?.toInt())
    }

    var selectedReadingMode by remember { mutableStateOf(initialReadingMode) }
    var selectedOrientation by remember { mutableStateOf(initialOrientation) }

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
                    .padding(vertical = 16.dp),
            ) {
                Text(
                    text = stringResource(MR.strings.pref_category_display),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Reading Mode section
                SettingsIconGrid(MR.strings.viewer) {
                    items(
                        items = ReadingModesWithoutDefault,
                        key = { it.flagValue },
                    ) { mode ->
                        IconToggleButton(
                            checked = mode == selectedReadingMode,
                            onCheckedChange = {
                                selectedReadingMode = mode
                                viewModel.setReadingMode(mode)
                                onChangeReadingModeString(mode.stringRes)
                            },
                            imageVector = mode.icon,
                            title = stringResource(mode.stringRes),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                HorizontalDivider(
                    color = Color(0x12FFFFFF),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )

                // Orientation section
                SettingsIconGrid(MR.strings.rotation_type) {
                    items(
                        items = ReaderOrientationsWithoutDefault,
                        key = { it.flagValue },
                    ) { ori ->
                        IconToggleButton(
                            checked = ori == selectedOrientation,
                            onCheckedChange = {
                                selectedOrientation = ori
                                viewModel.setOrientation(ori)
                                onChangeOrientationString(ori.stringRes)
                            },
                            imageVector = ori.icon,
                            title = stringResource(ori.stringRes),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                HorizontalDivider(
                    color = Color(0x12FFFFFF),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )

                // Crop Borders Switch Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleCropBorder)
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (cropEnabled) {
                            MaterialSymbols.Rounded.Crop
                        } else {
                            CustomMaterialSymbols.Rounded.CropOff
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(MR.strings.pref_crop_borders),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = cropEnabled,
                        onCheckedChange = { onToggleCropBorder() },
                    )
                }
            }
        }
    }
}
