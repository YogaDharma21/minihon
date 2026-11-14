package eu.kanade.presentation.browse.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BentoEngineBayPrimitivesTest {

    @Test
    fun `bento card defaults match design specification`() {
        BentoCardDefaults.SurfaceColor shouldBe Color(0xFF121316)
        BentoCardDefaults.HairlineBorderColor shouldBe Color(0x12FFFFFF)
        BentoCardDefaults.UpdateAccentBorderColor shouldBe Color(0x59D2BD93)
        BentoCardDefaults.Shape shouldBe RoundedCornerShape(12.dp)
    }

    @Test
    fun `bento card border updates correctly on update availability`() {
        val normalBorder = BentoCardDefaults.border(hasUpdate = false)
        normalBorder.width shouldBe 1.dp

        val updateBorder = BentoCardDefaults.border(hasUpdate = true)
        updateBorder.width shouldBe 1.dp
    }

    @Test
    fun `catalog container defaults match design specification`() {
        CatalogContainerDefaults.SurfaceColor shouldBe Color(0xFF121316)
        CatalogContainerDefaults.HairlineBorderColor shouldBe Color(0x12FFFFFF)
        CatalogContainerDefaults.DividerColor shouldBe Color(0x0AFFFFFF)
        CatalogContainerDefaults.Shape shouldBe RoundedCornerShape(14.dp)
        CatalogContainerDefaults.DividerStartIndent shouldBe 58.dp
        CatalogContainerDefaults.DividerThickness shouldBe 1.dp
    }
}
