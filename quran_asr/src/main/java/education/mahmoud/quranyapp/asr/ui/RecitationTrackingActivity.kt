package education.mahmoud.quranyapp.asr.ui

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat
import education.mahmoud.quranyapp.asr.R
import education.mahmoud.quranyapp.feature.asr.AsrEntryPoint

/**
 * Main recitation tracking screen.
 * Integrates with memorization test: shows ayah, highlights words, tracks errors.
 * Entirely inside the dynamic feature module.
 */
class RecitationTrackingActivity : AppCompatActivity() {

    private var isRecording = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recitation_tracking)

        val btnToggle = findViewById<Button>(R.id.btnToggle)
        val tvAyah = findViewById<TextView>(R.id.tvAyah)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvSurah = findViewById<TextView>(R.id.tvSurah)

        intent.getStringExtra(AsrEntryPoint.EXTRA_RANGE_LABEL)
            ?.takeIf { it.isNotBlank() }
            ?.let { tvSurah.text = it }
        tvAyah.text = intent.getStringExtra(AsrEntryPoint.EXTRA_AYAH_TEXT)
            ?.takeIf { it.isNotBlank() }
            ?: "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ"

        btnToggle.setOnClickListener {
            isRecording = !isRecording
            if (isRecording) {
                btnToggle.text = "إيقاف"
                tvStatus.text = "جاري التعرف على التلاوة..."
                // TODO: start audio recording + Sherpa-ONNX / ONNX inference
            } else {
                btnToggle.text = "ابدأ التلاوة"
                tvStatus.text = "تم إيقاف التسجيل"
                // TODO: show results / update memorization score
            }
        }
    }
}
