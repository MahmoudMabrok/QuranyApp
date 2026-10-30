package education.mahmoud.quranyapp.feature.listen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AudioStorageTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun write(reciter: Reciter, name: String, size: Int) {
        File(tmp.root, reciter.id).apply { mkdirs() }.resolve(name).writeBytes(ByteArray(size))
    }

    @Test
    fun `usage counts mp3 files per reciter and ignores partial downloads`() {
        write(Reciter.ALAFASY, "1.mp3", 100)
        write(Reciter.ALAFASY, "2.mp3", 50)
        write(Reciter.ALAFASY, "3.mp3.part", 999)
        write(Reciter.HUSARY, "1.mp3", 10)

        val usage = AudioStorage(tmp.root).usage()

        assertEquals(
            listOf(
                ReciterDownloads(Reciter.ALAFASY, 2, 150),
                ReciterDownloads(Reciter.HUSARY, 1, 10),
            ),
            usage,
        )
        assertEquals(160L, AudioStorage(tmp.root).totalBytes())
    }

    @Test
    fun `delete removes one reciter and deleteAll removes everything`() {
        write(Reciter.ALAFASY, "1.mp3", 100)
        write(Reciter.HUSARY, "1.mp3", 10)
        val storage = AudioStorage(tmp.root)

        assertTrue(storage.delete(Reciter.ALAFASY))
        assertFalse(File(tmp.root, Reciter.ALAFASY.id).exists())
        assertEquals(listOf(ReciterDownloads(Reciter.HUSARY, 1, 10)), storage.usage())

        assertTrue(storage.deleteAll())
        assertTrue(storage.usage().isEmpty())
    }
}
