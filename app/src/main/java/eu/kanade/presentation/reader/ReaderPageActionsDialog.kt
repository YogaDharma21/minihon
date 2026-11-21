package eu.kanade.presentation.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.ContentCopy
import mihon.icons.materialsymbols.rounded.Photo
import mihon.icons.materialsymbols.rounded.Save
import mihon.icons.materialsymbols.rounded.Share
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ActionButton
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun ReaderPageActionsDialog(
    onDismissRequest: () -> Unit,
    onSetAsCover: () -> Unit,
    onShare: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    var showSetCoverDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onDismissRequest,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable(
                        interactionSource = null,
                        indication = null,
                        onClick = {},
                    ),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF121316),
                border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        title = stringResource(MR.strings.set_as_cover),
                        icon = MaterialSymbols.Rounded.Photo,
                        onClick = { showSetCoverDialog = true },
                    )
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        title = stringResource(MR.strings.action_copy_to_clipboard),
                        icon = MaterialSymbols.Rounded.ContentCopy,
                        onClick = {
                            onShare(true)
                            onDismissRequest()
                        },
                    )
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        title = stringResource(MR.strings.action_share),
                        icon = MaterialSymbols.Rounded.Share,
                        onClick = {
                            onShare(false)
                            onDismissRequest()
                        },
                    )
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        title = stringResource(MR.strings.action_save),
                        icon = MaterialSymbols.Rounded.Save,
                        onClick = {
                            onSave()
                            onDismissRequest()
                        },
                    )
                }
            }
        }
    }

    if (showSetCoverDialog) {
        SetCoverDialog(
            onConfirm = {
                onSetAsCover()
                showSetCoverDialog = false
            },
            onDismiss = { showSetCoverDialog = false },
        )
    }
}

@Composable
private fun SetCoverDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(MR.strings.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(MR.strings.action_cancel))
            }
        },
        text = {
            Text(stringResource(MR.strings.confirm_set_image_as_cover))
        },
        containerColor = Color(0xFF121316),
        textContentColor = Color(0xFFF4F4F2),
    )
}
