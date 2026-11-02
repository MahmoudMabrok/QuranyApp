package education.mahmoud.quranyapp.asr

import android.content.Context
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineZipformer2CtcModelConfig

/**
 * Loads the streaming Zipformer CTC model once and keeps it for the next recitation.
 */
object RecitationEngine {

    private val lock = Any()
    private var recognizer: OnlineRecognizer? = null

    fun get(context: Context): OnlineRecognizer {
        synchronized(lock) {
            recognizer?.let { return it }
            val app = context.applicationContext
            val created = OnlineRecognizer(
                assetManager = null,
                config = OnlineRecognizerConfig(
                    modelConfig = OnlineModelConfig(
                        zipformer2Ctc = OnlineZipformer2CtcModelConfig(
                            model = ModelDownloader.getModelFile(app).absolutePath,
                        ),
                        tokens = ModelDownloader.getTokensFile(app).absolutePath,
                        numThreads = 2,
                        modelType = "zipformer2",
                    ),
                ),
            )
            recognizer = created
            return created
        }
    }
}
