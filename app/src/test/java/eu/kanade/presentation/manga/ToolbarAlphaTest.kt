package eu.kanade.presentation.manga

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ToolbarAlphaTest {

    @Test
    fun `title alpha hidden at first item`() {
        assertEquals(0f, toolbarTitleAlpha(0))
    }

    @Test
    fun `title alpha visible after scroll`() {
        assertEquals(1f, toolbarTitleAlpha(1))
    }
}
