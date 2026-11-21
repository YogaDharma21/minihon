package eu.kanade.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ArrowRight
import mihon.icons.materialsymbols.rounded.RadioButtonChecked
import mihon.icons.materialsymbols.rounded.RadioButtonUnchecked
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import androidx.compose.material3.DropdownMenu as ComposeDropdownMenu
import androidx.compose.material3.DropdownMenuItem as ComposeDropdownMenuItem

/**
 * DropdownMenu matching DESIGN.md floating elevated surface.
 */
@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(8.dp, (-56).dp),
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = Color(0xFF17181C),
    border: BorderStroke? = BorderStroke(1.dp, Color(0x12FFFFFF)),
    shadowElevation: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    ComposeDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.sizeIn(minWidth = 196.dp, maxWidth = 260.dp),
        offset = offset,
        scrollState = scrollState,
        properties = properties,
        shape = shape,
        containerColor = containerColor,
        border = border,
        shadowElevation = shadowElevation,
        content = content,
    )
}

@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(
        textColor = Color(0xFFF4F4F2),
        leadingIconColor = Color(0xFF8A8C92),
        trailingIconColor = Color(0xFF8A8C92),
        disabledTextColor = Color(0xFF5F6065),
        disabledLeadingIconColor = Color(0xFF5F6065),
        disabledTrailingIconColor = Color(0xFF5F6065),
    ),
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
) {
    ComposeDropdownMenuItem(
        text = text,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
    )
}

@Composable
fun RadioMenuItem(
    text: @Composable () -> Unit,
    isChecked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = text,
        onClick = onClick,
        trailingIcon = {
            if (isChecked) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.RadioButtonChecked,
                    contentDescription = stringResource(MR.strings.selected),
                    tint = Color(0xFFF2F1EC),
                )
            } else {
                Icon(
                    imageVector = MaterialSymbols.Rounded.RadioButtonUnchecked,
                    contentDescription = stringResource(MR.strings.not_selected),
                    tint = Color(0xFF8A8C92),
                )
            }
        },
        modifier = modifier,
    )
}

@Composable
fun NestedMenuItem(
    text: @Composable () -> Unit,
    children: @Composable ColumnScope.(() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var nestedExpanded by remember { mutableStateOf(false) }
    val closeMenu = { nestedExpanded = false }

    Box {
        DropdownMenuItem(
            text = text,
            onClick = { nestedExpanded = true },
            trailingIcon = {
                Icon(
                    imageVector = MaterialSymbols.AutoMirroredRounded.ArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF8A8C92),
                )
            },
        )

        DropdownMenu(
            expanded = nestedExpanded,
            onDismissRequest = closeMenu,
            modifier = modifier,
        ) {
            children(closeMenu)
        }
    }
}
