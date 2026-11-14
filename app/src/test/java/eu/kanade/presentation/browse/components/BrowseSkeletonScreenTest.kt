package eu.kanade.presentation.browse.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BrowseSkeletonScreenTest {

    @Test
    fun `pulse alpha bounds and transition defaults match design specification`() {
        BrowseSkeletonDefaults.MinAlpha shouldBe 0.35f
        BrowseSkeletonDefaults.MaxAlpha shouldBe 0.75f
        BrowseSkeletonDefaults.PulseDurationMillis shouldBe 900
        BrowseSkeletonDefaults.PulseEasing shouldBe FastOutSlowInEasing
        BrowseSkeletonDefaults.BasePlaceholderColor shouldBe Color(0xFF22242B)

        val computed = BrowseSkeletonDefaults.placeholderColor(0.5f)
        computed shouldBe Color(0xFF22242B).copy(alpha = 0.5f)
    }

    @Test
    fun `bento deck skeleton defaults match design specification`() {
        BrowseSkeletonDefaults.CardSurfaceColor shouldBe Color(0xFF121316)
        BrowseSkeletonDefaults.HairlineBorderColor shouldBe Color(0x12FFFFFF)
        BrowseSkeletonDefaults.CardBorder.width shouldBe 1.dp
        (BrowseSkeletonDefaults.CardBorder.brush as SolidColor).value shouldBe
            BrowseSkeletonDefaults.HairlineBorderColor
        BrowseSkeletonDefaults.CardShape shouldBe RoundedCornerShape(14.dp)
        BrowseSkeletonDefaults.CardPadding shouldBe 12.dp

        BrowseSkeletonDefaults.BentoCardCount shouldBe 2
        BrowseSkeletonDefaults.BentoCardSpacing shouldBe 8.dp
        BrowseSkeletonDefaults.BentoAvatarSize shouldBe 36.dp
        BrowseSkeletonDefaults.BentoTitleWidth shouldBe 80.dp
        BrowseSkeletonDefaults.BentoTitleHeight shouldBe 12.dp
        BrowseSkeletonDefaults.BentoSubtitleWidth shouldBe 110.dp
        BrowseSkeletonDefaults.BentoSubtitleHeight shouldBe 10.dp
    }

    @Test
    fun `filter rail skeleton defaults match design specification`() {
        BrowseSkeletonDefaults.FilterChipWidths shouldContainExactly listOf(
            64.dp,
            88.dp,
            72.dp,
            80.dp,
            56.dp,
        )
        BrowseSkeletonDefaults.FilterChipHeight shouldBe 32.dp
        BrowseSkeletonDefaults.FilterChipInnerBarHeight shouldBe 10.dp
        BrowseSkeletonDefaults.ChipShape shouldBe RoundedCornerShape(9999.dp)
    }

    @Test
    fun `section headers skeleton defaults match design specification`() {
        BrowseSkeletonDefaults.PinnedHeaderWidth shouldBe 140.dp
        BrowseSkeletonDefaults.PinnedHeaderHeight shouldBe 16.dp
        BrowseSkeletonDefaults.CatalogHeaderWidth shouldBe 100.dp
        BrowseSkeletonDefaults.CatalogHeaderHeight shouldBe 16.dp
        BrowseSkeletonDefaults.HeaderShape shouldBe RoundedCornerShape(4.dp)
        BrowseSkeletonDefaults.AvatarShape shouldBe RoundedCornerShape(6.dp)
    }

    @Test
    fun `catalog container skeleton defaults match design specification`() {
        BrowseSkeletonDefaults.CatalogRowCount shouldBe 5
        BrowseSkeletonDefaults.CatalogRowHeight shouldBe 56.dp
        BrowseSkeletonDefaults.CatalogAvatarSize shouldBe 36.dp
        BrowseSkeletonDefaults.CatalogTitleWidth shouldBe 120.dp
        BrowseSkeletonDefaults.CatalogTitleHeight shouldBe 12.dp
        BrowseSkeletonDefaults.CatalogMetadataWidth shouldBe 70.dp
        BrowseSkeletonDefaults.CatalogMetadataHeight shouldBe 10.dp
        BrowseSkeletonDefaults.CatalogActionWidth shouldBe 52.dp
        BrowseSkeletonDefaults.CatalogActionHeight shouldBe 28.dp
    }
}
