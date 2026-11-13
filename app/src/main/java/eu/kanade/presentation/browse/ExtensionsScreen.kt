package eu.kanade.presentation.browse

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.browse.components.BaseBrowseItem
import eu.kanade.presentation.browse.components.ExtensionIcon
import eu.kanade.presentation.browse.components.ExtensionPill
import eu.kanade.presentation.browse.components.label
import eu.kanade.presentation.components.WarningBanner
import eu.kanade.presentation.more.settings.screen.browse.ExtensionStoresScreen
import eu.kanade.presentation.util.rememberRequestPackageInstallsPermissionState
import eu.kanade.tachiyomi.extension.model.Extension
import eu.kanade.tachiyomi.extension.model.InstallStep
import eu.kanade.tachiyomi.ui.browse.extension.ExtensionUiModel
import eu.kanade.tachiyomi.ui.browse.extension.ExtensionsViewModel
import eu.kanade.tachiyomi.util.system.LocaleHelper
import eu.kanade.tachiyomi.util.system.copyToClipboard
import eu.kanade.tachiyomi.util.system.launchRequestPackageInstallsPermission
import mihon.domain.extension.model.ExtensionStore
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Close
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.Info
import mihon.icons.materialsymbols.rounded.Public
import mihon.icons.materialsymbols.rounded.Refresh
import mihon.icons.materialsymbols.rounded.Settings
import mihon.icons.materialsymbols.rounded.VerifiedUser
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.material.PullRefresh
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.components.material.topSmallPaddingValues
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.EmptyScreenAction
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.theme.header
import tachiyomi.presentation.core.util.plus

@Composable
fun ExtensionScreen(
    state: ExtensionsViewModel.State,
    contentPadding: PaddingValues,
    searchQuery: String?,
    onLongClickItem: (Extension) -> Unit,
    onClickItemCancel: (Extension) -> Unit,
    onOpenWebView: (Extension.Available) -> Unit,
    onInstallExtension: (Extension.Available) -> Unit,
    onUninstallExtension: (Extension.Installed) -> Unit,
    onUpdateExtension: (Extension.Installed) -> Unit,
    onTrustExtension: (Extension.NotLoaded) -> Unit,
    onOpenExtension: (Extension.Loaded) -> Unit,
    onClickUpdateAll: () -> Unit,
    onRefresh: () -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow

    PullRefresh(
        refreshing = state.isRefreshing,
        onRefresh = onRefresh,
        enabled = !state.isLoading,
    ) {
        when {
            state.isLoading -> LoadingScreen(Modifier.padding(contentPadding))
            state.isEmpty -> {
                val msg = if (!searchQuery.isNullOrEmpty()) {
                    MR.strings.no_results_found
                } else {
                    MR.strings.empty_screen
                }
                EmptyScreen(
                    stringRes = msg,
                    modifier = Modifier.padding(contentPadding),
                    actions = listOf(
                        EmptyScreenAction(
                            stringRes = MR.strings.extensionStores,
                            icon = MaterialSymbols.Rounded.Settings,
                            onClick = { navigator.push(ExtensionStoresScreen()) },
                        ),
                    ),
                )
            }
            else -> {
                ExtensionContent(
                    state = state,
                    contentPadding = contentPadding,
                    onLongClickItem = onLongClickItem,
                    onClickItemCancel = onClickItemCancel,
                    onOpenWebView = onOpenWebView,
                    onInstallExtension = onInstallExtension,
                    onUninstallExtension = onUninstallExtension,
                    onUpdateExtension = onUpdateExtension,
                    onTrustExtension = onTrustExtension,
                    onOpenExtension = onOpenExtension,
                    onClickUpdateAll = onClickUpdateAll,
                )
            }
        }
    }
}

