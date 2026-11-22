package eu.kanade.presentation.more.settings.screen.data

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.ScrollChromeScaffold
import eu.kanade.presentation.components.WarningBanner
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.backup.BackupFileValidator
import eu.kanade.tachiyomi.data.backup.restore.BackupRestoreWorker
import eu.kanade.tachiyomi.data.backup.restore.RestoreOptions
import eu.kanade.tachiyomi.util.system.DeviceUtil
import eu.kanade.tachiyomi.util.system.workManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.Label
import mihon.icons.materialsymbols.rounded.CollectionsBookmark
import mihon.icons.materialsymbols.rounded.KeyboardArrowDown
import mihon.icons.materialsymbols.rounded.KeyboardArrowUp
import mihon.icons.materialsymbols.rounded.Public
import mihon.icons.materialsymbols.rounded.Settings
import mihon.icons.materialsymbols.roundedfilled.Error
import mihon.icons.materialsymbols.roundedfilled.Extension
import mihon.icons.materialsymbols.roundedfilled.Warning
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

private val CanvasColor = Color(0xFF08090B)
private val CardSurfaceColor = Color(0xFF121316)
private val SurfaceRaisedColor = Color(0xFF17181C)
private val CardBorderColor = Color(0x12FFFFFF)
private val BorderSubtleColor = Color(0x0AFFFFFF)
private val TextForegroundColor = Color(0xFFF4F4F2)
private val TextMutedColor = Color(0xFF8A8C92)
private val TextSubtleColor = Color(0xFF5F6065)
private val PrimaryCtaColor = Color(0xFFF2F1EC)
private val PrimaryCtaTextColor = Color(0xFF0A0A0B)
private val EmeraldAccentColor = Color(0xFFA9BFA9)
private val EmeraldBgColor = Color(0x1FA9BFA9)
private val AmberAccentColor = Color(0xFFD2BD93)
private val AmberBgColor = Color(0x1AD2BD93)
private val AmberBorderColor = Color(0x40D2BD93)
private val CrimsonAccentColor = Color(0xFFC99A90)
private val CrimsonBgColor = Color(0x21C99A90)
private val CrimsonBorderColor = Color(0x59C99A90)
private val SteelBlueAccentColor = Color(0xFFA6B0BD)
private val SteelBlueBgColor = Color(0x1FA6B0BD)

