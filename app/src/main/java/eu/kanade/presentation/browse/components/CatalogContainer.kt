package eu.kanade.presentation.browse.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object CatalogContainerDefaults {
    val SurfaceColor = Color(0xFF121316)
    val HairlineBorderColor = Color(0x12FFFFFF)
    val DividerColor = Color(0x0AFFFFFF)
    val Shape = RoundedCornerShape(14.dp)
    val DividerStartIndent: Dp = 58.dp
    val DividerThickness: Dp = 1.dp
    val Border: BorderStroke = BorderStroke(1.dp, HairlineBorderColor)

    @Composable
    fun Divider(
        modifier: Modifier = Modifier,
        startIndent: Dp = DividerStartIndent,
        color: Color = DividerColor,
        thickness: Dp = DividerThickness,
    ) {
        CatalogDivider(
            modifier = modifier,
            startIndent = startIndent,
            color = color,
            thickness = thickness,
        )
    }
}

@Composable
fun CatalogContainer(
    modifier: Modifier = Modifier,
    shape: Shape = CatalogContainerDefaults.Shape,
    color: Color = CatalogContainerDefaults.SurfaceColor,
    border: BorderStroke? = CatalogContainerDefaults.Border,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = color,
        border = border,
    ) {
        val clickableModifier = if (onClick != null || onLongClick != null) {
            Modifier.combinedClickable(
                onClick = { onClick?.invoke() },
                onLongClick = onLongClick,
            )
        } else {
            Modifier
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(clickableModifier),
            content = content,
        )
    }
}

@Composable
fun CatalogDivider(
    modifier: Modifier = Modifier,
    startIndent: Dp = CatalogContainerDefaults.DividerStartIndent,
    color: Color = CatalogContainerDefaults.DividerColor,
    thickness: Dp = CatalogContainerDefaults.DividerThickness,
) {
    HorizontalDivider(
        modifier = modifier.padding(start = startIndent),
        thickness = thickness,
        color = color,
    )
}
