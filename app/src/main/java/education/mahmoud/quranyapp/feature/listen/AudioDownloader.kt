package education.mahmoud.quranyapp.feature.listen

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/** Blocking download of one ayah recitation into [root] (call from a background thread). */
class AudioDownloader(private val http: OkHttpClient, private val root: File) {

    fun download(reciter: Reciter, ayahIndex: Int): Boolean {
        val file = reciter.cacheFile(root, ayahIndex)
        file.parentFile?.mkdirs()
        val temp = File(file.parentFile, "${file.name}.part")
        return try {
            http.newCall(Request.Builder().url(reciter.audioUrl(ayahIndex)).build()).execute().use { response ->
                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    Log.w(TAG, "download failed ${response.code()} for $ayahIndex")
                    return false
                }
                temp.outputStream().use { out -> body.byteStream().copyTo(out) }
            }
            temp.renameTo(file)
        } catch (e: IOException) {
            Log.w(TAG, "download failed for $ayahIndex", e)
            temp.delete()
            false
        }
    }

    private companion object {
        const val TAG = "AudioDownloader"
    }
}
