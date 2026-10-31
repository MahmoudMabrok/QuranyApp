package education.mahmoud.quranyapp.asr.ui

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import education.mahmoud.quranyapp.asr.ModelDownloader
import education.mahmoud.quranyapp.asr.R

/**
 * Shown only the first time the ASR feature is used.
 * Lives entirely inside the dynamic feature module.
 */
class DownloadModelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_model)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvProgress = findViewById<TextView>(R.id.tvProgress)

        ModelDownloader.downloadModel(
            context = this,
            onProgress = { percent ->
                progressBar.progress = percent
                tvProgress.text = "$percent%"
            },
            onComplete = {
                // TODO: start recitation tracking activity
                finish()
            },
            onError = { msg ->
                tvProgress.text = msg
            }
        )
    }
}
