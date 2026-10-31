package education.mahmoud.quranyapp.feature.asr

import android.content.Context
import kotlin.jvm.functions.Function0
import kotlin.jvm.functions.Function1

/**
 * Base-module entry for the on-demand ASR model.
 *
 * ModelDownloader lives in the :quran_asr dynamic feature. The feature depends on
 * the base module, so the base cannot take a compile-time dependency on it.
 * Calls are dispatched once the split classloader can see that class.
 */
object AsrFeatureManager {

    private const val DOWNLOADER = "education.mahmoud.quranyapp.asr.ModelDownloader"

    fun isReady(context: Context): Boolean {
        return try {
            val downloader = downloaderInstance()
            val method = downloader.javaClass.getMethod("isModelReady", Context::class.java)
            method.invoke(downloader, context) as Boolean
        } catch (e: ClassNotFoundException) {
            false
        } catch (e: ReflectiveOperationException) {
            false
        }
    }

    fun ensureModel(
        context: Context,
        onProgress: (Int) -> Unit = {},
        onReady: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val downloader = downloaderInstance()
            val method = downloader.javaClass.getMethod(
                "downloadModel",
                Context::class.java,
                Function1::class.java,
                Function0::class.java,
                Function1::class.java
            )
            method.invoke(
                downloader,
                context,
                object : Function1<Int, Unit> {
                    override fun invoke(percent: Int) = onProgress(percent)
                },
                object : Function0<Unit> {
                    override fun invoke() = onReady()
                },
                object : Function1<String, Unit> {
                    override fun invoke(message: String) = onError(message)
                }
            )
        } catch (e: ClassNotFoundException) {
            onError("Quran ASR module is not installed")
        } catch (e: ReflectiveOperationException) {
            onError(e.cause?.message ?: e.message ?: "Failed to download ASR model")
        }
    }

    private fun downloaderInstance(): Any {
        val clazz = Class.forName(DOWNLOADER)
        return clazz.getField("INSTANCE").get(null)
            ?: error("ModelDownloader INSTANCE is null")
    }
}
