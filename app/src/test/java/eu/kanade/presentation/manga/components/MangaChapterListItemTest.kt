package eu.kanade.presentation.manga.components

import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MangaChapterListItemTest {

    @Test
    fun `shouldShowInnerUnreadDot returns false when isSubChapter is true regardless of read status`() {
        shouldShowInnerUnreadDot(read = false, isSubChapter = true) shouldBe false
        shouldShowInnerUnreadDot(read = true, isSubChapter = true) shouldBe false
    }

    @Test
    fun `shouldShowInnerUnreadDot returns true only when unread and not sub-chapter`() {
        shouldShowInnerUnreadDot(read = false, isSubChapter = false) shouldBe true
        shouldShowInnerUnreadDot(read = true, isSubChapter = false) shouldBe false
    }

    @Test
    fun `sub-chapter node sizing constants match specification`() {
        SUB_CHAPTER_NODE_UNREAD_SIZE shouldBe 6.5.dp
        SUB_CHAPTER_NODE_READ_SIZE shouldBe 6.dp
        SUB_CHAPTER_NODE_READ_BORDER_WIDTH shouldBe 1.5.dp
    }

    @Test
    fun `sub-chapter unread node has solid fill and correct dimensions`() {
        val properties = getSubChapterNodeProperties(read = false)
        properties.isSolidFill shouldBe true
        properties.isHollowOutline shouldBe false
        properties.size shouldBe 6.5.dp
        properties.borderWidth shouldBe 0.dp
    }

    @Test
    fun `sub-chapter read node has hollow outline and correct dimensions`() {
        val properties = getSubChapterNodeProperties(read = true)
        properties.isSolidFill shouldBe false
        properties.isHollowOutline shouldBe true
        properties.size shouldBe 6.dp
        properties.borderWidth shouldBe 1.5.dp
    }

    @Test
    fun `subChapterNode helper object mirrors properties correctly`() {
        SubChapterNode.UNREAD_SIZE shouldBe SUB_CHAPTER_NODE_UNREAD_SIZE
        SubChapterNode.READ_SIZE shouldBe SUB_CHAPTER_NODE_READ_SIZE
        SubChapterNode.READ_BORDER_WIDTH shouldBe SUB_CHAPTER_NODE_READ_BORDER_WIDTH

        SubChapterNode.properties(read = false) shouldBe getSubChapterNodeProperties(read = false)
        SubChapterNode.properties(read = true) shouldBe getSubChapterNodeProperties(read = true)
    }
}
