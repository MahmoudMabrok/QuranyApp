package education.mahmoud.quranyapp.feature.asr

import android.content.Context
import education.mahmoud.quranyapp.asr.ModelDownloader

/**
 * Entry point for the Quran ASR dynamic feature.
 * Call ensureModel() before starting recitation tracking.
 */
object AsrFeatureManager {

    fun isReady(context: Context): Boolean =
        ModelDownloader.isModelReady(context)

    fun ensureModel(
        context: Context,
        onProgress: (Int) -> Unit = {},
        onReady: () -> Unit,
        onError: (String) -> Unit
    ) {
        ModelDownloader.downloadModel(context, onProgress, onReady, onError)
    }
}
