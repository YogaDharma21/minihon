package eu.kanade.presentation.manga

import androidx.compose.ui.unit.dp
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MangaDetailAssemblyTest {

    @Test
    fun `detail assembly constants match specification`() {
        DETAIL_ASSEMBLY_DURATION shouldBe 800

        TOP_BAR_START_TRANSLATION_Y shouldBe (-24).dp
        COVER_START_TRANSLATION_X shouldBe (-40).dp
        COVER_START_SCALE shouldBe 0.92f
        TITLES_START_TRANSLATION_X shouldBe 36.dp
        ACTION_BUTTONS_START_TRANSLATION_Y shouldBe 20.dp
        ACTION_BUTTONS_START_SCALE shouldBe 0.95f
        DESCRIPTION_START_TRANSLATION_Y shouldBe 16.dp
        CHAPTERS_START_TRANSLATION_Y shouldBe 28.dp
    }

    @Test
    fun `all staggered animations finish within 800ms timeline`() {
        (TOP_BAR_DELAY + TOP_BAR_DURATION) shouldBe 450
        (COVER_DELAY + COVER_DURATION) shouldBe 610
        (TITLES_DELAY + TITLES_DURATION) shouldBe 670
        (ACTION_BUTTONS_DELAY + ACTION_BUTTONS_DURATION) shouldBe 680
        (DESCRIPTION_DELAY + DESCRIPTION_DURATION) shouldBe 740
        (CHAPTERS_DELAY + CHAPTERS_DURATION) shouldBe 800
        (FAB_DELAY + FAB_ALPHA_DURATION) shouldBe 700

        val maxEnd = maxOf(
            TOP_BAR_DELAY + TOP_BAR_DURATION,
            COVER_DELAY + COVER_DURATION,
            TITLES_DELAY + TITLES_DURATION,
            ACTION_BUTTONS_DELAY + ACTION_BUTTONS_DURATION,
            DESCRIPTION_DELAY + DESCRIPTION_DURATION,
            CHAPTERS_DELAY + CHAPTERS_DURATION,
            FAB_DELAY + FAB_ALPHA_DURATION,
        )
        maxEnd shouldBe DETAIL_ASSEMBLY_DURATION
    }

    @Test
    fun `easing curve reaches target endpoints`() {
        DETAIL_ASSEMBLY_EASING.transform(0f) shouldBe (0f plusOrMinus 0.001f)
        DETAIL_ASSEMBLY_EASING.transform(1f) shouldBe (1f plusOrMinus 0.001f)
    }
}
