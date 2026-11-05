package education.mahmoud.quranyapp.feature.asr

import android.content.Context

/**
 * Thin entry point in the base module.
 *
 * The :quran_asr dynamic feature is currently commented out / excluded from Gradle
 * builds, so the real implementation (SplitInstall + feature Activity) is disabled.
 * This stub keeps the class from breaking compilation while the module is out.
 */
object AsrEntryPoint {

    const val EXTRA_RANGE_LABEL = "asr_range_label"
    const val EXTRA_AYAH_TEXT = "asr_ayah_text"
    const val EXTRA_AYAH_TEXTS = "asr_ayah_texts"
    const val EXTRA_START_SURA = "asr_start_sura"
    const val EXTRA_START_AYAH = "asr_start_ayah"
    const val EXTRA_END_SURA = "asr_end_sura"
    const val EXTRA_END_AYAH = "asr_end_ayah"

    /** Surah/ayah range chosen on the memorization test screen. Indexes are 1-based. */
    data class RecitationRange(
        val startSura: Int,
        val startAyah: Int,
        val endSura: Int,
        val endAyah: Int,
        val label: String,
        val ayahText: String? = null,
        val ayahTexts: List<String> = emptyList(),
    )

    /**
     * No-op while the :quran_asr module is excluded.
     * The original launch / install logic lived here and depended on
     * com.google.android.play:feature-delivery.
     */
    fun launch(context: Context, range: RecitationRange? = null) {
        // Feature disabled for now.
    }
}