class RestoreBackupScreen(
    private val uri: String,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel =
            assistedMetroViewModel<RestoreBackupViewModel, RestoreBackupViewModel.Factory> { create(uri = uri) }
        val state by viewModel.state.collectAsState()

        ScrollChromeScaffold(
            topBar = {
                AppBar(
                    title = stringResource(MR.strings.pref_restore_backup),
                    navigateUp = navigator::pop,
                    backgroundColor = CanvasColor,
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CanvasColor)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(CardBorderColor),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            onClick = {
                                viewModel.startRestore()
                                navigator.pop()
                            },
                            enabled = state.canRestore && state.options.canRestore(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryCtaColor,
                                contentColor = PrimaryCtaTextColor,
                                disabledContainerColor = SurfaceRaisedColor,
                                disabledContentColor = TextSubtleColor,
                            ),
                        ) {
                            Text(
                                text = stringResource(MR.strings.pref_restore_backup),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            },
            containerColor = CanvasColor,
        ) { contentPadding ->
            val layoutDirection = LocalLayoutDirection.current
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp + contentPadding.calculateStartPadding(layoutDirection),
                    top = contentPadding.calculateTopPadding() + 16.dp,
                    end = 16.dp + contentPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (DeviceUtil.isMiui && DeviceUtil.isMiuiOptimizationDisabled()) {
                    item {
                        WarningBanner(MR.strings.restore_miui_warning)
                    }
                }

                if (!state.canRestore && state.error == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = EmeraldAccentColor,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.5.dp,
                            )
                        }
                    }
                }

                if (state.error != null) {
                    item {
                        when (val err = state.error) {
                            is MissingRestoreComponents -> MissingComponentsCard(err)
                            is InvalidRestore -> InvalidRestoreCard(err)
                            else -> GenericErrorCard(err)
                        }
                    }
                }

                if (state.canRestore) {
                    item {
                        OptionsCard(
                            options = state.options,
                            onToggle = viewModel::toggle,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionsCard(
    options: RestoreOptions,
    onToggle: ((RestoreOptions, Boolean) -> RestoreOptions, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardSurfaceColor)
            .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(18.dp)),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "BACKUP DATA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMutedColor,
                        letterSpacing = 1.2.sp,
                    ),
                )
            }

            RestoreOptions.options.forEachIndexed { index, option ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = BorderSubtleColor,
                        thickness = 1.dp,
                    )
                }

                val isChecked = option.getter(options)
                val visuals = getOptionVisuals(option)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Switch) {
                            onToggle(option.setter, !isChecked)
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceRaisedColor)
                            .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(visuals.iconBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = visuals.icon,
                                contentDescription = null,
                                tint = visuals.iconTint,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    Text(
                        text = stringResource(option.label),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextForegroundColor,
                        ),
                        modifier = Modifier.weight(1f),
                    )

                    Switch(
                        checked = isChecked,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryCtaTextColor,
                            checkedTrackColor = PrimaryCtaColor,
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = TextSubtleColor,
                            uncheckedTrackColor = SurfaceRaisedColor,
                            uncheckedBorderColor = CardBorderColor,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun MissingComponentsCard(
    error: MissingRestoreComponents,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val totalCount = error.sources.size + error.trackers.size
    val isLongList = totalCount > 6

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardSurfaceColor)
            .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmberBgColor)
                        .border(BorderStroke(1.dp, AmberBorderColor), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MaterialSymbols.RoundedFilled.Warning,
                        contentDescription = null,
                        tint = AmberAccentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Text(
                    text = "Missing extensions or trackers",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextForegroundColor,
                    ),
                )
            }

            Text(
                text = stringResource(MR.strings.backup_restore_content_full),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = TextMutedColor,
                ),
            )

            if (error.sources.isNotEmpty()) {
                val visibleSources = if (!isLongList || isExpanded) {
                    error.sources
                } else {
                    error.sources.take(6)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(MR.strings.backup_restore_missing_sources).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SteelBlueAccentColor,
                            letterSpacing = 1.2.sp,
                        ),
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        visibleSources.forEach { sourceName ->
                            MineralBadgeChip(
                                text = sourceName,
                                tintColor = SteelBlueAccentColor,
                            )
                        }
                    }
                }
            }

            if (error.trackers.isNotEmpty()) {
                val remainingSlots = if (!isLongList || isExpanded) {
                    error.trackers.size
                } else {
                    (6 - error.sources.size).coerceAtLeast(0)
                }

                val visibleTrackers = if (!isLongList || isExpanded) {
                    error.trackers
                } else {
                    error.trackers.take(remainingSlots)
                }

                if (visibleTrackers.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(MR.strings.backup_restore_missing_trackers).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldAccentColor,
                                letterSpacing = 1.2.sp,
                            ),
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            visibleTrackers.forEach { trackerName ->
                                MineralBadgeChip(
                                    text = trackerName,
                                    tintColor = EmeraldAccentColor,
                                )
                            }
                        }
                    }
                }
            }

            if (isLongList) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = if (isExpanded) {
                            "Show less"
                        } else {
                            "Show all $totalCount missing"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberAccentColor,
                        ),
                    )
                    Icon(
                        imageVector = if (isExpanded) {
                            MaterialSymbols.Rounded.KeyboardArrowUp
                        } else {
                            MaterialSymbols.Rounded.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = AmberAccentColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MineralBadgeChip(
    text: String,
    tintColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceRaisedColor)
            .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(tintColor),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = tintColor,
                ),
            )
        }
    }
}

