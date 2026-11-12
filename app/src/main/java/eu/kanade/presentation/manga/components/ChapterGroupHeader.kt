package eu.kanade.presentation.manga.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import tachiyomi.presentation.core.theme.amber
import tachiyomi.presentation.core.theme.borderHairline
import tachiyomi.presentation.core.theme.steelBlue
import tachiyomi.presentation.core.theme.surfaceInset

@Composable
fun ChapterGroupHeader(
    modifier: Modifier = Modifier,
    title: String,
    tag: String,
    isSplit: Boolean,
) {
    val accentColor = if (isSplit) MaterialTheme.colorScheme.amber else MaterialTheme.colorScheme.steelBlue
    val tagBgColor = if (isSplit) {
        MaterialTheme.colorScheme.amber.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceInset
    }
    val tagBorderColor = if (isSplit) {
        MaterialTheme.colorScheme.amber.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.borderHairline
    }
    val tagTextColor = if (isSplit) {
        MaterialTheme.colorScheme.amber
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF17181C),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = accentColor,
                        topLeft = Offset.Zero,
                        size = Size(3.dp.toPx(), size.height),
                    )
                }
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum",
                    letterSpacing = 0.1.em,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = tagBgColor,
                border = BorderStroke(1.dp, tagBorderColor),
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontFeatureSettings = "tnum",
                        fontWeight = FontWeight.Medium,
                    ),
                    color = tagTextColor,
                    maxLines = 1,
                )
            }
        }
    }
}
