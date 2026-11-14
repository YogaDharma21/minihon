package eu.kanade.presentation.browse

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.domain.source.model.Pin
import tachiyomi.domain.source.model.Pins
import tachiyomi.domain.source.model.Source

class SourcesScreenBentoTest {

    private fun createSource(
        id: Long,
        lang: String = "en",
        name: String = "Source $id",
        supportsLatest: Boolean = true,
        isStub: Boolean = false,
        pin: Pins = Pins.unpinned,
        isUsedLast: Boolean = false,
    ): Source = Source(
        id = id,
        lang = lang,
        name = name,
        supportsLatest = supportsLatest,
        isStub = isStub,
        pin = pin,
        isUsedLast = isUsedLast,
    )

    @Test
    fun `pinned source partitioning distinguishes pinned vs unpinned catalog items`() {
        val s1 = createSource(id = 1L, pin = Pins(Pin.Pinned))
        val s2 = createSource(id = 2L, pin = Pins(Pin.Actual))
        val s3 = createSource(id = 3L, pin = Pins(Pin.Pinned, Pin.Actual))
        val s4 = createSource(id = 4L, pin = Pins.unpinned)
        val s5 = createSource(id = 5L, pin = Pins.unpinned, isUsedLast = true)

        isPinnedSource(s1) shouldBe true
        isPinnedSource(s2) shouldBe true
        isPinnedSource(s3) shouldBe true
        isPinnedSource(s4) shouldBe false
        isPinnedSource(s5) shouldBe false

        val partition = partitionSources(listOf(s1, s2, s3, s4, s5))
        partition.pinned shouldContainExactly listOf(s1, s2, s3)
        partition.catalog shouldContainExactly listOf(s4, s5)
        partition.unpinned shouldContainExactly listOf(s4, s5)

        val (pinned, catalog) = partitionSources(listOf(s1, s4))
        pinned shouldContainExactly listOf(s1)
        catalog shouldContainExactly listOf(s4)
    }

    @Test
    fun `pinned engines header typography renders correct count`() {
        formatPinnedEnginesHeader(0) shouldBe "Pinned Engines (0)"
        formatPinnedEnginesHeader(1) shouldBe "Pinned Engines (1)"
        formatPinnedEnginesHeader(4) shouldBe "Pinned Engines (4)"
        formatPinnedEnginesHeader(12) shouldBe "Pinned Engines (12)"
    }

    @Test
    fun `source metadata typography formats dot-separated pinned tags`() {
        formatSourceMetadata("en", "1.4.2") shouldBe "Pinned • EN • v1.4.2"
        formatSourceMetadata("en", "v1.4.2") shouldBe "Pinned • EN • v1.4.2"
        formatSourceMetadata("ja", "2.0.0") shouldBe "Pinned • JA • v2.0.0"
        formatSourceMetadata("all", "1.0.0") shouldBe "Pinned • Multi • v1.0.0"

        formatSourceMetadata("en", null) shouldBe "Pinned • EN"
        formatSourceMetadata(null, "1.4.2") shouldBe "Pinned • v1.4.2"
        formatSourceMetadata(null, null) shouldBe "Pinned"

        formatSourceMetadata("en", "1.4.2", isPinned = false) shouldBe "EN • v1.4.2"
        formatSourceMetadata("en", null, isPinned = false) shouldBe "EN"
        formatSourceMetadata(null, "1.4.2", isPinned = false) shouldBe "v1.4.2"
        formatSourceMetadata(null, null, isPinned = false) shouldBe ""
    }

    @Test
    fun `catalog language header typography formats display name with count`() {
        formatCatalogLanguageHeader("English", 12) shouldBe "English (12)"
        formatCatalogLanguageHeader("Japanese", 1) shouldBe "Japanese (1)"
        formatCatalogLanguageHeader("Multi", 5) shouldBe "Multi (5)"
    }

    @Test
    fun `grouping of sources by language preserves order and clusters by language`() {
        val s1 = createSource(id = 1L, lang = "en", name = "MangaDex")
        val s2 = createSource(id = 2L, lang = "ja", name = "Jump+")
        val s3 = createSource(id = 3L, lang = "en", name = "Webtoons")
        val s4 = createSource(id = 4L, lang = "all", name = "MultiSource")
        val s5 = createSource(id = 5L, lang = "es", name = "TuManga")

        val grouped = groupSourcesByLanguage(listOf(s1, s2, s3, s4, s5))

        grouped.keys.toList() shouldBe listOf("all", "en", "es", "ja")
        grouped["all"]?.map { it.name } shouldBe listOf("MultiSource")
        grouped["en"]?.map { it.name } shouldBe listOf("MangaDex", "Webtoons")
        grouped["es"]?.map { it.name } shouldBe listOf("TuManga")
        grouped["ja"]?.map { it.name } shouldBe listOf("Jump+")
    }
}
