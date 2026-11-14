package eu.kanade.presentation.browse.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

object BentoCardDefaults {
    val SurfaceColor = Color(0xFF121316)
    val HairlineBorderColor = Color(0x12FFFFFF)
    val UpdateAccentBorderColor = Color(0x59D2BD93)
    val Shape = RoundedCornerShape(12.dp)

    fun border(hasUpdate: Boolean): BorderStroke = BorderStroke(
        width = 1.dp,
        color = if (hasUpdate) UpdateAccentBorderColor else HairlineBorderColor,
    )
}

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    hasUpdate: Boolean = false,
    shape: Shape = BentoCardDefaults.Shape,
    color: Color = BentoCardDefaults.SurfaceColor,
    border: BorderStroke = BentoCardDefaults.border(hasUpdate),
    icon: @Composable () -> Unit = {},
    action: @Composable () -> Unit = {},
    title: @Composable () -> Unit = {},
    subtitle: @Composable () -> Unit = {},
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
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
                .then(clickableModifier)
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f, fill = false),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    icon()
                }
                Box(
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    action()
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                title()
                subtitle()
            }

            if (content != null) {
                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
        }
    }
}

@Composable
fun BentoCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    hasUpdate: Boolean = false,
    icon: @Composable () -> Unit = {},
    action: @Composable () -> Unit = {},
    metadata: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    BentoCard(
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
        hasUpdate = hasUpdate,
        icon = icon,
        action = action,
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFF4F4F2),
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        },
        subtitle = {
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF8A8C92),
                        fontFeatureSettings = "tnum",
                    ),
                )
            }
            if (metadata != null) {
                metadata()
            }
        },
        content = content,
    )
}
