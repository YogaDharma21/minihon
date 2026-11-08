package eu.kanade.presentation.theme

import androidx.compose.ui.graphics.Color
import eu.kanade.presentation.theme.colorscheme.YogaColorScheme
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.presentation.core.theme.amber
import tachiyomi.presentation.core.theme.borderHairline
import tachiyomi.presentation.core.theme.crimson
import tachiyomi.presentation.core.theme.emerald
import tachiyomi.presentation.core.theme.steelBlue
import tachiyomi.presentation.core.theme.subtle

class YogaColorSchemeTest {

    @Test
    fun `yoga color scheme matches design tokens`() {
        val scheme = YogaColorScheme.getColorScheme()

        scheme.background shouldBe Color(0xFF08090B)
        scheme.surfaceContainer shouldBe Color(0xFF121316)
        scheme.primary shouldBe Color(0xFFF2F1EC)
    }

    @Test
    fun `light scheme and amoled return identical dark tokens`() {
        val lightScheme = YogaColorScheme.getColorScheme(isDark = false)
        val amoledScheme = YogaColorScheme.getColorScheme(isDark = true, isAmoled = true)

        lightScheme.background shouldBe Color(0xFF08090B)
        lightScheme.surfaceContainer shouldBe Color(0xFF121316)
        lightScheme.primary shouldBe Color(0xFFF2F1EC)

        amoledScheme.background shouldBe Color(0xFF08090B)
        amoledScheme.surfaceContainer shouldBe Color(0xFF121316)
        amoledScheme.primary shouldBe Color(0xFFF2F1EC)
    }

    @Test
    fun `design token extensions return expected values`() {
        val scheme = YogaColorScheme.getColorScheme()

        scheme.emerald shouldBe Color(0xFFA9BFA9)
        scheme.crimson shouldBe Color(0xFFC99A90)
        scheme.amber shouldBe Color(0xFFD2BD93)
        scheme.steelBlue shouldBe Color(0xFFA6B0BD)
        scheme.subtle shouldBe Color(0xFF5F6065)
        scheme.borderHairline shouldBe Color(0xFFFFFFFF).copy(alpha = 0.07f)
    }
}