@Composable
private fun InvalidRestoreCard(
    error: InvalidRestore,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardSurfaceColor)
            .border(BorderStroke(1.dp, CrimsonBorderColor), RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CrimsonBgColor)
                        .border(BorderStroke(1.dp, CrimsonBorderColor), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MaterialSymbols.RoundedFilled.Error,
                        contentDescription = null,
                        tint = CrimsonAccentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Text(
                    text = stringResource(MR.strings.invalid_backup_file),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextForegroundColor,
                    ),
                )
            }

            if (error.uri != null) {
                SelectionContainer {
                    Text(
                        text = error.uri.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TextMutedColor,
                        ),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CrimsonBgColor)
                    .padding(12.dp),
            ) {
                SelectionContainer {
                    Text(
                        text = error.message,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = CrimsonAccentColor,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun GenericErrorCard(
    error: Any?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardSurfaceColor)
            .border(BorderStroke(1.dp, CrimsonBorderColor), RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        SelectionContainer {
            Text(
                text = error.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = CrimsonAccentColor,
                ),
            )
        }
    }
}

private data class OptionVisuals(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
)

private fun getOptionVisuals(option: RestoreOptions.Entry): OptionVisuals {
    return when (option.label) {
        MR.strings.label_library -> OptionVisuals(
            icon = MaterialSymbols.Rounded.CollectionsBookmark,
            iconTint = EmeraldAccentColor,
            iconBg = EmeraldBgColor,
        )
        MR.strings.categories -> OptionVisuals(
            icon = MaterialSymbols.AutoMirroredRounded.Label,
            iconTint = AmberAccentColor,
            iconBg = AmberBgColor,
        )
        MR.strings.app_settings -> OptionVisuals(
            icon = MaterialSymbols.Rounded.Settings,
            iconTint = SteelBlueAccentColor,
            iconBg = SteelBlueBgColor,
        )
        MR.strings.extensionStores -> OptionVisuals(
            icon = MaterialSymbols.RoundedFilled.Extension,
            iconTint = EmeraldAccentColor,
            iconBg = EmeraldBgColor,
        )
        MR.strings.source_settings -> OptionVisuals(
            icon = MaterialSymbols.Rounded.Public,
            iconTint = SteelBlueAccentColor,
            iconBg = SteelBlueBgColor,
        )
        else -> OptionVisuals(
            icon = MaterialSymbols.Rounded.Settings,
            iconTint = SteelBlueAccentColor,
            iconBg = SteelBlueBgColor,
        )
    }
}

@AssistedInject
class RestoreBackupViewModel(
    @Assisted private val uri: String,
    private val backupFileValidator: BackupFileValidator,
    private val context: Context,
) : ViewModel() {

    val state: StateFlow<RestoreBackupViewModel.State>
        field = MutableStateFlow<RestoreBackupViewModel.State>(State())

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(uri: String): RestoreBackupViewModel
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            validate(uri.toUri())
        }
    }

    fun toggle(setter: (RestoreOptions, Boolean) -> RestoreOptions, enabled: Boolean) {
        state.update {
            it.copy(
                options = setter(it.options, enabled),
            )
        }
    }

    fun startRestore() {
        BackupRestoreWorker.start(
            workManager = context.workManager,
            uri = uri.toUri(),
            options = state.value.options,
        )
    }

    private suspend fun validate(uri: Uri) {
        val results = try {
            backupFileValidator.validate(uri)
        } catch (e: Exception) {
            setError(
                error = InvalidRestore(uri, e.message.toString()),
                canRestore = false,
            )
            return
        }

        if (results.missingSources.isNotEmpty() || results.missingTrackers.isNotEmpty()) {
            setError(
                error = MissingRestoreComponents(uri, results.missingSources, results.missingTrackers),
                canRestore = true,
            )
            return
        }

        setError(error = null, canRestore = true)
    }

    private fun setError(error: Any?, canRestore: Boolean) {
        state.update {
            it.copy(
                error = error,
                canRestore = canRestore,
            )
        }
    }

    @Immutable
    data class State(
        val error: Any? = null,
        val canRestore: Boolean = false,
        val options: RestoreOptions = RestoreOptions(),
    )
}

private data class MissingRestoreComponents(
    val uri: Uri,
    val sources: List<String>,
    val trackers: List<String>,
)

private data class InvalidRestore(
    val uri: Uri? = null,
    val message: String,
)
