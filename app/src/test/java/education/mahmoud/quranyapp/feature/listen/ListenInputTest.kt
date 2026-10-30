package education.mahmoud.quranyapp.feature.listen

import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
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
        assertEquals(
            ListenInput.Range(
                2, 1, 2, 286, 1, 1,
                SessionMode.CHUNK, ListenInput.DEFAULT_CHUNK_SIZE, listOf(Reciter.ALAFASY),
            ),
            range,
        )
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

    @Test
    fun `no selected reciter and bad chunk size are rejected`() {
        val (range, errors) = ListenInput.validate(
            ListenInput.Form(selectedReciters = emptySet(), chunkSize = "0"),
            ayahCount,
        )
        assertNull(range)
        assertTrue(errors.noReciters)
        assertTrue(errors.chunkSize)
    }

    private val a1 = AyahItem(ayahIndex = 1, text = "1")
    private val a2 = AyahItem(ayahIndex = 2, text = "2")
    private val a3 = AyahItem(ayahIndex = 3, text = "3")
    private val reciters = listOf(Reciter.ALAFASY, Reciter.HUSARY)

    @Test
    fun `session playlist full mode plays the whole range per reciter`() {
        assertEquals(
            listOf(a1 to Reciter.ALAFASY, a2 to Reciter.ALAFASY, a1 to Reciter.HUSARY, a2 to Reciter.HUSARY),
            ListenInput.sessionPlaylist(listOf(a1, a2), reciters, SessionMode.FULL, 5),
        )
    }

    @Test
    fun `session playlist chunk mode alternates reciters every chunk`() {
        assertEquals(
            listOf(a1 to Reciter.ALAFASY, a2 to Reciter.ALAFASY, a3 to Reciter.HUSARY),
            ListenInput.sessionPlaylist(listOf(a1, a2, a3), reciters, SessionMode.CHUNK, 2),
        )
    }

    @Test
    fun `session playlist per ayah mode plays every reciter for each ayah`() {
        assertEquals(
            listOf(a1 to Reciter.ALAFASY, a1 to Reciter.HUSARY, a2 to Reciter.ALAFASY, a2 to Reciter.HUSARY),
            ListenInput.sessionPlaylist(listOf(a1, a2), reciters, SessionMode.PER_AYAH, 5),
        )
    }
}
