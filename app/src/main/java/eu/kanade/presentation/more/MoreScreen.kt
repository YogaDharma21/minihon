package eu.kanade.presentation.more

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.settings.widget.SwitchPreferenceWidget
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.tachiyomi.ui.more.DownloadQueueState
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ChromeReaderMode
import mihon.icons.materialsymbols.automirroredrounded.Help
import mihon.icons.materialsymbols.automirroredrounded.Label
import mihon.icons.materialsymbols.rounded.CloudOff
import mihon.icons.materialsymbols.rounded.Code
import mihon.icons.materialsymbols.rounded.CollectionsBookmark
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.Explore
import mihon.icons.materialsymbols.rounded.Eyeglasses2
import mihon.icons.materialsymbols.rounded.Info
import mihon.icons.materialsymbols.rounded.Palette
import mihon.icons.materialsymbols.rounded.QueryStats
import mihon.icons.materialsymbols.rounded.Security
import mihon.icons.materialsymbols.rounded.Storage
import mihon.icons.materialsymbols.rounded.Sync
import mihon.icons.materialsymbols.roundedfilled.VolunteerActivism
import tachiyomi.core.common.Constants
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun MoreScreen(
    downloadQueueStateProvider: () -> DownloadQueueState,
    downloadedOnly: Boolean,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onIncognitoModeChange: (Boolean) -> Unit,
    onClickDownloadQueue: () -> Unit,
    onClickCategories: () -> Unit,
    onClickStats: () -> Unit,
    onClickAppearance: () -> Unit,
    onClickLibrarySettings: () -> Unit,
    onClickReaderSettings: () -> Unit,
    onClickDownloadsSettings: () -> Unit,
    onClickTrackingSettings: () -> Unit,
    onClickBrowseSettings: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSecurity: () -> Unit,
    onClickAdvanced: () -> Unit,
    onClickSupport: () -> Unit,
    onClickAbout: () -> Unit,
    onNavigateUp: (() -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = { scrollBehavior ->
            if (onNavigateUp != null) {
                AppBar(
                    title = stringResource(MR.strings.label_settings),
                    navigateUp = onNavigateUp,
                    scrollBehavior = scrollBehavior,
                )
            }
        },
    ) { contentPadding ->
        ScrollbarLazyColumn(contentPadding = contentPadding) {
            item {
                LogoHeader(
                    iconPadding = PaddingValues(vertical = 32.dp),
                )
            }
            item {
                SwitchPreferenceWidget(
                    title = stringResource(MR.strings.label_downloaded_only),
                    subtitle = stringResource(MR.strings.downloaded_only_summary),
                    icon = MaterialSymbols.Rounded.CloudOff,
                    checked = downloadedOnly,
                    onCheckedChanged = onDownloadedOnlyChange,
                )
            }
            item {
                SwitchPreferenceWidget(
                    title = stringResource(MR.strings.pref_incognito_mode),
                    subtitle = stringResource(MR.strings.pref_incognito_mode_summary),
                    icon = MaterialSymbols.Rounded.Eyeglasses2,
                    checked = incognitoMode,
                    onCheckedChanged = onIncognitoModeChange,
                )
            }

            item { HorizontalDivider() }

            item {
                val downloadQueueState = downloadQueueStateProvider()
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_download_queue),
                    subtitle = when (downloadQueueState) {
                        DownloadQueueState.Stopped -> null
                        is DownloadQueueState.Paused -> {
                            val pending = downloadQueueState.pending
                            if (pending == 0) {
                                stringResource(MR.strings.paused)
                            } else {
                                "${stringResource(MR.strings.paused)} • ${
                                    pluralStringResource(
                                        MR.plurals.download_queue_summary,
                                        count = pending,
                                        pending,
                                    )
                                }"
                            }
                        }
                        is DownloadQueueState.Downloading -> {
                            val pending = downloadQueueState.pending
                            pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                        }
                    },
                    icon = MaterialSymbols.Rounded.Download,
                    onPreferenceClick = onClickDownloadQueue,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.categories),
                    icon = MaterialSymbols.AutoMirroredRounded.Label,
                    onPreferenceClick = onClickCategories,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_stats),
                    icon = MaterialSymbols.Rounded.QueryStats,
                    onPreferenceClick = onClickStats,
                )
            }

            item { HorizontalDivider() }

            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.pref_category_appearance),
                    icon = MaterialSymbols.Rounded.Palette,
                    onPreferenceClick = onClickAppearance,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.pref_category_library),
                    icon = MaterialSymbols.Rounded.CollectionsBookmark,
                    onPreferenceClick = onClickLibrarySettings,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.pref_category_reader),
                    icon = MaterialSymbols.AutoMirroredRounded.ChromeReaderMode,
                    onPreferenceClick = onClickReaderSettings,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.pref_category_downloads),
                    icon = MaterialSymbols.Rounded.Download,
                    onPreferenceClick = onClickDownloadsSettings,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.pref_category_tracking),
                    icon = MaterialSymbols.Rounded.Sync,
                    onPreferenceClick = onClickTrackingSettings,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.browse),
                    icon = MaterialSymbols.Rounded.Explore,
                    onPreferenceClick = onClickBrowseSettings,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_data_storage),
                    icon = MaterialSymbols.Rounded.Storage,
                    onPreferenceClick = onClickDataAndStorage,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.pref_category_security),
                    icon = MaterialSymbols.Rounded.Security,
                    onPreferenceClick = onClickSecurity,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.pref_category_advanced),
                    icon = MaterialSymbols.Rounded.Code,
                    onPreferenceClick = onClickAdvanced,
                )
            }

            item { HorizontalDivider() }

            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_support_us),
                    icon = MaterialSymbols.RoundedFilled.VolunteerActivism,
                    onPreferenceClick = onClickSupport,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.pref_category_about),
                    icon = MaterialSymbols.Rounded.Info,
                    onPreferenceClick = onClickAbout,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_help),
                    icon = MaterialSymbols.AutoMirroredRounded.Help,
                    onPreferenceClick = { uriHandler.openUri(Constants.URL_HELP) },
                )
            }
        }
    }
}
