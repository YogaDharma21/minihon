package eu.kanade.presentation.browse.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CatalogRowPositionTest {

    @Test
    fun `CatalogRowPosition from handles totalCount 0 and 1 as Single`() {
        CatalogRowPosition.from(index = 0, totalCount = 0) shouldBe CatalogRowPosition.Single
        CatalogRowPosition.from(index = -1, totalCount = 0) shouldBe CatalogRowPosition.Single
        CatalogRowPosition.from(index = 1, totalCount = 0) shouldBe CatalogRowPosition.Single

        CatalogRowPosition.from(index = 0, totalCount = 1) shouldBe CatalogRowPosition.Single
        CatalogRowPosition.from(index = -1, totalCount = 1) shouldBe CatalogRowPosition.Single
        CatalogRowPosition.from(index = 1, totalCount = 1) shouldBe CatalogRowPosition.Single
    }

    @Test
    fun `CatalogRowPosition from handles totalCount 2`() {
        CatalogRowPosition.from(index = 0, totalCount = 2) shouldBe CatalogRowPosition.First
        CatalogRowPosition.from(index = 1, totalCount = 2) shouldBe CatalogRowPosition.Last
        CatalogRowPosition.from(index = -1, totalCount = 2) shouldBe CatalogRowPosition.First
        CatalogRowPosition.from(index = 2, totalCount = 2) shouldBe CatalogRowPosition.Last
    }

    @Test
    fun `CatalogRowPosition from handles totalCount 5`() {
        CatalogRowPosition.from(index = 0, totalCount = 5) shouldBe CatalogRowPosition.First
        CatalogRowPosition.from(index = 1, totalCount = 5) shouldBe CatalogRowPosition.Middle
        CatalogRowPosition.from(index = 2, totalCount = 5) shouldBe CatalogRowPosition.Middle
        CatalogRowPosition.from(index = 3, totalCount = 5) shouldBe CatalogRowPosition.Middle
        CatalogRowPosition.from(index = 4, totalCount = 5) shouldBe CatalogRowPosition.Last
    }

    @Test
    fun `CatalogRowPosition from handles totalCount 500`() {
        CatalogRowPosition.from(index = 0, totalCount = 500) shouldBe CatalogRowPosition.First
        CatalogRowPosition.from(index = 1, totalCount = 500) shouldBe CatalogRowPosition.Middle
        CatalogRowPosition.from(index = 249, totalCount = 500) shouldBe CatalogRowPosition.Middle
        CatalogRowPosition.from(index = 498, totalCount = 500) shouldBe CatalogRowPosition.Middle
        CatalogRowPosition.from(index = 499, totalCount = 500) shouldBe CatalogRowPosition.Last
    }

    @Test
    fun `CatalogRowPosition toShape maps correct shapes with default corner radius`() {
        CatalogRowPosition.Single.toShape() shouldBe RoundedCornerShape(14.dp)
        CatalogRowPosition.First.toShape() shouldBe RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
        CatalogRowPosition.Middle.toShape() shouldBe RectangleShape
        CatalogRowPosition.Last.toShape() shouldBe RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
    }

    @Test
    fun `CatalogRowPosition toShape maps correct shapes with custom corner radius`() {
        CatalogRowPosition.Single.toShape(8.dp) shouldBe RoundedCornerShape(8.dp)
        CatalogRowPosition.First.toShape(8.dp) shouldBe RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
        CatalogRowPosition.Middle.toShape(8.dp) shouldBe RectangleShape
        CatalogRowPosition.Last.toShape(8.dp) shouldBe RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
    }

    @Test
    fun `CatalogRowPosition border assertions verify perimeter-only boundaries`() {
        CatalogRowPosition.Single.hasTopBorder shouldBe true
        CatalogRowPosition.Single.hasBottomBorder shouldBe true
        CatalogRowPosition.Single.hasLeftBorder shouldBe true
        CatalogRowPosition.Single.hasRightBorder shouldBe true

        CatalogRowPosition.First.hasTopBorder shouldBe true
        CatalogRowPosition.First.hasBottomBorder shouldBe false
        CatalogRowPosition.First.hasLeftBorder shouldBe true
        CatalogRowPosition.First.hasRightBorder shouldBe true

        CatalogRowPosition.Middle.hasTopBorder shouldBe false
        CatalogRowPosition.Middle.hasBottomBorder shouldBe false
        CatalogRowPosition.Middle.hasLeftBorder shouldBe true
        CatalogRowPosition.Middle.hasRightBorder shouldBe true

        CatalogRowPosition.Last.hasTopBorder shouldBe false
        CatalogRowPosition.Last.hasBottomBorder shouldBe true
        CatalogRowPosition.Last.hasLeftBorder shouldBe true
        CatalogRowPosition.Last.hasRightBorder shouldBe true
    }

    @Test
    fun `Modifier drawCatalogRowBorder attaches modifier across all positions`() {
        CatalogRowPosition.entries.forEach { position ->
            val modifier = Modifier.drawCatalogRowBorder(position)
            modifier.shouldNotBeNull()
        }
    }
}
