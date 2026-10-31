package education.mahmoud.quranyapp.feature.asr

import android.content.Context
import android.content.Intent
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest

/**
 * Thin entry point in the base module.
 * All real UI and logic live in the :quran_asr dynamic feature.
 */
object AsrEntryPoint {

    private const val MODULE_NAME = "quran_asr"

    fun launch(context: Context) {
        val manager = SplitInstallManagerFactory.create(context)

        if (manager.installedModules.contains(MODULE_NAME)) {
            startFeature(context)
            return
        }

        val request = SplitInstallRequest.newBuilder()
            .addModule(MODULE_NAME)
            .build()

        manager.startInstall(request)
            .addOnSuccessListener {
                startFeature(context)
            }
    }

    private fun startFeature(context: Context) {
        // Prefer tracking screen; download screen is shown internally if model missing
        val intent = Intent().setClassName(
            context.packageName,
            "education.mahmoud.quranyapp.asr.ui.RecitationTrackingActivity"
        )
        context.startActivity(intent)
    }
}
