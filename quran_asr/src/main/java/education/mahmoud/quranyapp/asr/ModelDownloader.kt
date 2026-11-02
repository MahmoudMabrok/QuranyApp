package education.mahmoud.quranyapp.asr

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object ModelDownloader {

    private const val FILE_ID = "1d2aDGa-wLbAfj0AIJHmeHZ4-n9GT2Sal"
    private const val MODEL_URL =
        "https://drive.usercontent.google.com/download?id=$FILE_ID&export=download&confirm=t"
    private const val MODEL_NAME = "zipformer_p_arabic_v3.1.int8.onnx"
    private const val TOKENS_NAME = "tokens.txt"
    private const val TOKENS_URL =
        "https://raw.githubusercontent.com/yazinsai/tilawa/main/lab/experiments/zipformer-ctc/tokens.txt"
    private const val MIN_MODEL_BYTES = 1_000_000L

    private val main = Handler(Looper.getMainLooper())
    private val io = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "asr-model-download").apply { isDaemon = true }
    }

    @Volatile
    private var active: Handle? = null

    class Handle {
        @Volatile
        var cancelled = false
            private set

        fun cancel() {
            cancelled = true
        }
    }

    fun getModelDir(context: Context): File {
        return File(context.filesDir, "asr_model").apply { mkdirs() }
    }

    fun getModelFile(context: Context): File {
        return File(getModelDir(context), MODEL_NAME)
    }

    fun getTokensFile(context: Context): File {
        return File(getModelDir(context), TOKENS_NAME)
    }

    fun isModelReady(context: Context): Boolean {
        return isValidModel(getModelFile(context)) && isValidTokens(getTokensFile(context))
    }

    /**
     * [onProgress] receives 0–100 when the size is known, and -1 when it is not.
     * [onBytes] receives bytes written and the total, or -1 if the total is unknown.
     * The four-argument form stays for [education.mahmoud.quranyapp.feature.asr.AsrFeatureManager].
     */
    @JvmOverloads
    fun downloadModel(
        context: Context,
        onProgress: (Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit,
        onBytes: ((downloaded: Long, total: Long) -> Unit)? = null,
    ): Handle {
        val handle = Handle()
        val app = context.applicationContext
        val modelFile = getModelFile(app)
        val tokensFile = getTokensFile(app)
        if (isValidModel(modelFile) && isValidTokens(tokensFile)) {
            main.post { if (!handle.cancelled) onComplete() }
            return handle
        }

        synchronized(this) {
            active?.cancel()
            active = handle
        }

        io.execute {
            try {
                if (!isValidModel(modelFile)) {
                    val part = File(modelFile.parentFile, "$MODEL_NAME.${System.nanoTime()}.part")
                    try {
                        downloadTo(MODEL_URL, part, handle) { downloaded, total ->
                            report(handle, onProgress, onBytes, downloaded, total)
                        }
                    } catch (stopped: Stopped) {
                        part.delete()
                        return@execute
                    } catch (error: Exception) {
                        part.delete()
                        throw error
                    }
                    if (handle.cancelled || active !== handle) {
                        part.delete()
                        return@execute
                    }
                    if (!isValidModel(part)) {
                        part.delete()
                        postError(handle, onError, "الملف الذي تم تحميله ليس نموذج التعرف.")
                        return@execute
                    }
                    replaceFile(part, modelFile)
                }
                if (!isValidTokens(tokensFile)) {
                    val part = File(tokensFile.parentFile, "$TOKENS_NAME.${System.nanoTime()}.part")
                    try {
                        downloadTo(TOKENS_URL, part, handle) { downloaded, total ->
                            report(handle, onProgress, onBytes, downloaded, total)
                        }
                    } catch (stopped: Stopped) {
                        part.delete()
                        return@execute
                    } catch (error: Exception) {
                        part.delete()
                        throw error
                    }
                    if (handle.cancelled || active !== handle) {
                        part.delete()
                        return@execute
                    }
                    if (!isValidTokens(part)) {
                        part.delete()
                        postError(handle, onError, "ملف رموز النموذج غير صالح.")
                        return@execute
                    }
                    replaceFile(part, tokensFile)
                }
                main.post { if (!handle.cancelled) onComplete() }
            } catch (error: Exception) {
                if (!handle.cancelled) postError(handle, onError, messageFor(error))
            }
        }
        return handle
    }

    private fun downloadTo(
        url: String,
        dest: File,
        handle: Handle,
        onBytes: (downloaded: Long, total: Long) -> Unit,
    ) {
        val connection = openConnection(url, redirectsLeft = 3)
        try {
            val total = connection.contentLengthLong
            connection.inputStream.use { input ->
                dest.outputStream().use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var downloaded = 0L
                    var lastUpdate = 0L
                    while (true) {
                        if (handle.cancelled) throw Stopped()
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        val now = SystemClock.elapsedRealtime()
                        if (now - lastUpdate >= 200) {
                            lastUpdate = now
                            onBytes(downloaded, total)
                        }
                    }
                    output.flush()
                    onBytes(downloaded, total)
                    if (total > 0 && downloaded != total) {
                        throw IOException("انقطع التحميل قبل اكتمال الملف.")
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(url: String, redirectsLeft: Int): HttpURLConnection {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = true
            connectTimeout = 20_000
            readTimeout = 60_000
            setRequestProperty("User-Agent", "Mozilla/5.0")
        }
        val code = connection.responseCode
        if (code !in 200..299) {
            connection.disconnect()
            throw IOException("تعذر تحميل النموذج (رمز $code).")
        }
        val type = connection.contentType.orEmpty()
        if (type.contains("text/html", ignoreCase = true)) {
            val html = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            if (redirectsLeft <= 0) throw IOException("تعذر الوصول إلى ملف النموذج.")
            val next = confirmUrl(html) ?: throw IOException("تعذر الوصول إلى ملف النموذج.")
            return openConnection(next, redirectsLeft - 1)
        }
        return connection
    }

    private fun confirmUrl(html: String): String? {
        val uuid = Regex("""name="uuid"\s+value="([^"]+)"""").find(html)?.groupValues?.get(1)
            ?: Regex("""uuid=([0-9A-Fa-f-]+)""").find(html)?.groupValues?.get(1)
        if (!uuid.isNullOrBlank()) {
            return "https://drive.usercontent.google.com/download?id=$FILE_ID&export=download&confirm=t&uuid=$uuid"
        }
        val href = Regex("""href="([^"]*confirm=[^"]*)"""").find(html)?.groupValues?.get(1)
            ?.replace("amp;", "")
            ?: return null
        return when {
            href.startsWith("http") -> href
            href.startsWith("/") -> "https://drive.google.com$href"
            else -> null
        }
    }

    private fun report(
        handle: Handle,
        onProgress: (Int) -> Unit,
        onBytes: ((Long, Long) -> Unit)?,
        downloaded: Long,
        total: Long,
    ) {
        if (handle.cancelled) return
        val percent = if (total > 0) ((downloaded * 100) / total).toInt().coerceIn(0, 100) else -1
        main.post {
            if (handle.cancelled) return@post
            onProgress(percent)
            onBytes?.invoke(downloaded, total)
        }
    }

    private fun replaceFile(part: File, dest: File) {
        if (dest.exists()) dest.delete()
        if (!part.renameTo(dest)) {
            part.copyTo(dest, overwrite = true)
            part.delete()
        }
    }

    private fun isValidTokens(file: File): Boolean {
        if (!file.isFile || file.length() < 200) return false
        val head = ByteArray(64)
        val read = file.inputStream().use { it.read(head) }
        if (read <= 0) return false
        val start = String(head, 0, read, Charsets.UTF_8).trimStart()
        if (start.startsWith("<") || start.startsWith("{")) return false
        return file.bufferedReader().use { reader ->
            var lines = 0
            while (reader.readLine() != null && lines < 200) lines++
            lines >= 200
        }
    }

    private fun isValidModel(file: File): Boolean {
        if (!file.isFile || file.length() < MIN_MODEL_BYTES) return false
        val head = ByteArray(32)
        val read = file.inputStream().use { it.read(head) }
        if (read <= 0) return false
        val start = String(head, 0, read, Charsets.ISO_8859_1).trimStart()
        return !start.startsWith("<") && !start.startsWith("{")
    }

    private fun messageFor(error: Exception): String {
        val text = error.message?.takeIf { it.isNotBlank() }
        return when {
            text != null && text.any { it.code in 0x0600..0x06FF } -> text
            else -> "تعذر تحميل النموذج. تحقق من الاتصال وحاول مرة أخرى."
        }
    }

    private fun postError(handle: Handle, onError: (String) -> Unit, message: String) {
        main.post { if (!handle.cancelled) onError(message) }
    }

    private class Stopped : RuntimeException()
}
