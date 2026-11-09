package eu.kanade.presentation.theme

import androidx.compose.ui.graphics.Color
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.domain.ui.model.AppTheme
import eu.kanade.domain.ui.model.ThemeMode
import eu.kanade.presentation.theme.colorscheme.YogaColorScheme
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.base.delegate.ThemingDelegate
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.core.common.preference.InMemoryPreferenceStore

class YogaDesignSystemVerificationTest {

    @Test
    fun `ui preferences defaults to dark theme mode`() {
        val preferences = UiPreferences(InMemoryPreferenceStore())
        preferences.themeMode.get() shouldBe ThemeMode.DARK
    }

    @Test
    fun `theming delegate getThemeResIds returns only Theme_Tachiyomi regardless of parameters`() {
        ThemingDelegate.getThemeResIds() shouldBe listOf(R.style.Theme_Tachiyomi)
        for (theme in AppTheme.entries) {
            ThemingDelegate.getThemeResIds(theme, isAmoled = false) shouldBe listOf(R.style.Theme_Tachiyomi)
            ThemingDelegate.getThemeResIds(theme, isAmoled = true) shouldBe listOf(R.style.Theme_Tachiyomi)
        }
    }

    @Test
    fun `yoga color scheme has dark background, dark card surface, and matte raised surface`() {
        val scheme = YogaColorScheme.getColorScheme()

        scheme.background shouldBe Color(0xFF08090B)
        scheme.surfaceContainer shouldBe Color(0xFF121316)
        scheme.surfaceContainerHigh shouldBe Color(0xFF17181C)
        scheme.surfaceVariant shouldBe Color(0xFF17181C)
    }
}
