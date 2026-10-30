package education.mahmoud.quranyapp.feature.listen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ListenInputTest {

    // Al-Fatiha 7, Al-Baqarah 286, Al-Imran 200
    private val counts = mapOf(1 to 7, 2 to 286, 3 to 200)
    private val ayahCount: (Int) -> Int = { counts[it] ?: 0 }

    @Test
    fun `empty ayah fields cover the whole sura`() {
        val (range, errors) = ListenInput.validate(ListenInput.Form(startSura = 2, endSura = 2), ayahCount)
        assertEquals(ListenInput.Range(2, 1, 2, 286, 1, 1), range)
        assertEquals(false, errors.any)
    }

    @Test
    fun `out of range ayah and reversed order are rejected`() {
        val (range, errors) = ListenInput.validate(ListenInput.Form(1, "8", 1, "3"), ayahCount)
        assertNull(range)
        assertTrue(errors.startAyah)

        val (range2, errors2) = ListenInput.validate(ListenInput.Form(2, "5", 1, "3"), ayahCount)
        assertNull(range2)
        assertTrue(errors2.order)

        val (_, errors3) = ListenInput.validate(ListenInput.Form(1, "", 1, "", repeatEach = "21"), ayahCount)
        assertTrue(errors3.repeatEach)
    }

    @Test
    fun `global index adds previous suras`() {
        assertEquals(1, ListenInput.globalIndex(ayahCount, 1, 1))
        assertEquals(8, ListenInput.globalIndex(ayahCount, 2, 1))
        assertEquals(262, ListenInput.globalIndex(ayahCount, 2, 255))
    }

    @Test
    fun `playlist repeats each ayah then the whole range`() {
        assertEquals(listOf(1, 1, 2, 2, 1, 1, 2, 2), ListenInput.playlist(listOf(1, 2), 2, 2))
    }
}