@Composable
private fun ExtensionContent(
    state: ExtensionsViewModel.State,
    contentPadding: PaddingValues,
    onLongClickItem: (Extension) -> Unit,
    onClickItemCancel: (Extension) -> Unit,
    onOpenWebView: (Extension.Available) -> Unit,
    onInstallExtension: (Extension.Available) -> Unit,
    onUninstallExtension: (Extension.Installed) -> Unit,
    onUpdateExtension: (Extension.Installed) -> Unit,
    onTrustExtension: (Extension.NotLoaded) -> Unit,
    onOpenExtension: (Extension.Loaded) -> Unit,
    onClickUpdateAll: () -> Unit,
) {
    val context = LocalContext.current
    var notLoadedState by remember { mutableStateOf<Extension.NotLoaded?>(null) }
    val installGranted = rememberRequestPackageInstallsPermissionState(initialValue = true)

    FastScrollLazyColumn(
        contentPadding = contentPadding + topSmallPaddingValues + PaddingValues(bottom = 96.dp),
    ) {
        if (!installGranted && state.installer?.requiresSystemPermission == true) {
            item(key = "extension-permissions-warning") {
                WarningBanner(
                    textRes = MR.strings.ext_permission_install_apps_warning,
                    modifier = Modifier.clickable {
                        context.launchRequestPackageInstallsPermission()
                    },
                )
            }
        }

        state.items.forEach { (header, items) ->
            item(
                contentType = "header",
                key = "extensionHeader-${header.hashCode()}",
            ) {
                when (header) {
                    is ExtensionUiModel.Header.Resource -> {
                        val action: @Composable RowScope.() -> Unit =
                            if (header.textRes == MR.strings.ext_updates_pending) {
                                {
                                    Surface(
                                        onClick = { onClickUpdateAll() },
                                        shape = RoundedCornerShape(9999.dp),
                                        color = Color(0xFFF2F1EC),
                                    ) {
                                        Text(
                                            text = stringResource(MR.strings.ext_update_all),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = Color(0xFF0A0A0B),
                                                fontWeight = FontWeight.SemiBold,
                                                fontFeatureSettings = "tnum",
                                            ),
                                        )
                                    }
                                }
                            } else {
                                {}
                            }
                        ExtensionHeader(
                            textRes = header.textRes,
                            modifier = Modifier.animateItem(),
                            action = action,
                        )
                    }
                    is ExtensionUiModel.Header.Text -> {
                        ExtensionHeader(
                            text = header.text,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }

            items(
                items = items,
                contentType = { "item" },
                key = { item ->
                    when (item.extension) {
                        is Extension.NotLoaded -> "extension-not-loaded-${item.hashCode()}"
                        is Extension.Loaded -> "extension-loaded-${item.hashCode()}"
                        is Extension.Available -> "extension-available-${item.hashCode()}"
                    }
                },
            ) { item ->
                ExtensionItem(
                    modifier = Modifier.animateItem(),
                    item = item,
                    onClickItem = {
                        when (it) {
                            is Extension.Available -> onInstallExtension(it)
                            is Extension.Loaded -> onOpenExtension(it)
                            is Extension.NotLoaded -> {
                                notLoadedState = it
                            }
                        }
                    },
                    onLongClickItem = onLongClickItem,
                    onClickItemSecondaryAction = {
                        when (it) {
                            is Extension.Available -> onOpenWebView(it)
                            is Extension.Loaded -> onOpenExtension(it)
                            is Extension.NotLoaded -> {
                                notLoadedState = it
                            }
                        }
                    },
                    onClickItemCancel = onClickItemCancel,
                    onClickItemAction = {
                        when (it) {
                            is Extension.Available -> onInstallExtension(it)
                            is Extension.Loaded -> {
                                if (it.hasUpdate) {
                                    onUpdateExtension(it)
                                } else {
                                    onOpenExtension(it)
                                }
                            }
                            is Extension.NotLoaded -> {
                                if (it.hasUpdate) {
                                    onUpdateExtension(it)
                                } else {
                                    notLoadedState = it
                                }
                            }
                        }
                    },
                )
            }
        }
    }
    notLoadedState?.let { extension ->
        val dismiss = { notLoadedState = null }
        if (extension.reason is Extension.NotLoaded.Reason.Untrusted) {
            ExtensionTrustDialog(
                onClickConfirm = {
                    onTrustExtension(extension)
                    dismiss()
                },
                onClickDismiss = {
                    onUninstallExtension(extension)
                    dismiss()
                },
                onDismissRequest = dismiss,
            )
        } else {
            ExtensionNotLoadedDialog(
                reason = extension.reason,
                onClickUninstall = {
                    onUninstallExtension(extension)
                    dismiss()
                },
                onDismissRequest = dismiss,
            )
        }
    }
}

@Composable
private fun ExtensionItem(
    item: ExtensionUiModel.Item,
    onClickItem: (Extension) -> Unit,
    onLongClickItem: (Extension) -> Unit,
    onClickItemCancel: (Extension) -> Unit,
    onClickItemAction: (Extension) -> Unit,
    onClickItemSecondaryAction: (Extension) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (extension, installStep) = item
    val store = when (extension) {
        is Extension.Available -> extension.store
        is Extension.Installed -> extension.store
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF121316),
        border = BorderStroke(1.dp, Color(0x12FFFFFF)),
    ) {
        BaseBrowseItem(
            onClickItem = { onClickItem(extension) },
            onLongClickItem = { onLongClickItem(extension) },
            icon = {
                ExtensionItemIcon(extension = extension, installStep = installStep)
            },
            action = {
                ExtensionItemActions(
                    extension = extension,
                    installStep = installStep,
                    onClickItemCancel = onClickItemCancel,
                    onClickItemAction = onClickItemAction,
                    onClickItemSecondaryAction = onClickItemSecondaryAction,
                )
            },
        ) {
            ExtensionItemContent(
                extension = extension,
                store = store,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ExtensionItemContent(
    extension: Extension,
    store: ExtensionStore?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(start = 14.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = extension.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFFF4F4F2),
                fontWeight = FontWeight.Bold,
            ),
        )

        ExtensionItemMetadata(
            extension = extension,
            store = store,
        )
    }
}

@Composable
private fun ExtensionItemIcon(
    extension: Extension,
    installStep: InstallStep,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF17181C),
        border = BorderStroke(1.dp, Color(0x12FFFFFF)),
        shadowElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val idle = installStep.isCompleted()
            if (!idle) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFFF2F1EC),
                )
            }

            val padding by animateDpAsState(
                targetValue = if (idle) 0.dp else 4.dp,
                label = "iconPadding",
            )
            ExtensionIcon(
                extension = extension,
                modifier = Modifier
                    .padding(padding)
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }
    }
}

