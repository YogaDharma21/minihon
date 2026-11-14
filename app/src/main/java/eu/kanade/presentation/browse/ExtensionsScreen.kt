package eu.kanade.presentation.browse

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import eu.kanade.presentation.browse.components.BentoCard
import eu.kanade.presentation.browse.components.BrowseSkeletonScreen
import eu.kanade.presentation.browse.components.CatalogRowContainer
import eu.kanade.presentation.browse.components.CatalogRowPosition
import eu.kanade.presentation.browse.components.ExtensionIcon
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
import mihon.domain.extension.model.ContentWarning
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
            state.isLoading -> BrowseSkeletonScreen(modifier = Modifier.padding(contentPadding))
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

    val consolidatedItems = remember(state.items) {
        buildMap {
            val updates = state.items.entries.find { (h, _) ->
                h is ExtensionUiModel.Header.Resource && h.textRes == MR.strings.ext_updates_pending
            }?.value.orEmpty()
            val loaded = state.items.entries.find { (h, _) ->
                h is ExtensionUiModel.Header.Resource && h.textRes == MR.strings.ext_installed
            }?.value.orEmpty()

            val allInstalled = (updates + loaded)
                .distinctBy { it.extension.pkgName }
                .sortedWith(
                    compareByDescending<ExtensionUiModel.Item> {
                        (it.extension as? Extension.Installed)?.hasUpdate == true
                    }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.extension.name },
                )

            if (allInstalled.isNotEmpty()) {
                put(ExtensionUiModel.Header.Resource(MR.strings.ext_installed), allInstalled)
            }

            state.items.forEach { (header, items) ->
                if (header is ExtensionUiModel.Header.Resource &&
                    (header.textRes == MR.strings.ext_installed || header.textRes == MR.strings.ext_updates_pending)
                ) {
                    // Handled in allInstalled
                } else {
                    put(header, items)
                }
            }
        }
    }

    val installedItems = remember(consolidatedItems) {
        consolidatedItems[ExtensionUiModel.Header.Resource(MR.strings.ext_installed)].orEmpty()
    }
    val updateItems = remember(installedItems) {
        installedItems.filter { (it.extension as? Extension.Installed)?.hasUpdate == true }
    }
    val updatesCount = updateItems.size

    val catalogEntries = remember(consolidatedItems) {
        consolidatedItems.filterKeys { header ->
            !(header is ExtensionUiModel.Header.Resource && header.textRes == MR.strings.ext_installed)
        }
    }

    val filterChips = remember(catalogEntries, updatesCount) {
        buildExtensionFilterChips(
            catalogEntries = catalogEntries,
            updatesCount = updatesCount,
        )
    }

    var selectedFilterId by remember { mutableStateOf(FILTER_ALL) }
    val effectiveFilterId = if (filterChips.any { it.id == selectedFilterId }) selectedFilterId else FILTER_ALL

    val displayedCatalogEntries = remember(catalogEntries, effectiveFilterId, updateItems) {
        filterCatalogEntries(
            catalogEntries = catalogEntries,
            selectedFilterId = effectiveFilterId,
            updateItems = updateItems,
        )
    }

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

        if (installedItems.isNotEmpty()) {
            item(
                contentType = "header",
                key = "extensionHeader-installed",
            ) {
                val hasAnyUpdate = updateItems.isNotEmpty()
                val action: @Composable RowScope.() -> Unit = if (hasAnyUpdate) {
                    {
                        Surface(
                            onClick = { onClickUpdateAll() },
                            shape = RoundedCornerShape(9999.dp),
                            color = Color(0x1FD2BD93),
                            border = BorderStroke(1.dp, Color(0xFFD2BD93).copy(alpha = 0.25f)),
                        ) {
                            Text(
                                text = stringResource(MR.strings.ext_update_all),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFFD2BD93),
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
                    text = formatInstalledEnginesHeader(installedItems.size),
                    modifier = Modifier.animateItem(),
                    action = action,
                )
            }

            val chunkedItems = installedItems.chunked(2)
            items(
                items = chunkedItems,
                contentType = { "bento-deck-row" },
                key = { row -> "bento-row-" + row.joinToString("-") { it.extension.pkgName } },
            ) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (item in row) {
                        val extension = item.extension
                        val hasUpdate = (extension as? Extension.Installed)?.hasUpdate == true
                        BentoExtensionCard(
                            modifier = Modifier
                                .weight(1f)
                                .animateItem(),
                            item = item,
                            hasUpdate = hasUpdate,
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
                            onOpenExtension = onOpenExtension,
                            onUpdateExtension = onUpdateExtension,
                            onClickItemCancel = onClickItemCancel,
                        )
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (filterChips.isNotEmpty()) {
            item(
                contentType = "filter-rail",
                key = "extension-filter-rail",
            ) {
                ExtensionsFilterRail(
                    chips = filterChips,
                    selectedFilterId = effectiveFilterId,
                    onSelectFilter = { selectedFilterId = it.id },
                    modifier = Modifier
                        .animateItem()
                        .padding(vertical = 8.dp),
                )
            }
        }

        displayedCatalogEntries.forEach { (header, items) ->
            item(
                contentType = "header",
                key = "catalogHeader-${header.hashCode()}",
            ) {
                when (header) {
                    is ExtensionUiModel.Header.Resource -> ExtensionHeader(
                        textRes = header.textRes,
                        modifier = Modifier.animateItem(),
                    )
                    is ExtensionUiModel.Header.Text -> ExtensionHeader(
                        text = header.text,
                        modifier = Modifier.animateItem(),
                    )
                }
            }

            itemsIndexed(
                items = items,
                contentType = { _, _ -> "catalog-row" },
                key = { _, item -> "catalog-ext-${item.extension.key()}" },
            ) { index, item ->
                CatalogRowContainer(
                    position = CatalogRowPosition.from(index, items.size),
                    modifier = Modifier.animateItem(),
                ) {
                    CatalogExtensionRow(
                        item = item,
                        onClickItem = {
                            when (it) {
                                is Extension.Available -> onInstallExtension(it)
                                is Extension.Loaded -> onOpenExtension(it)
                                is Extension.NotLoaded -> notLoadedState = it
                            }
                        },
                        onLongClickItem = onLongClickItem,
                        onClickItemSecondaryAction = {
                            when (it) {
                                is Extension.Available -> onOpenWebView(it)
                                is Extension.Installed -> onUninstallExtension(it)
                            }
                        },
                        onClickItemCancel = onClickItemCancel,
                    )
                }
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

internal fun Extension.key(): String = when (this) {
    is Extension.Available -> "$pkgName-$apkUrl"
    else -> "$pkgName-$versionCode"
}

internal fun formatInstalledEnginesHeader(count: Int): String = "Installed Engines ($count)"

internal fun formatRepoProvenance(badgeLabel: String?, storeName: String?): String {
    return badgeLabel?.ifBlank { storeName } ?: storeName ?: "Official"
}

internal fun formatExtensionVersion(rawVersion: String?): String? {
    return rawVersion?.takeIf { it.isNotBlank() }?.let {
        if (it.startsWith("v", ignoreCase = true)) it else "v$it"
    }
}

internal fun formatExtensionLanguage(lang: String?, localizedFallback: String? = null): String? {
    return when {
        lang.isNullOrBlank() -> null
        lang.equals("all", ignoreCase = true) -> "Multi"
        lang.length <= 3 -> lang.uppercase()
        else -> localizedFallback
    }
}

@Composable
private fun BentoExtensionCard(
    item: ExtensionUiModel.Item,
    hasUpdate: Boolean,
    onClickItem: (Extension) -> Unit,
    onLongClickItem: (Extension) -> Unit,
    onOpenExtension: (Extension.Loaded) -> Unit,
    onUpdateExtension: (Extension.Installed) -> Unit,
    onClickItemCancel: (Extension) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (extension, installStep) = item
    val store = when (extension) {
        is Extension.Available -> extension.store
        is Extension.Installed -> extension.store
    }
    val repoProvenance = formatRepoProvenance(store?.badgeLabel, store?.name)
    val versionTag = formatExtensionVersion(extension.versionName)

    val context = LocalContext.current
    val extLang = extension.lang
    val langLabel = formatExtensionLanguage(
        lang = extLang,
        localizedFallback = extLang?.let { LocaleHelper.getSourceDisplayName(it, context) },
    )

    BentoCard(
        modifier = modifier,
        onClick = { onClickItem(extension) },
        onLongClick = { onLongClickItem(extension) },
        hasUpdate = hasUpdate,
        icon = {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF17181C),
                border = BorderStroke(1.dp, Color(0x12FFFFFF)),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    val idle = installStep.isCompleted()
                    if (!idle) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFF2F1EC),
                        )
                    }

                    val padding by animateDpAsState(
                        targetValue = if (idle) 0.dp else 2.dp,
                        label = "iconPadding",
                    )
                    ExtensionIcon(
                        extension = extension,
                        modifier = Modifier
                            .padding(padding)
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }
        },
        action = {
            if (!installStep.isCompleted()) {
                ExtensionIconButton(
                    icon = MaterialSymbols.Rounded.Close,
                    contentDescription = stringResource(MR.strings.action_cancel),
                    onClick = { onClickItemCancel(extension) },
                    tint = Color(0xFFC99A90),
                )
            } else if (hasUpdate) {
                Surface(
                    onClick = {
                        if (extension is Extension.Installed) {
                            onUpdateExtension(extension)
                        }
                    },
                    shape = RoundedCornerShape(9999.dp),
                    color = Color(0x1FD2BD93),
                    border = BorderStroke(1.dp, Color(0x40D2BD93)),
                ) {
                    Text(
                        text = "UPDATE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFD2BD93),
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum",
                        ),
                    )
                }
            } else {
                ExtensionIconButton(
                    icon = MaterialSymbols.Rounded.Settings,
                    contentDescription = stringResource(MR.strings.action_settings),
                    onClick = {
                        if (extension is Extension.Loaded) {
                            onOpenExtension(extension)
                        } else {
                            onClickItem(extension)
                        }
                    },
                    tint = Color(0xFF8A8C92),
                )
            }
        },
        title = {
            Text(
                text = extension.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFF4F4F2),
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        },
        subtitle = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = repoProvenance,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFA6B0BD),
                        fontWeight = FontWeight.Medium,
                    ),
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (versionTag != null) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5F6065)),
                    )
                    Text(
                        text = versionTag,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF8A8C92),
                            fontFeatureSettings = "tnum",
                        ),
                    )
                }
                if (langLabel != null) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5F6065)),
                    )
                    Text(
                        text = langLabel,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF8A8C92),
                            fontFeatureSettings = "tnum",
                        ),
                    )
                }
            }
        },
    )
}

