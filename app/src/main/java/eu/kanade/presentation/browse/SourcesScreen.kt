package eu.kanade.presentation.browse

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.browse.components.BentoCard
import eu.kanade.presentation.browse.components.BrowseSkeletonScreen
import eu.kanade.presentation.browse.components.CatalogRowContainer
import eu.kanade.presentation.browse.components.CatalogRowPosition
import eu.kanade.presentation.browse.components.SourceIcon
import eu.kanade.presentation.browse.components.SourceLatestButton
import eu.kanade.presentation.browse.components.SourcePinButton
import eu.kanade.tachiyomi.ui.browse.source.SourcesViewModel
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceViewModel.Listing
import eu.kanade.tachiyomi.util.system.LocaleHelper
import mihon.app.di.appGraph
import tachiyomi.domain.source.model.Pin
import tachiyomi.domain.source.model.Source
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.components.material.topSmallPaddingValues
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.theme.header
import tachiyomi.presentation.core.util.plus
import tachiyomi.source.local.isLocal
import java.util.Locale
import java.util.TreeMap

fun isPinnedSource(source: Source): Boolean {
    return Pin.Pinned in source.pin || Pin.Actual in source.pin
}

data class SourcesPartition(
    val pinned: List<Source>,
    val catalog: List<Source>,
) {
    val unpinned: List<Source> get() = catalog
}

fun partitionSources(sources: List<Source>): SourcesPartition {
    val (pinned, catalog) = sources.partition { isPinnedSource(it) }
    return SourcesPartition(pinned = pinned, catalog = catalog)
}

fun formatPinnedEnginesHeader(count: Int): String {
    return "Pinned Engines ($count)"
}

fun formatCatalogLanguageHeader(language: String, count: Int): String {
    return "$language ($count)"
}

fun formatSourceLanguage(lang: String?, localizedFallback: String? = null): String? {
    if (lang.isNullOrBlank()) return null
    val trimmed = lang.trim()
    return when {
        trimmed.equals("all", ignoreCase = true) -> "Multi"
        trimmed.length in 2..3 -> trimmed.uppercase(Locale.ROOT)
        else -> localizedFallback ?: trimmed
    }
}

fun formatSourceVersion(versionName: String?): String? {
    if (versionName.isNullOrBlank()) return null
    val trimmed = versionName.trim()
    return if (trimmed.startsWith("v", ignoreCase = true)) {
        trimmed
    } else {
        "v$trimmed"
    }
}

fun formatSourceMetadata(
    lang: String?,
    version: String?,
    isPinned: Boolean = true,
): String {
    return buildList {
        if (isPinned) {
            add("Pinned")
        }
        val langTag = formatSourceLanguage(lang)
        if (langTag != null) {
            add(langTag)
        }
        val versionTag = formatSourceVersion(version)
        if (versionTag != null) {
            add(versionTag)
        }
    }.joinToString(" • ")
}

fun groupSourcesByLanguage(sources: List<Source>): Map<String, List<Source>> {
    val comparator = Comparator<String> { d1, d2 ->
        when {
            d1 == "all" && d2 != "all" -> -1
            d2 == "all" && d1 != "all" -> 1
            d1 == "other" && d2 != "other" -> 1
            d2 == "other" && d1 != "other" -> -1
            d1 == "" && d2 != "" -> 1
            d2 == "" && d1 != "" -> -1
            else -> d1.compareTo(d2, ignoreCase = true)
        }
    }
    val map = TreeMap<String, MutableList<Source>>(comparator)
    return sources.groupByTo(map) { it.lang }
}

