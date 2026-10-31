package education.mahmoud.quranyapp.asr

import android.content.Context
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import java.io.File

object ModelDownloader {

    private const val MODEL_URL =
        "https://drive.google.com/uc?export=download&confirm=t&id=1d2aDGa-wLbAfj0AIJHmeHZ4-n9GT2Sal"
    private const val MODEL_NAME = "zipformer_p_arabic_v3.1.int8.onnx"
    private const val TOKENS_NAME = "tokens.txt"

    fun getModelDir(context: Context): File {
        return File(context.filesDir, "asr_model").apply { mkdirs() }
    }

    fun getModelFile(context: Context): File {
        return File(getModelDir(context), MODEL_NAME)
    }

    fun isModelReady(context: Context): Boolean {
        return getModelFile(context).exists()
    }

    fun downloadModel(
        context: Context,
        onProgress: (Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val modelFile = getModelFile(context)
        if (modelFile.exists()) {
            onComplete()
            return
        }

        PRDownloader.download(MODEL_URL, modelFile.parent, modelFile.name)
            .build()
            .setOnProgressListener { progress ->
                val percent = if (progress.totalBytes > 0)
                    (progress.currentBytes * 100 / progress.totalBytes).toInt()
                else 0
                onProgress(percent)
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    onComplete()
                }

                override fun onError(error: Error?) {
                    onError(error?.serverErrorMessage ?: "Download failed")
                }
            })
    }
}
