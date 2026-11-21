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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CatalogRowPosition {
    Single,
    First,
    Middle,
    Last,
    ;

    val hasTopBorder: Boolean
        get() = this == Single || this == First

    val hasBottomBorder: Boolean
        get() = this == Single || this == Last

    val hasLeftBorder: Boolean
        get() = true

    val hasRightBorder: Boolean
        get() = true

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

private val SingleRowShape14 = RoundedCornerShape(14.dp)
private val FirstRowShape14 = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
private val MiddleRowShape14 = RectangleShape
private val LastRowShape14 = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)

fun CatalogRowPosition.toShape(cornerRadius: Dp = 14.dp): Shape {
    return if (cornerRadius == 14.dp) {
        when (this) {
            CatalogRowPosition.Single -> SingleRowShape14
            CatalogRowPosition.First -> FirstRowShape14
            CatalogRowPosition.Middle -> MiddleRowShape14
            CatalogRowPosition.Last -> LastRowShape14
        }
    } else {
        when (this) {
            CatalogRowPosition.Single -> RoundedCornerShape(cornerRadius)
            CatalogRowPosition.First -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
            CatalogRowPosition.Middle -> RectangleShape
            CatalogRowPosition.Last -> RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
        }
    }
}

private val reusableRowPath = ThreadLocal.withInitial { Path() }

private fun Path.addRowFill(position: CatalogRowPosition, w: Float, h: Float, radius: Float) {
    rewind()
    val d = radius * 2
    when (position) {
        CatalogRowPosition.Single -> {
            moveTo(radius, 0f)
            lineTo(w - radius, 0f)
            arcTo(Rect(w - d, 0f, w, d), 270f, 90f, false)
            lineTo(w, h - radius)
            arcTo(Rect(w - d, h - d, w, h), 0f, 90f, false)
            lineTo(radius, h)
            arcTo(Rect(0f, h - d, d, h), 90f, 90f, false)
            lineTo(0f, radius)
            arcTo(Rect(0f, 0f, d, d), 180f, 90f, false)
        }
        CatalogRowPosition.First -> {
            moveTo(0f, h)
            lineTo(0f, radius)
            arcTo(Rect(0f, 0f, d, d), 180f, 90f, false)
            lineTo(w - radius, 0f)
            arcTo(Rect(w - d, 0f, w, d), 270f, 90f, false)
            lineTo(w, h)
        }
        CatalogRowPosition.Middle -> {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, h)
            lineTo(0f, h)
        }
        CatalogRowPosition.Last -> {
            moveTo(w, 0f)
            lineTo(w, h - radius)
            arcTo(Rect(w - d, h - d, w, h), 0f, 90f, false)
            lineTo(radius, h)
            arcTo(Rect(0f, h - d, d, h), 90f, 90f, false)
            lineTo(0f, 0f)
        }
    }
    close()
}

/**
 * Fills the row and strokes only the perimeter edges its [position] needs, in one draw pass.
 */
fun Modifier.drawCatalogRowBorder(
    position: CatalogRowPosition,
    strokeWidth: Dp = 1.dp,
    color: Color = CatalogContainerDefaults.HairlineBorderColor,
    cornerRadius: Dp = 14.dp,
    fillColor: Color = CatalogContainerDefaults.SurfaceColor,
): Modifier = drawWithContent {
    val w = size.width
    val h = size.height

    if (w <= 0f || h <= 0f) return@drawWithContent

    val stroke = strokeWidth.toPx()
    val halfStroke = stroke / 2f
    val r = cornerRadius.toPx()
    val arcRadius = (r - halfStroke).coerceAtLeast(0f)
    val arcDiameter = 2 * arcRadius
    val cornerOffset = halfStroke + arcRadius
    val path = reusableRowPath.get()

    path.addRowFill(position, w, h, r)
    drawPath(path = path, color = fillColor)

    drawContent()

    when (position) {
        CatalogRowPosition.Single -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(halfStroke, halfStroke),
                size = Size(w - stroke, h - stroke),
                cornerRadius = CornerRadius(arcRadius, arcRadius),
                style = Stroke(width = stroke),
            )
        }
        CatalogRowPosition.First -> {
            path.rewind()
            path.moveTo(halfStroke, h)
            path.lineTo(halfStroke, cornerOffset)
            path.arcTo(
                rect = Rect(halfStroke, halfStroke, halfStroke + arcDiameter, halfStroke + arcDiameter),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
            path.lineTo(w - cornerOffset, halfStroke)
            path.arcTo(
                rect = Rect(w - halfStroke - arcDiameter, halfStroke, w - halfStroke, halfStroke + arcDiameter),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
            path.lineTo(w - halfStroke, h)
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = stroke, cap = StrokeCap.Butt),
            )
        }
        CatalogRowPosition.Middle -> {
            drawLine(
                color = color,
                start = Offset(halfStroke, 0f),
                end = Offset(halfStroke, h),
                strokeWidth = stroke,
                cap = StrokeCap.Butt,
            )
            drawLine(
                color = color,
                start = Offset(w - halfStroke, 0f),
                end = Offset(w - halfStroke, h),
                strokeWidth = stroke,
                cap = StrokeCap.Butt,
            )
        }
        CatalogRowPosition.Last -> {
            path.rewind()
            path.moveTo(w - halfStroke, 0f)
            path.lineTo(w - halfStroke, h - cornerOffset)
            path.arcTo(
                rect = Rect(
                    w - halfStroke - arcDiameter,
                    h - halfStroke - arcDiameter,
                    w - halfStroke,
                    h - halfStroke,
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
            path.lineTo(cornerOffset, h - halfStroke)
            path.arcTo(
                rect = Rect(halfStroke, h - halfStroke - arcDiameter, halfStroke + arcDiameter, h - halfStroke),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
            path.lineTo(halfStroke, 0f)
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = stroke, cap = StrokeCap.Butt),
            )
        }
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
    val clickableModifier = if (onClick != null || onLongClick != null) {
        Modifier
            .combinedClickable(
                onClick = { onClick?.invoke() },
                onLongClick = onLongClick,
            )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .drawCatalogRowBorder(position)
            .clip(shape)
            .then(clickableModifier),
        content = content,
    )
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
