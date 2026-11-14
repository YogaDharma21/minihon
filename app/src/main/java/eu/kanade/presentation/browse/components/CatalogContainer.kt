package eu.kanade.presentation.browse.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CatalogRowPosition {
    Single,
    First,
    Middle,
    Last;

    companion object {
        fun from(index: Int, totalCount: Int): CatalogRowPosition {
            return when {
                totalCount <= 1 -> Single
                index <= 0 -> First
                index >= totalCount - 1 -> Last
                else -> Middle
            }
        }
    }
}

fun CatalogRowPosition.toShape(cornerRadius: Dp = 14.dp): Shape {
    return when (this) {
        CatalogRowPosition.Single -> RoundedCornerShape(cornerRadius)
        CatalogRowPosition.First -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
        CatalogRowPosition.Middle -> RectangleShape
        CatalogRowPosition.Last -> RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
    }
}

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
fun CatalogRowContainer(
    position: CatalogRowPosition,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = position.toShape()
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = shape,
        color = CatalogContainerDefaults.SurfaceColor,
        border = CatalogContainerDefaults.Border,
    ) {
        val clickableModifier = if (onClick != null || onLongClick != null) {
            Modifier
                .clip(shape)
                .combinedClickable(
                    onClick = { onClick?.invoke() },
                    onLongClick = onLongClick,
                )
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(clickableModifier),
        ) {
            content()

            if (position == CatalogRowPosition.First || position == CatalogRowPosition.Middle) {
                CatalogDivider(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    startIndent = CatalogContainerDefaults.DividerStartIndent,
                    color = CatalogContainerDefaults.DividerColor,
                    thickness = CatalogContainerDefaults.DividerThickness,
                )
            }
        }
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