@Composable
private fun ExtensionItemMetadata(
    extension: Extension,
    store: ExtensionStore?,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        val repoProvenance = store?.badgeLabel?.ifBlank { store.name }
            ?: store?.name
            ?: "Official"
        ExtensionPill(
            text = repoProvenance,
            color = Color(0xFFA6B0BD),
            backgroundColor = Color(0x1FA6B0BD),
        )

        val lang = extension.lang?.takeIf { it.isNotBlank() }
        val langBadge = lang?.let { LocaleHelper.getSourceDisplayName(it, LocalContext.current) }
        if (langBadge != null) {
            ExtensionPill(
                text = langBadge,
                color = Color(0xFF8A8C92),
                backgroundColor = Color(0x14FFFFFF),
            )
        }

        val rawVersion = extension.versionName.takeIf { it.isNotBlank() }
        val versionTag = rawVersion?.let {
            if (it.startsWith("v", ignoreCase = true)) it else "v$it"
        }
        if (versionTag != null) {
            ExtensionPill(
                text = versionTag,
                color = Color(0xFF8A8C92),
                backgroundColor = Color(0x14FFFFFF),
            )
        }

        extension.contentWarning.label?.let {
            ExtensionPill(
                text = stringResource(it.title),
                color = it.color,
                backgroundColor = it.color.copy(alpha = 0.12f),
            )
        }

        (extension as? Extension.Loaded)
            ?.takeIf { !it.isShared }
            ?.let {
                ExtensionPill(
                    text = stringResource(MR.strings.ext_installer_private),
                    color = Color(0xFF8A8C92),
                    backgroundColor = Color(0x14FFFFFF),
                )
            }
    }
}

@Composable
private fun ExtensionStatusPill(
    text: String,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        shape = RoundedCornerShape(9999.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                color = color,
                fontWeight = FontWeight.Medium,
                fontFeatureSettings = "tnum",
            ),
        )
    }
}

