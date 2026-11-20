package eu.kanade.tachiyomi.ui.browse.source.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AdaptiveSheet
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import tachiyomi.core.common.preference.TriState
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.CheckboxItem
import tachiyomi.presentation.core.components.CollapsibleBox
import tachiyomi.presentation.core.components.HeadingItem
import tachiyomi.presentation.core.components.SelectItem
import tachiyomi.presentation.core.components.SortItem
import tachiyomi.presentation.core.components.TextItem
import tachiyomi.presentation.core.components.TriStateItem
import tachiyomi.presentation.core.components.material.Button
import tachiyomi.presentation.core.components.material.ButtonDefaults
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun SourceFilterDialog(
    onDismissRequest: () -> Unit,
    filters: FilterList,
    onReset: () -> Unit,
    onFilter: () -> Unit,
    onUpdate: (FilterList) -> Unit,
) {
    val updateFilters = { onUpdate(filters) }

    AdaptiveSheet(onDismissRequest = onDismissRequest) {
        LazyColumn {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .background(Color(0xFF121316))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onReset) {
                        Text(
                            text = stringResource(MR.strings.action_reset),
                            style = LocalTextStyle.current.copy(
                                color = Color(0xFF8A8C92),
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            onFilter()
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF2F1EC),
                            contentColor = Color(0xFF0A0A0B),
                        ),
                    ) {
                        Text(stringResource(MR.strings.action_filter))
                    }
                }
                HorizontalDivider(color = Color(0x12FFFFFF))
            }

            items(filters) {
                FilterItem(it, updateFilters)
            }
        }
    }
}

@Composable
private fun FilterItem(filter: Filter<*>, onUpdate: () -> Unit) {
    when (filter) {
        is Filter.Header -> {
            HeadingItem(filter.name)
        }
        is Filter.Separator -> {
            HorizontalDivider(color = Color(0x12FFFFFF))
        }
        is Filter.CheckBox -> {
            CheckboxItem(
                label = filter.name,
                checked = filter.state,
            ) {
                filter.state = !filter.state
                onUpdate()
            }
        }
        is Filter.TriState -> {
            TriStateItem(
                label = filter.name,
                state = filter.state.toTriStateFilter(),
            ) {
                filter.state = filter.state.toTriStateFilter().next().toTriStateInt()
                onUpdate()
            }
        }
        is Filter.Text -> {
            TextItem(
                label = filter.name,
                value = filter.state,
            ) {
                filter.state = it
                onUpdate()
            }
        }
        is Filter.Select<*> -> {
            SelectItem(
                label = filter.name,
                options = filter.values,
                selectedIndex = filter.state,
            ) {
                filter.state = it
                onUpdate()
            }
        }
        is Filter.Sort -> {
            CollapsibleBox(
                heading = filter.name,
            ) {
                Column {
                    filter.values.mapIndexed { index, item ->
                        val sortAscending = filter.state?.ascending
                            ?.takeIf { index == filter.state?.index }
                        SortItem(
                            label = item,
                            sortDescending = if (sortAscending != null) !sortAscending else null,
                            onClick = {
                                val ascending = if (index == filter.state?.index) {
                                    !filter.state!!.ascending
                                } else {
                                    filter.state?.ascending ?: true
                                }
                                filter.state = Filter.Sort.Selection(
                                    index = index,
                                    ascending = ascending,
                                )
                                onUpdate()
                            },
                        )
                    }
                }
            }
        }
        is Filter.Group<*> -> {
            CollapsibleBox(
                heading = filter.name,
            ) {
                Column {
                    filter.state
                        .filterIsInstance<Filter<*>>()
                        .map { FilterItem(filter = it, onUpdate = onUpdate) }
                }
            }
        }
    }
}

private fun Int.toTriStateFilter(): TriState {
    return when (this) {
        Filter.TriState.STATE_IGNORE -> TriState.DISABLED
        Filter.TriState.STATE_INCLUDE -> TriState.ENABLED_IS
        Filter.TriState.STATE_EXCLUDE -> TriState.ENABLED_NOT
        else -> TriState.DISABLED
    }
}

private fun TriState.toTriStateInt(): Int {
    return when (this) {
        TriState.DISABLED -> Filter.TriState.STATE_IGNORE
        TriState.ENABLED_IS -> Filter.TriState.STATE_INCLUDE
        TriState.ENABLED_NOT -> Filter.TriState.STATE_EXCLUDE
    }
}