data class ExtensionFilterChip(
    val id: String,
    val label: String,
    val count: Int,
)

internal const val FILTER_ALL = "all"
internal const val FILTER_UPDATES = "updates"
internal const val FILTER_NSFW = "18+"

internal fun buildExtensionFilterChips(
    catalogEntries: Map<ExtensionUiModel.Header, List<ExtensionUiModel.Item>>,
    updatesCount: Int,
): List<ExtensionFilterChip> {
    val allAvailable = catalogEntries.values.flatten().filter { it.extension is Extension.Available }
    val totalCount = allAvailable.size

    val chips = mutableListOf<ExtensionFilterChip>()

    chips.add(
        ExtensionFilterChip(
            id = FILTER_ALL,
            label = "All ($totalCount)",
            count = totalCount,
        ),
    )

    if (updatesCount > 0) {
        chips.add(
            ExtensionFilterChip(
                id = FILTER_UPDATES,
                label = "Updates ($updatesCount)",
                count = updatesCount,
            ),
        )
    }

    catalogEntries.forEach { (header, items) ->
        if (header is ExtensionUiModel.Header.Text && items.isNotEmpty()) {
            val firstLang = items.firstOrNull()?.extension?.lang
            val isMulti = firstLang.equals("all", ignoreCase = true) ||
                header.text.contains("multi", ignoreCase = true) ||
                header.text.equals("all", ignoreCase = true)
            val displayName = if (isMulti) "Multi" else header.text
            val id = if (isMulti) "lang-multi" else "lang-${firstLang ?: header.text}"
            chips.add(
                ExtensionFilterChip(
                    id = id,
                    label = "$displayName (${items.size})",
                    count = items.size,
                ),
            )
        }
    }

    val nsfwCount = allAvailable.count { it.extension.contentWarning == ContentWarning.NSFW }
    if (nsfwCount > 0) {
        chips.add(
            ExtensionFilterChip(
                id = FILTER_NSFW,
                label = "18+ ($nsfwCount)",
                count = nsfwCount,
            ),
        )
    }

    return chips.distinctBy { it.id }
}

