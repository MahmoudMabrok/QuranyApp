package education.mahmoud.quranyapp.asr

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerResult
import java.io.IOException
import kotlin.math.max

/**
 * Records 16 kHz mono audio and feeds it to the streaming recognizer.
 * Callbacks are delivered on the main thread.
 */
class RecitationSession(
    private val recognizer: OnlineRecognizer,
    private val onPartial: (String) -> Unit,
    private val onDone: (String) -> Unit,
    private val onError: (String) -> Unit,
) {
    private val main = Handler(Looper.getMainLooper())
    private val stream = recognizer.createStream()
    private var recorder: AudioRecord? = null

    @Volatile
    private var running = false

    @SuppressLint("MissingPermission")
    fun start() {
        val min = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (min <= 0) throw IOException("الميكروفون غير متاح.")
        val bufferBytes = max(min, SAMPLE_RATE / 5 * 2)
        val audio = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferBytes,
        )
        if (audio.state != AudioRecord.STATE_INITIALIZED) {
            audio.release()
            throw IOException("تعذر فتح الميكروفون.")
        }
        recorder = audio
        running = true
        try {
            audio.startRecording()
        } catch (error: IllegalStateException) {
            audio.release()
            throw IOException("تعذر بدء التسجيل.")
        }
        if (audio.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            audio.release()
            throw IOException("تعذر بدء التسجيل.")
        }
        Thread({ loop(audio) }, "asr-mic").apply { isDaemon = true }.start()
    }

    fun stop() {
        running = false
    }

    private fun loop(audio: AudioRecord) {
        val pcm = ShortArray(SAMPLE_RATE / 5)
        try {
            while (running) {
                val read = audio.read(pcm, 0, pcm.size)
                if (read < 0) throw IOException("توقف الميكروفون أثناء التسجيل.")
                if (read == 0) continue
                val samples = FloatArray(read) { index -> pcm[index] / 32768.0f }
                stream.acceptWaveform(samples, SAMPLE_RATE)
                while (recognizer.isReady(stream)) recognizer.decode(stream)
                val text = spoken(recognizer.getResult(stream))
                main.post { if (running) onPartial(text) }
            }
            stream.inputFinished()
            while (recognizer.isReady(stream)) recognizer.decode(stream)
            val text = spoken(recognizer.getResult(stream))
            main.post { onDone(text) }
        } catch (error: Exception) {
            val message = error.message?.takeIf { it.isNotBlank() } ?: "توقف التعرف بسبب خطأ."
            main.post { onError(message) }
        } finally {
            runCatching { if (audio.recordingState == AudioRecord.RECORDSTATE_RECORDING) audio.stop() }
            audio.release()
            runCatching { stream.release() }
        }
    }

    private fun spoken(result: OnlineRecognizerResult): String {
        val tokens = result.tokens.filter { it.isNotBlank() && it != "<blank>" }
        return if (tokens.isEmpty()) result.text else tokens.joinToString("")
    }

    private companion object {
        const val SAMPLE_RATE = 16000
    }
}
