package education.mahmoud.quranyapp.feature.testquran

import education.mahmoud.quranyapp.feature.test_quran.MemorizationTest
import education.mahmoud.quranyapp.feature.test_quran.MemorizationTest.Level
import education.mahmoud.quranyapp.feature.test_quran.MemorizationTest.WordState.CORRECT
import education.mahmoud.quranyapp.feature.test_quran.MemorizationTest.WordState.EXTRA
import education.mahmoud.quranyapp.feature.test_quran.MemorizationTest.WordState.MISSING
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MemorizationTestTest {

    private val fatiha2 = "الحمد لله رب العالمين"

    @Test
    fun `perfect answer scores 100`() {
        val g = MemorizationTest.grade(fatiha2, "الحمد لله رب العالمين")
        assertTrue(g.isPerfect)
        assertEquals(100, g.accuracy)
        assertEquals(8, g.points)
        assertTrue(g.words.all { it.state == CORRECT })
    }

    @Test
    fun `tashkeel and letter forms are ignored`() {
        val g = MemorizationTest.grade("إياك نعبد وإياك نستعين", "اِيَّاكَ نَعْبُدُ  واياك نستعين")
        assertTrue(g.isPerfect)
    }

    @Test
    fun `missing and extra words are marked in order`() {
        val g = MemorizationTest.grade(fatiha2, "الحمد رب كل العالمين")
        assertEquals(
            listOf(CORRECT, MISSING, CORRECT, EXTRA, CORRECT),
            g.words.map { it.state },
        )
        assertEquals("لله", g.words[1].text)
        assertEquals("كل", g.words[3].text)
        assertEquals(3, g.correctCount)
        assertEquals(1, g.missingCount)
        assertEquals(1, g.extraCount)
        assertEquals(60, g.accuracy) // 3 of (4 expected + 1 extra)
        assertEquals(4, g.points) // 3*2 - 1 - 1
    }

    @Test
    fun `empty answer is all missing with zero points`() {
        val g = MemorizationTest.grade(fatiha2, "   ")
        assertEquals(4, g.missingCount)
        assertEquals(0, g.accuracy)
        assertEquals(0, g.points)
        assertFalse(g.isPerfect)
    }

    @Test
    fun `levels by accuracy`() {
        assertEquals(Level.EXCELLENT, MemorizationTest.level(95))
        assertEquals(Level.VERY_GOOD, MemorizationTest.level(80))
        assertEquals(Level.GOOD, MemorizationTest.level(50))
        assertEquals(Level.NEEDS_REVIEW, MemorizationTest.level(49))
    }

    @Test
    fun `full questions prompt each ayah with the previous one`() {
        val q = MemorizationTest.fullQuestions(3)
        assertEquals(listOf(null, 0, 1), q.map { it.promptIndex })
        assertEquals(listOf(0, 1, 2), q.map { it.answerIndex })
    }

    @Test
    fun `random question asks for the ayah after the prompt`() {
        assertNull(MemorizationTest.randomQuestion(1))
        repeat(50) { seed ->
            val q = MemorizationTest.randomQuestion(5, Random(seed))!!
            assertEquals(q.promptIndex!! + 1, q.answerIndex)
            assertTrue(q.answerIndex in 1..4)
        }
    }

    @Test
    fun `hint shows the first words`() {
        assertEquals("الحمد لله …", MemorizationTest.hint(fatiha2))
        assertEquals("الحمد", MemorizationTest.hint("الحمد"))
    }
}