internal fun filterCatalogEntries(
    catalogEntries: Map<ExtensionUiModel.Header, List<ExtensionUiModel.Item>>,
    selectedFilterId: String,
    updateItems: List<ExtensionUiModel.Item> = emptyList(),
): Map<ExtensionUiModel.Header, List<ExtensionUiModel.Item>> {
    return when (selectedFilterId) {
        FILTER_ALL -> catalogEntries
        FILTER_UPDATES -> {
            if (updateItems.isNotEmpty()) {
                mapOf(ExtensionUiModel.Header.Resource(MR.strings.ext_updates_pending) to updateItems)
            } else {
                emptyMap()
            }
        }
        FILTER_NSFW -> {
            buildMap {
                catalogEntries.forEach { (header, items) ->
                    val nsfwItems = items.filter { it.extension.contentWarning == ContentWarning.NSFW }
                    if (nsfwItems.isNotEmpty()) {
                        put(header, nsfwItems)
                    }
                }
            }
        }
        else -> {
            catalogEntries.filter { (header, items) ->
                if (header is ExtensionUiModel.Header.Text && items.isNotEmpty()) {
                    val firstLang = items.firstOrNull()?.extension?.lang
                    val isMulti = firstLang.equals("all", ignoreCase = true) ||
                        header.text.contains("multi", ignoreCase = true) ||
                        header.text.equals("all", ignoreCase = true)
                    val id = if (isMulti) "lang-multi" else "lang-${firstLang ?: header.text}"
                    id == selectedFilterId
                } else {
                    false
                }
            }
        }
    }
}

