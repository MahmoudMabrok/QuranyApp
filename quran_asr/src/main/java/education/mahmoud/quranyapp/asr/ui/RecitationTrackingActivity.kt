package education.mahmoud.quranyapp.asr.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat
import education.mahmoud.quranyapp.asr.AyahReference
import education.mahmoud.quranyapp.asr.ModelDownloader
import education.mahmoud.quranyapp.asr.PhonemeCompare
import education.mahmoud.quranyapp.asr.R
import education.mahmoud.quranyapp.asr.RecitationEngine
import education.mahmoud.quranyapp.asr.RecitationSession
import education.mahmoud.quranyapp.feature.asr.AsrEntryPoint
import java.util.Locale

/**
 * Main recitation tracking screen.
 * Listens to the recitation and shows the tajweed rulings, not a spelling score.
 */
class RecitationTrackingActivity : AppCompatActivity() {

    private var download: ModelDownloader.Handle? = null
    private var session: RecitationSession? = null
    private var stopping = false
    private var listenGeneration = 0

    private lateinit var btnToggle: Button
    private lateinit var tvStatus: TextView
    private lateinit var progressModel: ProgressBar
    private lateinit var tvModelDetail: TextView
    private lateinit var tvResults: TextView
    private var words: List<AyahReference.Word> = emptyList()
    private var startSura = 1
    private var startAyah = 1
    private var endSura = 1
    private var endAyah = 1

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recitation_tracking)

        btnToggle = findViewById(R.id.btnToggle)
        val tvAyah = findViewById<TextView>(R.id.tvAyah)
        tvStatus = findViewById(R.id.tvStatus)
        val tvSurah = findViewById<TextView>(R.id.tvSurah)
        progressModel = findViewById(R.id.progressModel)
        tvModelDetail = findViewById(R.id.tvModelDetail)
        tvResults = findViewById(R.id.tvResults)
        tvResults.textSize = 22f
        startSura = intent.getIntExtra(AsrEntryPoint.EXTRA_START_SURA, 1).coerceAtLeast(1)
        startAyah = intent.getIntExtra(AsrEntryPoint.EXTRA_START_AYAH, 1).coerceAtLeast(1)
        endSura = intent.getIntExtra(AsrEntryPoint.EXTRA_END_SURA, startSura).coerceAtLeast(startSura)
        endAyah = intent.getIntExtra(AsrEntryPoint.EXTRA_END_AYAH, startAyah).coerceAtLeast(1)

        intent.getStringExtra(AsrEntryPoint.EXTRA_RANGE_LABEL)
            ?.takeIf { it.isNotBlank() }
            ?.let { tvSurah.text = it }
        tvAyah.text = intent.getStringExtra(AsrEntryPoint.EXTRA_AYAH_TEXT)
            ?.takeIf { it.isNotBlank() }
            ?: "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ"

        btnToggle.setOnClickListener { onToggle() }
        if (!ModelDownloader.isModelReady(this)) {
            beginDownload()
        }
    }

    private fun onToggle() {
        if (download != null) {
            download?.cancel()
            download = null
            showIdle(getString(R.string.asr_model_cancelled), getString(R.string.asr_model_retry))
            return
        }
        if (session != null) {
            if (stopping) return
            stopping = true
            tvStatus.text = getString(R.string.asr_finishing)
            btnToggle.isEnabled = false
            session?.stop()
            return
        }
        if (!ModelDownloader.isModelReady(this)) {
            beginDownload()
            return
        }
        if (!hasMicPermission()) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), MIC_REQUEST)
            return
        }
        startListening()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != MIC_REQUEST) return
        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            showProblem(getString(R.string.asr_mic_denied), getString(R.string.asr_model_retry))
        }
    }

    private fun startListening() {
        val generation = ++listenGeneration
        stopping = false
        progressModel.visibility = View.VISIBLE
        progressModel.isIndeterminate = true
        tvModelDetail.visibility = View.GONE
        tvResults.text = ""
        tvStatus.text = getString(R.string.asr_preparing)
        btnToggle.isEnabled = false

        Thread {
            try {
                words = AyahReference.words(
                    applicationContext,
                    startSura,
                    startAyah,
                    endSura,
                    endAyah,
                )
                val engine = RecitationEngine.get(applicationContext)
                runOnUiThread {
                    if (isDestroyed || generation != listenGeneration) return@runOnUiThread
                    val created = RecitationSession(
                        recognizer = engine,
                        onPartial = { text ->
                            if (!isDestroyed) showRulings(text, finished = false)
                        },
                        onDone = { text ->
                            if (isDestroyed) return@RecitationSession
                            session = null
                            stopping = false
                            progressModel.visibility = View.GONE
                            btnToggle.isEnabled = true
                            btnToggle.text = getString(R.string.asr_start)
                            if (text.isBlank()) {
                                showRulings("", finished = false)
                                showProblem(getString(R.string.asr_empty), getString(R.string.asr_start))
                            } else {
                                showRulings(text, finished = true)
                            }
                        },
                        onError = { message ->
                            if (isDestroyed) return@RecitationSession
                            session = null
                            stopping = false
                            showProblem(message, getString(R.string.asr_model_retry))
                        },
                    )
                    try {
                        created.start()
                    } catch (error: Exception) {
                        showProblem(
                            error.message ?: getString(R.string.asr_mic_failed),
                            getString(R.string.asr_model_retry),
                        )
                        return@runOnUiThread
                    }
                    session = created
                    showRulings("", finished = false)
                    progressModel.visibility = View.GONE
                    tvStatus.text = getString(R.string.asr_listening)
                    btnToggle.isEnabled = true
                    btnToggle.text = getString(R.string.asr_stop)
                }
            } catch (error: Throwable) {
                runOnUiThread {
                    if (isDestroyed) return@runOnUiThread
                    val missingLibrary = error is UnsatisfiedLinkError ||
                        error.cause is UnsatisfiedLinkError ||
                        error is ExceptionInInitializerError
                    val detail = if (missingLibrary) {
                        getString(R.string.asr_library_missing)
                    } else {
                        error.message?.takeIf { it.isNotBlank() }
                            ?: getString(R.string.asr_engine_failed)
                    }
                    showProblem(detail, getString(R.string.asr_model_retry))
                }
            }
        }.apply { isDaemon = true }.start()
    }

    private fun hasMicPermission(): Boolean {
        if (Build.VERSION.SDK_INT < 23) return true
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun showRulings(heard: String, finished: Boolean) {
        val lines = PhonemeCompare.grade(heard, words, finished)
        if (lines.isEmpty()) {
            tvResults.text = getString(R.string.asr_no_rules)
            tvModelDetail.visibility = View.GONE
            if (finished) tvStatus.text = getString(R.string.asr_result)
            return
        }
        val text = SpannableStringBuilder()
        var index = 0
        while (index < lines.size) {
            var end = index + 1
            while (end < lines.size && lines[end].arabic == lines[index].arabic && !lines[end].newAyah) end++
            val group = lines.subList(index, end)
            if (text.isNotEmpty()) text.append("\n")
            val wordStart = text.length
            text.append(lines[index].arabic)
            text.setSpan(StyleSpan(Typeface.BOLD), wordStart, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.setSpan(
                ForegroundColorSpan(wordColor(group)),
                wordStart,
                text.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            for (line in group) {
                text.append("\n")
                val start = text.length
                text.append(line.rule)
                text.append(" — ")
                text.append(statusLabel(line.status))
                text.setSpan(
                    ForegroundColorSpan(statusColor(line.status)),
                    start,
                    text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
            index = end
        }
        tvResults.text = text
        val ok = lines.count { it.status == PhonemeCompare.Status.OK }
        val review = lines.count { it.status == PhonemeCompare.Status.MISSING || it.status == PhonemeCompare.Status.SHORT }
        tvModelDetail.visibility = View.VISIBLE
        tvModelDetail.text = getString(R.string.asr_rules_summary, ok, review)
        if (finished) {
            tvStatus.text = if (review == 0) getString(R.string.asr_rules_match) else getString(R.string.asr_result)
        }
    }

    private fun wordColor(lines: List<PhonemeCompare.Line>): Int {
        return when {
            lines.any { it.status == PhonemeCompare.Status.MISSING || it.status == PhonemeCompare.Status.SHORT } -> 0xFFC62828.toInt()
            lines.all { it.status == PhonemeCompare.Status.OK } -> 0xFF2E7D32.toInt()
            else -> 0xFF607D8B.toInt()
        }
    }

    private fun statusColor(status: PhonemeCompare.Status): Int = when (status) {
        PhonemeCompare.Status.OK -> 0xFF2E7D32.toInt()
        PhonemeCompare.Status.SHORT, PhonemeCompare.Status.MISSING -> 0xFFC62828.toInt()
        PhonemeCompare.Status.WAIT -> 0xFF78909C.toInt()
    }

    private fun statusLabel(status: PhonemeCompare.Status): String = when (status) {
        PhonemeCompare.Status.OK -> getString(R.string.asr_rule_ok)
        PhonemeCompare.Status.MISSING -> getString(R.string.asr_rule_missing)
        PhonemeCompare.Status.SHORT -> getString(R.string.asr_rule_short)
        PhonemeCompare.Status.WAIT -> getString(R.string.asr_rule_wait)
    }

    private fun beginDownload() {
        progressModel.visibility = View.VISIBLE
        progressModel.isIndeterminate = true
        progressModel.progress = 0
        tvModelDetail.visibility = View.VISIBLE
        tvModelDetail.text = getString(R.string.asr_model_progress_unknown, "0 ك.ب")
        tvResults.text = ""
        tvStatus.text = getString(R.string.asr_model_downloading)
        btnToggle.isEnabled = true
        btnToggle.text = getString(R.string.asr_model_cancel)

        download = ModelDownloader.downloadModel(
            context = this,
            onProgress = { percent ->
                if (isDestroyed || percent < 0) return@downloadModel
                progressModel.isIndeterminate = false
                progressModel.progress = percent
            },
            onComplete = {
                if (isDestroyed) return@downloadModel
                download = null
                showIdle(getString(R.string.asr_model_ready), getString(R.string.asr_start))
            },
            onError = { message ->
                if (isDestroyed) return@downloadModel
                download = null
                showProblem(message, getString(R.string.asr_model_retry))
            },
            onBytes = { downloaded, total ->
                if (isDestroyed) return@downloadModel
                tvModelDetail.visibility = View.VISIBLE
                tvModelDetail.text = if (total > 0) {
                    val percent = ((downloaded * 100) / total).toInt().coerceIn(0, 100)
                    getString(
                        R.string.asr_model_progress,
                        percent,
                        formatBytes(downloaded),
                        formatBytes(total),
                    )
                } else {
                    getString(R.string.asr_model_progress_unknown, formatBytes(downloaded))
                }
            },
        )
    }

    private fun showIdle(status: String, button: String) {
        progressModel.visibility = View.GONE
        tvModelDetail.visibility = View.GONE
        tvStatus.text = status
        btnToggle.isEnabled = true
        btnToggle.text = button
    }

    private fun showProblem(message: String, button: String) {
        progressModel.visibility = View.GONE
        tvModelDetail.visibility = View.VISIBLE
        tvModelDetail.text = message
        tvStatus.text = getString(R.string.asr_error_title)
        btnToggle.isEnabled = true
        btnToggle.text = button
    }

    private fun formatBytes(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 0.1) {
            String.format(Locale.US, "%.1f م.ب", mb)
        } else {
            String.format(Locale.US, "%.0f ك.ب", bytes / 1024.0)
        }
    }

    override fun onDestroy() {
        listenGeneration++
        download?.cancel()
        session?.stop()
        super.onDestroy()
    }

    private companion object {
        const val MIC_REQUEST = 41
    }
}
