package education.mahmoud.quranyapp.feature.search

import education.mahmoud.quranyapp.feature.ayahs_search.SearchText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTextTest {

    @Test
    fun `normalizeQuery strips tashkeel and extra spaces`() {
        assertEquals("الرحمن الرحيم", SearchText.normalizeQuery("  الرَّحْمَنِ   الرَّحِيمِ "))
    }

    @Test
    fun `normalizeQuery handles null and blank`() {
        assertEquals("", SearchText.normalizeQuery(null))
        assertEquals("", SearchText.normalizeQuery("   "))
    }

    @Test
    fun `matchRanges finds every occurrence`() {
        val text = "الرحمن الرحيم الرحمن"
        assertEquals(listOf(0..5, 14..19), SearchText.matchRanges(text, "الرحمن"))
    }

    @Test
    fun `matchRanges treats regex characters literally`() {
        assertTrue(SearchText.matchRanges("بسم الله", "(*[").isEmpty())
        assertEquals(listOf(2..2), SearchText.matchRanges("ab.cd", "."))
    }

    @Test
    fun `matchRanges with empty word returns nothing`() {
        assertTrue(SearchText.matchRanges("بسم الله", "").isEmpty())
    }
}