internal fun formatCatalogMetadata(
    repoProvenance: String,
    langBadge: String?,
    versionTag: String?,
): String {
    return listOfNotNull(
        repoProvenance.takeIf { it.isNotBlank() },
        langBadge?.takeIf { it.isNotBlank() },
        versionTag?.takeIf { it.isNotBlank() },
    ).joinToString(" • ")
}

@Composable
private fun ExtensionsFilterRail(
    chips: List<ExtensionFilterChip>,
    selectedFilterId: String,
    onSelectFilter: (ExtensionFilterChip) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = chips,
            key = { it.id },
        ) { chip ->
            val isSelected = chip.id == selectedFilterId
            Surface(
                onClick = { onSelectFilter(chip) },
                shape = RoundedCornerShape(9999.dp),
                color = if (isSelected) Color(0xFFF2F1EC) else Color(0x0AFFFFFF),
                border = if (isSelected) null else BorderStroke(1.dp, Color(0x12FFFFFF)),
            ) {
                Text(
                    text = chip.label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isSelected) Color(0xFF0A0A0B) else Color(0xFF8A8C92),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        fontFeatureSettings = "tnum",
                    ),
                )
            }
        }
    }
}

@Composable
private fun CatalogExtensionRow(
    item: ExtensionUiModel.Item,
    onClickItem: (Extension) -> Unit,
    onLongClickItem: (Extension) -> Unit,
    onClickItemCancel: (Extension) -> Unit,
    onClickItemSecondaryAction: (Extension) -> Unit,
    modifier: Modifier = Modifier,
    onClickItemAction: (Extension) -> Unit = onClickItem,
) {
    val (extension, installStep) = item
    val store = when (extension) {
        is Extension.Available -> extension.store
        is Extension.Installed -> extension.store
    }
    val repoProvenance = formatRepoProvenance(store?.badgeLabel, store?.name)
    val versionTag = formatExtensionVersion(extension.versionName)
    val context = LocalContext.current
    val extLang = extension.lang
    val langBadge = formatExtensionLanguage(
        lang = extLang,
        localizedFallback = extLang?.let { LocaleHelper.getSourceDisplayName(it, context) },
    )
    val metadataText = formatCatalogMetadata(repoProvenance, langBadge, versionTag)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .combinedClickable(
                onClick = { onClickItem(extension) },
                onLongClick = { onLongClickItem(extension) },
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF17181C),
            border = BorderStroke(1.dp, Color(0x12FFFFFF)),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val idle = installStep.isCompleted()
                if (!idle) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFF2F1EC),
                    )
                }
                val padding by animateDpAsState(
                    targetValue = if (idle) 0.dp else 2.dp,
                    label = "catalogIconPadding",
                )
                ExtensionIcon(
                    extension = extension,
                    modifier = Modifier
                        .padding(padding)
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = extension.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFF4F4F2),
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = metadataText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF8A8C92),
                        fontFeatureSettings = "tnum",
                    ),
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (extension.contentWarning == ContentWarning.NSFW) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0x21C99A90),
                        border = BorderStroke(1.dp, Color(0x40C99A90)),
                    ) {
                        Text(
                            text = "18+",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFC99A90),
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum",
                            ),
                        )
                    }
                }
            }
        }

        CatalogExtensionActions(
            extension = extension,
            installStep = installStep,
            onClickItemCancel = onClickItemCancel,
            onClickItemAction = onClickItemAction,
            onClickItemSecondaryAction = onClickItemSecondaryAction,
        )
    }
}