@Composable
fun SourcesScreen(
    state: SourcesViewModel.State,
    contentPadding: PaddingValues,
    onClickItem: (Source, Listing) -> Unit,
    onClickPin: (Source) -> Unit,
    onLongClickItem: (Source) -> Unit,
) {
    when {
        state.isLoading -> BrowseSkeletonScreen(modifier = Modifier.padding(contentPadding))
        state.isEmpty -> EmptyScreen(
            stringRes = MR.strings.source_empty_screen,
            modifier = Modifier.padding(contentPadding),
        )
        else -> {
            val context = LocalContext.current
            val loadedExtensions by context.appGraph.extensionManager.loadedExtensionsFlow.collectAsState(
                initial = emptyList(),
            )
            val sourceVersions = remember(loadedExtensions) {
                buildMap {
                    for (ext in loadedExtensions) {
                        for (s in ext.sources) {
                            put(s.id, ext.versionName)
                        }
                    }
                }
            }

            val allSources = remember(state.items) {
                state.items.filterIsInstance<SourceUiModel.Item>()
                    .map { it.source }
                    .distinctBy { it.id }
            }
            val (pinnedSources, catalogSources) = remember(allSources) {
                partitionSources(allSources)
            }
            val groupedCatalog = remember(catalogSources) {
                groupSourcesByLanguage(catalogSources)
            }

            ScrollbarLazyColumn(
                contentPadding = contentPadding + topSmallPaddingValues + PaddingValues(bottom = 96.dp),
            ) {
                if (pinnedSources.isNotEmpty()) {
                    item(
                        contentType = "header",
                        key = "pinned-sources-header",
                    ) {
                        SourceHeader(
                            text = formatPinnedEnginesHeader(pinnedSources.size),
                            modifier = Modifier.animateItem(),
                        )
                    }

                    val chunkedPinned = pinnedSources.chunked(2)
                    items(
                        items = chunkedPinned,
                        contentType = { "bento-deck-row" },
                        key = { row -> "bento-row-" + row.joinToString("-") { it.key() } },
                    ) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            for (source in row) {
                                BentoSourceCard(
                                    source = source,
                                    version = sourceVersions[source.id],
                                    onClickItem = onClickItem,
                                    onLongClickItem = onLongClickItem,
                                    modifier = Modifier
                                        .weight(1f)
                                        .animateItem(),
                                )
                            }
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                groupedCatalog.forEach { (lang, sources) ->
                    item(
                        contentType = "header",
                        key = "catalog-header-$lang",
                    ) {
                        val languageDisplayName = LocaleHelper.getSourceDisplayName(lang, context)
                        SourceHeader(
                            text = formatCatalogLanguageHeader(languageDisplayName, sources.size),
                            modifier = Modifier.animateItem(),
                        )
                    }

                    itemsIndexed(
                        items = sources,
                        contentType = { _, _ -> "catalog-source-row" },
                        key = { _, source -> "catalog-source-${source.key()}" },
                    ) { index, source ->
                        CatalogRowContainer(
                            position = CatalogRowPosition.from(index, sources.size),
                            modifier = Modifier.animateItem(),
                        ) {
                            CatalogSourceRow(
                                source = source,
                                version = sourceVersions[source.id],
                                onClickItem = onClickItem,
                                onLongClickItem = onLongClickItem,
                                onClickPin = onClickPin,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = MaterialTheme.padding.small),
        style = MaterialTheme.typography.header.copy(
            color = Color(0xFF8A8C92),
        ),
    )
}

@Composable
private fun BentoSourceCard(
    source: Source,
    version: String?,
    onClickItem: (Source, Listing) -> Unit,
    onLongClickItem: (Source) -> Unit,
    modifier: Modifier = Modifier,
) {
    BentoCard(
        modifier = modifier,
        onClick = { onClickItem(source, Listing.Popular) },
        onLongClick = { onLongClickItem(source) },
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF121316),
        border = BorderStroke(1.dp, Color(0x12FFFFFF)),
        icon = {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF17181C),
                border = BorderStroke(1.dp, Color(0x12FFFFFF)),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    SourceIcon(
                        source = source,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp)),
                    )
                }
            }
        },
        action = {
            if (source.supportsLatest) {
                SourceLatestButton(
                    onClick = { onClickItem(source, Listing.Latest) },
                )
            }
        },
        title = {
            Text(
                text = source.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFF4F4F2),
                    fontWeight = FontWeight.Bold,
                ),
            )
        },
        subtitle = {
            Text(
                text = formatSourceMetadata(source.lang, version),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF8A8C92),
                    fontFeatureSettings = "tnum",
                ),
            )
        },
    )
}

@Composable
private fun CatalogSourceRow(
    source: Source,
    version: String?,
    onClickItem: (Source, Listing) -> Unit,
    onLongClickItem: (Source) -> Unit,
    onClickPin: (Source) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .combinedClickable(
                onClick = { onClickItem(source, Listing.Popular) },
                onLongClick = { onLongClickItem(source) },
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF17181C),
            border = BorderStroke(1.dp, Color(0x12FFFFFF)),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                SourceIcon(
                    source = source,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = source.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFF4F4F2),
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            val metadata = formatSourceMetadata(source.lang, version, isPinned = false)
            if (metadata.isNotBlank()) {
                Text(
                    text = metadata,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF8A8C92),
                        fontFeatureSettings = "tnum",
                    ),
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (source.supportsLatest) {
                SourceLatestButton(
                    onClick = { onClickItem(source, Listing.Latest) },
                )
            }
            SourcePinButton(
                isPinned = isPinnedSource(source),
                onClick = { onClickPin(source) },
            )
        }
    }
}

@Composable
fun SourceOptionsDialog(
    source: Source,
    onClickPin: () -> Unit,
    onClickDisable: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = source.visualName)
        },
        text = {
            Column {
                val textId = if (isPinnedSource(source)) MR.strings.action_unpin else MR.strings.action_pin
                Text(
                    text = stringResource(textId),
                    modifier = Modifier
                        .clickable(onClick = onClickPin)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                )
                if (!source.isLocal()) {
                    Text(
                        text = stringResource(MR.strings.action_disable),
                        modifier = Modifier
                            .clickable(onClick = onClickDisable)
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    )
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {},
    )
}

sealed interface SourceUiModel {
    data class Item(val source: Source) : SourceUiModel
    data class Header(val language: String) : SourceUiModel
}
