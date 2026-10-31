package education.mahmoud.quranyapp.feature.asr

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest

/**
 * Thin entry point in the base module.
 * All real UI and logic live in the :quran_asr dynamic feature.
 */
object AsrEntryPoint {

    private const val MODULE_NAME = "quran_asr"
    private const val FEATURE_ACTIVITY =
        "education.mahmoud.quranyapp.asr.ui.RecitationTrackingActivity"
    private const val FUSED_MODULES_META = "com.android.dynamic.apk.fused.modules"

    fun launch(context: Context) {
        if (isModuleAvailable(context)) {
            startFeature(context)
            return
        }

        val request = SplitInstallRequest.newBuilder()
            .addModule(MODULE_NAME)
            .build()

        SplitInstallManagerFactory.create(context)
            .startInstall(request)
            .addOnSuccessListener {
                startFeature(context)
            }
    }

    /**
     * Play splits report the module via [SplitInstallManager][com.google.android.play.core.splitinstall.SplitInstallManager].
     * A sideloaded universal APK fuses :quran_asr into the base APK instead, so the
     * module is already present when the fused-modules metadata or feature class is.
     */
    private fun isModuleAvailable(context: Context): Boolean {
        val manager = SplitInstallManagerFactory.create(context)
        if (manager.installedModules.contains(MODULE_NAME)) {
            return true
        }
        if (isFusedIntoBaseApk(context)) {
            return true
        }
        return try {
            context.classLoader.loadClass(FEATURE_ACTIVITY)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    private fun isFusedIntoBaseApk(context: Context): Boolean {
        return try {
            val info = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val fused = info.metaData?.getString(FUSED_MODULES_META) ?: return false
            fused.split(",").any { it.trim() == MODULE_NAME }
        } catch (e: PackageManager.NameNotFoundException) {
            false
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
