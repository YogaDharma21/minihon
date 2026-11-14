package eu.kanade.presentation.browse

import androidx.compose.ui.graphics.SolidColor
import eu.kanade.presentation.browse.components.BentoCardDefaults
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BentoDeckTest {

    @Test
    fun `installed engines header renders live count`() {
        formatInstalledEnginesHeader(0) shouldBe "Installed Engines (0)"
        formatInstalledEnginesHeader(4) shouldBe "Installed Engines (4)"
        formatInstalledEnginesHeader(12) shouldBe "Installed Engines (12)"
    }

    @Test
    fun `repo provenance favors badge label and falls back gracefully`() {
        formatRepoProvenance(badgeLabel = "KEI", storeName = "Keiyoushi") shouldBe "KEI"
        formatRepoProvenance(badgeLabel = "", storeName = "Keiyoushi") shouldBe "Keiyoushi"
        formatRepoProvenance(badgeLabel = null, storeName = "Keiyoushi") shouldBe "Keiyoushi"
        formatRepoProvenance(badgeLabel = null, storeName = null) shouldBe "Official"
    }

    @Test
    fun `extension version formatting ensures leading v`() {
        formatExtensionVersion("1.6.38") shouldBe "v1.6.38"
        formatExtensionVersion("v1.6.38") shouldBe "v1.6.38"
        formatExtensionVersion("V2.0") shouldBe "V2.0"
        formatExtensionVersion(null) shouldBe null
        formatExtensionVersion("") shouldBe null
    }

    @Test
    fun `extension language formats multi and short uppercase codes`() {
        formatExtensionLanguage("all") shouldBe "Multi"
        formatExtensionLanguage("ALL") shouldBe "Multi"
        formatExtensionLanguage("en") shouldBe "EN"
        formatExtensionLanguage("ja") shouldBe "JA"
        formatExtensionLanguage("spa") shouldBe "SPA"
        formatExtensionLanguage(null) shouldBe null
        formatExtensionLanguage("") shouldBe null
        formatExtensionLanguage("zh-Hans", localizedFallback = "Chinese (Simplified)") shouldBe "Chinese (Simplified)"
    }

    @Test
    fun `bento deck border highlight matches amber accent on update`() {
        val normalBorder = BentoCardDefaults.border(hasUpdate = false)
        val updateBorder = BentoCardDefaults.border(hasUpdate = true)

        (normalBorder.brush as SolidColor).value shouldBe BentoCardDefaults.HairlineBorderColor
        (updateBorder.brush as SolidColor).value shouldBe BentoCardDefaults.UpdateAccentBorderColor
    }
}
