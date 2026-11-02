package education.mahmoud.quranyapp.asr.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat
import education.mahmoud.quranyapp.asr.ModelDownloader
import education.mahmoud.quranyapp.asr.R
import java.util.Locale

/**
 * Shown only the first time the ASR feature is used.
 * Lives entirely inside the dynamic feature module.
 */
class DownloadModelActivity : AppCompatActivity() {

    private var download: ModelDownloader.Handle? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_model)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvProgress = findViewById<TextView>(R.id.tvProgress)
        progressBar.isIndeterminate = true

        download = ModelDownloader.downloadModel(
            context = this,
            onProgress = { percent ->
                if (isDestroyed || percent < 0) return@downloadModel
                progressBar.isIndeterminate = false
                progressBar.progress = percent
            },
            onComplete = {
                if (isDestroyed) return@downloadModel
                finish()
            },
            onError = { message ->
                if (isDestroyed) return@downloadModel
                progressBar.visibility = View.GONE
                tvProgress.text = message
            },
            onBytes = { downloaded, total ->
                if (isDestroyed) return@downloadModel
                tvProgress.text = if (total > 0) {
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

    private fun formatBytes(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 0.1) {
            String.format(Locale.US, "%.1f م.ب", mb)
        } else {
            String.format(Locale.US, "%.0f ك.ب", bytes / 1024.0)
        }
    }

    override fun onDestroy() {
        if (isFinishing) download?.cancel()
        super.onDestroy()
    }
}
