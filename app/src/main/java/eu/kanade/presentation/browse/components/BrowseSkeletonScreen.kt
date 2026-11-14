package eu.kanade.presentation.browse.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object BrowseSkeletonDefaults {
    val MinAlpha: Float = 0.35f
    val MaxAlpha: Float = 0.75f
    val PulseDurationMillis: Int = 900
    val PulseEasing = FastOutSlowInEasing
    val BasePlaceholderColor = Color(0xFF22242B)

    val CardSurfaceColor = Color(0xFF121316)
    val HairlineBorderColor = Color(0x12FFFFFF)
    val CardBorder: BorderStroke = BorderStroke(1.dp, HairlineBorderColor)
    val CardShape = RoundedCornerShape(14.dp)
    val CardPadding: Dp = 12.dp

    val HeaderShape = RoundedCornerShape(4.dp)
    val ChipShape = RoundedCornerShape(9999.dp)
    val AvatarShape = RoundedCornerShape(6.dp)

    val PinnedHeaderWidth: Dp = 140.dp
    val PinnedHeaderHeight: Dp = 16.dp

    val BentoCardCount: Int = 2
    val BentoCardSpacing: Dp = 8.dp
    val BentoAvatarSize: Dp = 36.dp
    val BentoTitleWidth: Dp = 80.dp
    val BentoTitleHeight: Dp = 12.dp
    val BentoSubtitleWidth: Dp = 110.dp
    val BentoSubtitleHeight: Dp = 10.dp

    val FilterChipWidths: List<Dp> = listOf(64.dp, 88.dp, 72.dp, 80.dp, 56.dp)
    val FilterChipHeight: Dp = 32.dp
    val FilterChipInnerBarHeight: Dp = 10.dp

    val CatalogHeaderWidth: Dp = 100.dp
    val CatalogHeaderHeight: Dp = 16.dp

    val CatalogRowCount: Int = 5
    val CatalogRowHeight: Dp = 56.dp
    val CatalogAvatarSize: Dp = 36.dp
    val CatalogTitleWidth: Dp = 120.dp
    val CatalogTitleHeight: Dp = 12.dp
    val CatalogMetadataWidth: Dp = 70.dp
    val CatalogMetadataHeight: Dp = 10.dp
    val CatalogActionWidth: Dp = 52.dp
    val CatalogActionHeight: Dp = 28.dp

    fun placeholderColor(alpha: Float): Color = BasePlaceholderColor.copy(alpha = alpha)
}

@Composable
fun BrowseSkeletonScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val transition = rememberInfiniteTransition(label = "BrowseSkeletonPulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = BrowseSkeletonDefaults.MinAlpha,
        targetValue = BrowseSkeletonDefaults.MaxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = BrowseSkeletonDefaults.PulseDurationMillis,
                easing = BrowseSkeletonDefaults.PulseEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "BrowseSkeletonPulseAlpha",
    )
    val placeholderColor = BrowseSkeletonDefaults.placeholderColor(pulseAlpha)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(
                        width = BrowseSkeletonDefaults.PinnedHeaderWidth,
                        height = BrowseSkeletonDefaults.PinnedHeaderHeight,
                    )
                    .clip(BrowseSkeletonDefaults.HeaderShape)
                    .background(placeholderColor),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(BrowseSkeletonDefaults.BentoCardSpacing),
        ) {
            repeat(BrowseSkeletonDefaults.BentoCardCount) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = BrowseSkeletonDefaults.CardShape,
                    color = BrowseSkeletonDefaults.CardSurfaceColor,
                    border = BrowseSkeletonDefaults.CardBorder,
                ) {
                    Column(
                        modifier = Modifier.padding(BrowseSkeletonDefaults.CardPadding),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(BrowseSkeletonDefaults.BentoAvatarSize)
                                .clip(BrowseSkeletonDefaults.AvatarShape)
                                .background(placeholderColor),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .size(
                                    width = BrowseSkeletonDefaults.BentoTitleWidth,
                                    height = BrowseSkeletonDefaults.BentoTitleHeight,
                                )
                                .clip(BrowseSkeletonDefaults.HeaderShape)
                                .background(placeholderColor),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(
                                    width = BrowseSkeletonDefaults.BentoSubtitleWidth,
                                    height = BrowseSkeletonDefaults.BentoSubtitleHeight,
                                )
                                .clip(BrowseSkeletonDefaults.HeaderShape)
                                .background(placeholderColor),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (chipWidth in BrowseSkeletonDefaults.FilterChipWidths) {
                Surface(
                    modifier = Modifier.size(
                        width = chipWidth,
                        height = BrowseSkeletonDefaults.FilterChipHeight,
                    ),
                    shape = BrowseSkeletonDefaults.ChipShape,
                    color = BrowseSkeletonDefaults.CardSurfaceColor,
                    border = BrowseSkeletonDefaults.CardBorder,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(
                                    width = (chipWidth - 24.dp).coerceAtLeast(16.dp),
                                    height = BrowseSkeletonDefaults.FilterChipInnerBarHeight,
                                )
                                .clip(BrowseSkeletonDefaults.HeaderShape)
                                .background(placeholderColor),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(
                        width = BrowseSkeletonDefaults.CatalogHeaderWidth,
                        height = BrowseSkeletonDefaults.CatalogHeaderHeight,
                    )
                    .clip(BrowseSkeletonDefaults.HeaderShape)
                    .background(placeholderColor),
            )
        }

        CatalogContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            repeat(BrowseSkeletonDefaults.CatalogRowCount) { index ->
                if (index > 0) {
                    CatalogContainerDefaults.Divider()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BrowseSkeletonDefaults.CatalogRowHeight)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(BrowseSkeletonDefaults.CatalogAvatarSize)
                            .clip(BrowseSkeletonDefaults.AvatarShape)
                            .background(placeholderColor),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(
                                    width = BrowseSkeletonDefaults.CatalogTitleWidth,
                                    height = BrowseSkeletonDefaults.CatalogTitleHeight,
                                )
                                .clip(BrowseSkeletonDefaults.HeaderShape)
                                .background(placeholderColor),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(
                                    width = BrowseSkeletonDefaults.CatalogMetadataWidth,
                                    height = BrowseSkeletonDefaults.CatalogMetadataHeight,
                                )
                                .clip(BrowseSkeletonDefaults.HeaderShape)
                                .background(placeholderColor),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(
                                width = BrowseSkeletonDefaults.CatalogActionWidth,
                                height = BrowseSkeletonDefaults.CatalogActionHeight,
                            )
                            .clip(BrowseSkeletonDefaults.ChipShape)
                            .background(placeholderColor),
                    )
                }
            }
        }
    }
}