@Composable
private fun ExtensionIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF8A8C92),
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        IconButton(
            onClick = onClick,
            modifier = modifier.size(32.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun ExtensionItemActions(
    extension: Extension,
    installStep: InstallStep,
    modifier: Modifier = Modifier,
    onClickItemCancel: (Extension) -> Unit = {},
    onClickItemAction: (Extension) -> Unit = {},
    onClickItemSecondaryAction: (Extension) -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!installStep.isCompleted()) {
            val stepLabel = when (installStep) {
                InstallStep.Pending -> MR.strings.ext_pending
                InstallStep.Downloading -> MR.strings.ext_downloading
                InstallStep.Installing -> MR.strings.ext_installing
                else -> null
            }
            if (stepLabel != null) {
                ExtensionStatusPill(
                    text = stringResource(stepLabel),
                    color = Color(0xFFD2BD93),
                    backgroundColor = Color(0x1FD2BD93),
                )
            }
            ExtensionIconButton(
                icon = MaterialSymbols.Rounded.Close,
                contentDescription = stringResource(MR.strings.action_cancel),
                onClick = { onClickItemCancel(extension) },
                tint = Color(0xFFC99A90),
            )
            return@Row
        }

        if (installStep == InstallStep.Error) {
            ExtensionStatusPill(
                text = stringResource(MR.strings.action_retry),
                color = Color(0xFFC99A90),
                backgroundColor = Color(0x1FC99A90),
                onClick = { onClickItemAction(extension) },
            )
            ExtensionIconButton(
                icon = MaterialSymbols.Rounded.Refresh,
                contentDescription = stringResource(MR.strings.action_retry),
                onClick = { onClickItemAction(extension) },
                tint = Color(0xFFC99A90),
            )
            return@Row
        }

        when {
            extension is Extension.Installed && extension.hasUpdate -> {
                ExtensionStatusPill(
                    text = stringResource(MR.strings.ext_update),
                    color = Color(0xFFD2BD93),
                    backgroundColor = Color(0x1FD2BD93),
                    onClick = { onClickItemAction(extension) },
                )
            }
            extension is Extension.Loaded && extension.isObsolete -> {
                ExtensionStatusPill(
                    text = stringResource(MR.strings.ext_obsolete),
                    color = Color(0xFFC99A90),
                    backgroundColor = Color(0x1FC99A90),
                )
            }
            extension is Extension.Loaded -> {
                ExtensionStatusPill(
                    text = stringResource(MR.strings.ext_installed),
                    color = Color(0xFFA9BFA9),
                    backgroundColor = Color(0x1FA9BFA9),
                )
            }
            extension is Extension.NotLoaded -> {
                val label = extension.reason.labelRes ?: MR.strings.ext_untrusted
                ExtensionStatusPill(
                    text = stringResource(label),
                    color = Color(0xFFC99A90),
                    backgroundColor = Color(0x1FC99A90),
                    onClick = if (extension.reason is Extension.NotLoaded.Reason.Untrusted) {
                        { onClickItemSecondaryAction(extension) }
                    } else {
                        null
                    },
                )
            }
        }

        when (extension) {
            is Extension.Available -> {
                if (extension.sources.isNotEmpty()) {
                    ExtensionIconButton(
                        icon = MaterialSymbols.Rounded.Public,
                        contentDescription = stringResource(MR.strings.action_open_in_web_view),
                        onClick = { onClickItemSecondaryAction(extension) },
                        tint = Color(0xFF8A8C92),
                    )
                }
                ExtensionIconButton(
                    icon = MaterialSymbols.Rounded.Download,
                    contentDescription = stringResource(MR.strings.ext_install),
                    onClick = { onClickItemAction(extension) },
                    tint = Color(0xFFF2F1EC),
                )
            }
            is Extension.Loaded -> {
                ExtensionIconButton(
                    icon = MaterialSymbols.Rounded.Settings,
                    contentDescription = stringResource(MR.strings.action_settings),
                    onClick = { onClickItemSecondaryAction(extension) },
                    tint = Color(0xFF8A8C92),
                )
            }
            is Extension.NotLoaded -> {
                if (extension.reason is Extension.NotLoaded.Reason.Untrusted) {
                    ExtensionIconButton(
                        icon = MaterialSymbols.Rounded.VerifiedUser,
                        contentDescription = stringResource(MR.strings.ext_trust),
                        onClick = { onClickItemSecondaryAction(extension) },
                        tint = Color(0xFFD2BD93),
                    )
                } else {
                    ExtensionIconButton(
                        icon = MaterialSymbols.Rounded.Info,
                        contentDescription = stringResource(MR.strings.ext_not_loaded_details),
                        onClick = { onClickItemSecondaryAction(extension) },
                        tint = Color(0xFF8A8C92),
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtensionHeader(
    textRes: StringResource,
    modifier: Modifier = Modifier,
    action: @Composable RowScope.() -> Unit = {},
) {
    ExtensionHeader(
        text = stringResource(textRes),
        modifier = modifier,
        action = action,
    )
}

@Composable
private fun ExtensionHeader(
    text: String,
    modifier: Modifier = Modifier,
    action: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier.padding(horizontal = 24.dp, vertical = MaterialTheme.padding.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .weight(1f),
            style = MaterialTheme.typography.header.copy(
                color = Color(0xFF8A8C92),
            ),
        )
        action()
    }
}

/**
 * Only the reasons the user can act on are worth naming in the row; the rest all mean "broken" to
 * them and are spelled out in [ExtensionNotLoadedDialog] instead.
 */
private val Extension.NotLoaded.Reason.labelRes: StringResource?
    get() = when (this) {
        is Extension.NotLoaded.Reason.Untrusted -> MR.strings.ext_untrusted
        Extension.NotLoaded.Reason.Filtered -> MR.strings.ext_filtered
        // The section header already says these aren't loaded; the dialog says why
        Extension.NotLoaded.Reason.Unsigned,
        Extension.NotLoaded.Reason.UnsupportedLibVersion,
        Extension.NotLoaded.Reason.Malformed,
        is Extension.NotLoaded.Reason.Failed,
        -> null
    }

private val Extension.NotLoaded.Reason.descriptionRes: StringResource
    get() = when (this) {
        is Extension.NotLoaded.Reason.Untrusted -> MR.strings.untrusted_extension_message
        Extension.NotLoaded.Reason.Filtered -> MR.strings.ext_filtered_message
        Extension.NotLoaded.Reason.Unsigned -> MR.strings.ext_unsigned_message
        Extension.NotLoaded.Reason.UnsupportedLibVersion -> MR.strings.ext_unsupported_message
        Extension.NotLoaded.Reason.Malformed -> MR.strings.ext_malformed_message
        is Extension.NotLoaded.Reason.Failed -> MR.strings.ext_load_failed_message
    }

@Composable
private fun ExtensionNotLoadedDialog(
    reason: Extension.NotLoaded.Reason,
    onClickUninstall: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(MR.strings.ext_not_loaded_dialog))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small)) {
                Text(text = stringResource(reason.descriptionRes))

                if (reason is Extension.NotLoaded.Reason.Failed) {
                    Text(
                        text = reason.message,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    val context = LocalContext.current
                    TextButton(
                        onClick = {
                            context.copyToClipboard(
                                label = context.stringResource(MR.strings.ext_copy_stacktrace),
                                content = reason.stackTrace,
                            )
                        },
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(text = stringResource(MR.strings.ext_copy_stacktrace))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onClickUninstall) {
                Text(text = stringResource(MR.strings.ext_uninstall))
            }
        },
        onDismissRequest = onDismissRequest,
    )
}

@Composable
private fun ExtensionTrustDialog(
    onClickConfirm: () -> Unit,
    onClickDismiss: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(MR.strings.untrusted_extension))
        },
        text = {
            Text(text = stringResource(MR.strings.untrusted_extension_message))
        },
        confirmButton = {
            TextButton(onClick = onClickConfirm) {
                Text(text = stringResource(MR.strings.ext_trust))
            }
        },
        dismissButton = {
            TextButton(onClick = onClickDismiss) {
                Text(text = stringResource(MR.strings.ext_uninstall))
            }
        },
        onDismissRequest = onDismissRequest,
    )
}
