package education.mahmoud.quranyapp.asr.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat
import education.mahmoud.quranyapp.asr.ModelDownloader
import education.mahmoud.quranyapp.asr.R
import education.mahmoud.quranyapp.feature.asr.AsrEntryPoint
import java.util.Locale

/**
 * Main recitation tracking screen.
 * Integrates with memorization test: shows ayah, highlights words, tracks errors.
 * Entirely inside the dynamic feature module.
 */
class RecitationTrackingActivity : AppCompatActivity() {

    private var isRecording = false
    private var download: ModelDownloader.Handle? = null

    private lateinit var btnToggle: Button
    private lateinit var tvStatus: TextView
    private lateinit var progressModel: ProgressBar
    private lateinit var tvModelDetail: TextView

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
        if (!ModelDownloader.isModelReady(this)) {
            beginDownload()
            return
        }
        isRecording = !isRecording
        if (isRecording) {
            btnToggle.text = getString(R.string.asr_stop)
            tvStatus.text = getString(R.string.asr_recognizing)
            // TODO: start audio recording + Sherpa-ONNX / ONNX inference
        } else {
            btnToggle.text = getString(R.string.asr_start)
            tvStatus.text = getString(R.string.asr_stopped)
            // TODO: show results / update memorization score
        }
    }

    private fun beginDownload() {
        progressModel.visibility = View.VISIBLE
        progressModel.isIndeterminate = true
        progressModel.progress = 0
        tvModelDetail.visibility = View.VISIBLE
        tvModelDetail.text = getString(R.string.asr_model_progress_unknown, "0 ك.ب")
        tvStatus.text = getString(R.string.asr_model_downloading)
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
                progressModel.visibility = View.GONE
                tvModelDetail.visibility = View.VISIBLE
                tvModelDetail.text = message
                tvStatus.text = getString(R.string.asr_model_failed)
                btnToggle.text = getString(R.string.asr_model_retry)
            },
            onBytes = { downloaded, total ->
                if (isDestroyed) return@downloadModel
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
        btnToggle.text = button
        isRecording = false
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
        download?.cancel()
        super.onDestroy()
    }
}