@Composable
private fun CatalogExtensionActions(
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
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Color(0xFFF2F1EC),
            )
            ExtensionIconButton(
                icon = MaterialSymbols.Rounded.Close,
                contentDescription = stringResource(MR.strings.action_cancel),
                onClick = { onClickItemCancel(extension) },
                tint = Color(0xFFC99A90),
            )
            return@Row
        }

        if (installStep == InstallStep.Error) {
            Surface(
                onClick = { onClickItemAction(extension) },
                shape = RoundedCornerShape(9999.dp),
                color = Color(0x1FC99A90),
                border = BorderStroke(1.dp, Color(0x40C99A90)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFC99A90),
                    )
                    Text(
                        text = stringResource(MR.strings.action_retry),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFC99A90),
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum",
                        ),
                    )
                }
            }
            return@Row
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
                Surface(
                    onClick = { onClickItemAction(extension) },
                    shape = RoundedCornerShape(9999.dp),
                    color = Color(0xFFF2F1EC),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Download,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF0A0A0B),
                        )
                        Text(
                            text = stringResource(MR.strings.ext_install),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF0A0A0B),
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum",
                            ),
                        )
                    }
                }
            }
            is Extension.Installed -> {
                if (extension.hasUpdate) {
                    Surface(
                        onClick = { onClickItemAction(extension) },
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0x1FD2BD93),
                        border = BorderStroke(1.dp, Color(0x40D2BD93)),
                    ) {
                        Text(
                            text = "UPDATE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFD2BD93),
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum",
                            ),
                        )
                    }
                } else if (extension is Extension.Loaded) {
                    ExtensionIconButton(
                        icon = MaterialSymbols.Rounded.Settings,
                        contentDescription = stringResource(MR.strings.action_settings),
                        onClick = { onClickItemSecondaryAction(extension) },
                        tint = Color(0xFF8A8C92),
                    )
                } else if (extension is Extension.NotLoaded) {
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
