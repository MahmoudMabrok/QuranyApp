package education.mahmoud.quranyapp.feature.listen

import java.io.File

/** Downloaded recitations of one reciter inside the app-specific audio folder. */
data class ReciterDownloads(val reciter: Reciter, val files: Int, val bytes: Long)

/** Reads and clears the per-reciter audio cache created by [AudioDownloader]. */
class AudioStorage(private val root: File) {

    /** Reciters that have at least one downloaded ayah, in [Reciter] order. */
    fun usage(): List<ReciterDownloads> = Reciter.values().mapNotNull { reciter ->
        val files = audioFiles(reciter)
        if (files.isEmpty()) null else ReciterDownloads(reciter, files.size, files.sumOf { it.length() })
    }

    fun totalBytes(): Long = usage().sumOf { it.bytes }

    /** Deletes everything downloaded for [reciter] (including unfinished `.part` files). */
    fun delete(reciter: Reciter): Boolean = File(root, reciter.id).let { !it.exists() || it.deleteRecursively() }

    fun deleteAll(): Boolean = Reciter.values().map { delete(it) }.all { it }

    private fun audioFiles(reciter: Reciter): List<File> =
        File(root, reciter.id).listFiles { file -> file.isFile && file.name.endsWith(".mp3") }?.toList().orEmpty()
}
