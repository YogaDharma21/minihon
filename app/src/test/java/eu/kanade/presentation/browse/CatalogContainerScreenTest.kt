package eu.kanade.presentation.browse

import eu.kanade.tachiyomi.extension.model.Extension
import eu.kanade.tachiyomi.extension.model.InstallStep
import eu.kanade.tachiyomi.ui.browse.extension.ExtensionUiModel
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import mihon.domain.extension.model.ContentWarning
import mihon.domain.extension.model.ExtensionStore
import org.junit.jupiter.api.Test
import tachiyomi.i18n.MR

class CatalogContainerScreenTest {

    private val testStore = ExtensionStore(
        indexUrl = "https://example.com",
        name = "Keiyoushi",
        badgeLabel = "KEI",
        signingKey = "key123",
        contact = ExtensionStore.Contact(website = "https://example.com", discord = null),
        isLegacy = false,
        extensionListUrl = null,
    )

    private fun createAvailable(
        name: String,
        pkgName: String,
        lang: String,
        contentWarning: ContentWarning = ContentWarning.SAFE,
        version: String = "1.4.2",
    ): ExtensionUiModel.Item {
        val ext = Extension.Available(
            name = name,
            pkgName = pkgName,
            versionName = version,
            versionCode = 1L,
            libVersion = 1.4,
            lang = lang,
            contentWarning = contentWarning,
            sources = emptyList(),
            apkUrl = "https://example.com/apk",
            iconUrl = "https://example.com/icon",
            store = testStore,
        )
        return ExtensionUiModel.Item(ext, InstallStep.Idle)
    }

    private fun createInstalled(
        name: String,
        pkgName: String,
        hasUpdate: Boolean = false,
    ): ExtensionUiModel.Item {
        val ext = Extension.Loaded(
            name = name,
            pkgName = pkgName,
            versionName = "1.0.0",
            versionCode = 1L,
            libVersion = 1.4,
            lang = "en",
            contentWarning = ContentWarning.SAFE,
            isShared = false,
            signatures = listOf("key123"),
            pkgFactory = null,
            sources = emptyList(),
            hasUpdate = hasUpdate,
            isObsolete = false,
            store = testStore,
        )
        return ExtensionUiModel.Item(ext, InstallStep.Idle)
    }

    @Test
    fun `formatCatalogMetadata joins provenance lang and version with dots`() {
        formatCatalogMetadata("Keiyoushi", "EN", "v1.4.2") shouldBe "Keiyoushi • EN • v1.4.2"
        formatCatalogMetadata("Official", null, "v2.0") shouldBe "Official • v2.0"
        formatCatalogMetadata("Official", "Multi", null) shouldBe "Official • Multi"
        formatCatalogMetadata("", "EN", "v1.0") shouldBe "EN • v1.0"
    }

    @Test
    fun `buildExtensionFilterChips creates all, updates, language, and 18+ chips`() {
        val multiItem = createAvailable("MangaDex", "dex", "all")
        val enSafeItem = createAvailable("ReadManga", "rm", "en", ContentWarning.SAFE)
        val enNsfwItem = createAvailable("AdultManga", "am", "en", ContentWarning.NSFW)

        val catalogEntries = mapOf<ExtensionUiModel.Header, List<ExtensionUiModel.Item>>(
            ExtensionUiModel.Header.Text("Multi") to listOf(multiItem),
            ExtensionUiModel.Header.Text("English") to listOf(enSafeItem, enNsfwItem),
        )

        val chips = buildExtensionFilterChips(
            catalogEntries = catalogEntries,
            updatesCount = 2,
        )

        chips.map { it.label } shouldContainExactly listOf(
            "All (3)",
            "Updates (2)",
            "Multi (1)",
            "English (2)",
            "18+ (1)",
        )
    }

    @Test
    fun `buildExtensionFilterChips omits updates and 18+ when counts are zero`() {
        val enSafeItem = createAvailable("ReadManga", "rm", "en", ContentWarning.SAFE)

        val catalogEntries = mapOf<ExtensionUiModel.Header, List<ExtensionUiModel.Item>>(
            ExtensionUiModel.Header.Text("English") to listOf(enSafeItem),
        )

        val chips = buildExtensionFilterChips(
            catalogEntries = catalogEntries,
            updatesCount = 0,
        )

        chips.map { it.label } shouldContainExactly listOf(
            "All (1)",
            "English (1)",
        )
    }

    @Test
    fun `filterCatalogEntries returns all when FILTER_ALL is selected`() {
        val item1 = createAvailable("Ext1", "e1", "en")
        val item2 = createAvailable("Ext2", "e2", "ja")
        val entries = mapOf<ExtensionUiModel.Header, List<ExtensionUiModel.Item>>(
            ExtensionUiModel.Header.Text("English") to listOf(item1),
            ExtensionUiModel.Header.Text("Japanese") to listOf(item2),
        )

        val filtered = filterCatalogEntries(entries, FILTER_ALL)
        filtered shouldBe entries
    }

    @Test
    fun `filterCatalogEntries returns pending updates when FILTER_UPDATES is selected`() {
        val updateItem = createInstalled("ExtUpdate", "up1", hasUpdate = true)
        val entries = mapOf<ExtensionUiModel.Header, List<ExtensionUiModel.Item>>(
            ExtensionUiModel.Header.Text("English") to listOf(createAvailable("Ext1", "e1", "en")),
        )

        val filtered = filterCatalogEntries(
            catalogEntries = entries,
            selectedFilterId = FILTER_UPDATES,
            updateItems = listOf(updateItem),
        )

        filtered.keys.single() shouldBe ExtensionUiModel.Header.Resource(MR.strings.ext_updates_pending)
        filtered.values.single() shouldBe listOf(updateItem)
    }

    @Test
    fun `filterCatalogEntries filters by 18+ content rating`() {
        val safeItem = createAvailable("Safe", "s1", "en", ContentWarning.SAFE)
        val nsfwItem = createAvailable("NSFW", "n1", "en", ContentWarning.NSFW)
        val safeJa = createAvailable("SafeJa", "sj", "ja", ContentWarning.SAFE)

        val entries = mapOf<ExtensionUiModel.Header, List<ExtensionUiModel.Item>>(
            ExtensionUiModel.Header.Text("English") to listOf(safeItem, nsfwItem),
            ExtensionUiModel.Header.Text("Japanese") to listOf(safeJa),
        )

        val filtered = filterCatalogEntries(entries, FILTER_NSFW)
        filtered.size shouldBe 1
        filtered.values.single() shouldBe listOf(nsfwItem)
    }

    @Test
    fun `filterCatalogEntries filters by language id`() {
        val enItem = createAvailable("En1", "en1", "en")
        val multiItem = createAvailable("Multi1", "m1", "all")

        val enHeader = ExtensionUiModel.Header.Text("English")
        val multiHeader = ExtensionUiModel.Header.Text("Multi")
        val entries = mapOf<ExtensionUiModel.Header, List<ExtensionUiModel.Item>>(
            multiHeader to listOf(multiItem),
            enHeader to listOf(enItem),
        )

        val filteredEn = filterCatalogEntries(entries, "lang-en")
        filteredEn shouldBe mapOf<ExtensionUiModel.Header, List<ExtensionUiModel.Item>>(enHeader to listOf(enItem))

        val filteredMulti = filterCatalogEntries(entries, "lang-multi")
        filteredMulti shouldBe mapOf<ExtensionUiModel.Header, List<ExtensionUiModel.Item>>(multiHeader to listOf(multiItem))
    }
}
