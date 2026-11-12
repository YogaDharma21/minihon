package eu.kanade.presentation.browse.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import eu.kanade.tachiyomi.util.system.LocaleHelper
import kotlinx.coroutines.flow.emptyFlow
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Keep
import mihon.icons.materialsymbols.roundedfilled.Keep
import tachiyomi.domain.source.model.Pin
import tachiyomi.domain.source.model.Source
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun BaseSourceItem(
    source: Source,
    modifier: Modifier = Modifier,
    showLanguageInContent: Boolean = true,
    version: String? = null,
    onClickItem: () -> Unit = {},
    onLongClickItem: () -> Unit = {},
    onClickLatest: (() -> Unit)? = null,
    onClickPin: (() -> Unit)? = null,
    icon: @Composable RowScope.(Source) -> Unit = defaultIcon,
    action: @Composable RowScope.(Source) -> Unit = {
        defaultAction(source, onClickLatest, onClickPin)
    },
    content: @Composable RowScope.(Source, String?) -> Unit = { src, lang ->
        defaultContent(src, lang, version)
    },
) {
    val sourceLangString = LocaleHelper.getSourceDisplayName(source.lang, LocalContext.current).takeIf {
        showLanguageInContent
    }
    BaseBrowseItem(
        modifier = modifier,
        onClickItem = onClickItem,
        onLongClickItem = onLongClickItem,
        icon = { icon.invoke(this, source) },
        action = { action.invoke(this, source) },
        content = { content.invoke(this, source, sourceLangString) },
    )
}

private val defaultIcon: @Composable RowScope.(Source) -> Unit = { source ->
    Surface(
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF17181C),
        border = BorderStroke(1.dp, Color(0x12FFFFFF)),
        shadowElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            SourceIcon(
                source = source,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }
    }
}

private val defaultAction: @Composable RowScope.(Source, (() -> Unit)?, (() -> Unit)?) -> Unit = {
        source,
        onClickLatest,
        onClickPin,
    ->
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (onClickLatest != null && source.supportsLatest) {
            SourceLatestButton(onClick = onClickLatest)
        }
        if (onClickPin != null) {
            SourcePinButton(
                isPinned = Pin.Pinned in source.pin,
                onClick = onClickPin,
            )
        }
    }
}

private val defaultContent: @Composable RowScope.(Source, String?, String?) -> Unit = {
        source,
        sourceLangString,
        version,
    ->
    val context = LocalContext.current
    val extensionManager = remember { runCatching { context.appGraph.extensionManager }.getOrNull() }
    val loadedExtensions by (extensionManager?.loadedExtensionsFlow ?: emptyFlow()).collectAsState(
        initial = emptyList(),
    )
    val resolvedVersion = version ?: remember(loadedExtensions, source.id) {
        loadedExtensions.find { ext -> ext.sources.any { it.id == source.id } }?.versionName
    }

    val tags = remember(resolvedVersion, sourceLangString) {
        buildList {
            if (!resolvedVersion.isNullOrBlank()) {
                add(if (resolvedVersion.startsWith("v", ignoreCase = true)) resolvedVersion else "v$resolvedVersion")
            }
            if (!sourceLangString.isNullOrBlank()) {
                add(sourceLangString)
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(start = 14.dp, end = 8.dp)
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = source.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFFF4F4F2),
                fontWeight = FontWeight.Medium,
            ),
        )
        if (tags.isNotEmpty()) {
            Text(
                text = tags.joinToString(" • "),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF8A8C92),
                    fontFeatureSettings = "tnum",
                ),
            )
        }
    }
}

@Composable
fun SourceLatestButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(9999.dp),
        color = Color(0x0AFFFFFF),
        border = BorderStroke(1.dp, Color(0x12FFFFFF)),
    ) {
        Text(
            text = stringResource(MR.strings.latest),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFFF4F4F2),
                fontWeight = FontWeight.Medium,
                fontFeatureSettings = "tnum",
            ),
        )
    }
}

@Composable
fun SourcePinButton(
    isPinned: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = if (isPinned) MaterialSymbols.RoundedFilled.Keep else MaterialSymbols.Rounded.Keep
    val tint = if (isPinned) {
        Color(0xFFF2F1EC)
    } else {
        Color(0xFF8A8C92)
    }
    val description = if (isPinned) MR.strings.action_unpin else MR.strings.action_pin
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        IconButton(
            onClick = onClick,
            modifier = modifier.size(32.dp),
        ) {
            Icon(
                imageVector = icon,
                tint = tint,
                contentDescription = stringResource(description),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
