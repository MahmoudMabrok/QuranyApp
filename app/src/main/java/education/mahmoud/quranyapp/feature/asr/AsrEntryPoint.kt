package education.mahmoud.quranyapp.feature.asr

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import education.mahmoud.quranyapp.R

/**
 * Thin entry point in the base module.
 * All real UI and logic live in the :quran_asr dynamic feature.
 */
object AsrEntryPoint {

    const val EXTRA_RANGE_LABEL = "asr_range_label"
    const val EXTRA_AYAH_TEXT = "asr_ayah_text"
    const val EXTRA_START_SURA = "asr_start_sura"
    const val EXTRA_START_AYAH = "asr_start_ayah"
    const val EXTRA_END_SURA = "asr_end_sura"
    const val EXTRA_END_AYAH = "asr_end_ayah"

    private const val MODULE_NAME = "quran_asr"
    private const val FEATURE_ACTIVITY =
        "education.mahmoud.quranyapp.asr.ui.RecitationTrackingActivity"
    private const val FUSED_MODULES_META = "com.android.dynamic.apk.fused.modules"
    private const val CONFIRM_REQUEST = 0xA5A

    /** Surah/ayah range chosen on the memorization test screen. Indexes are 1-based. */
    data class RecitationRange(
        val startSura: Int,
        val startAyah: Int,
        val endSura: Int,
        val endAyah: Int,
        val label: String,
        val ayahText: String? = null,
    )

    private var installInProgress = false

    fun launch(context: Context, range: RecitationRange? = null) {
        if (isModuleAvailable(context)) {
            startFeature(context, range)
            return
        }
        requestInstall(context, range)
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

    private fun requestInstall(context: Context, range: RecitationRange?) {
        if (installInProgress) return
        val manager = SplitInstallManagerFactory.create(context)
        val dialog = installDialog(context)
        installInProgress = true
        var sessionId = 0
        var finished = false
        var listener: SplitInstallStateUpdatedListener? = null

        listener = SplitInstallStateUpdatedListener { state ->
            if (sessionId != 0 && state.sessionId() != sessionId) return@SplitInstallStateUpdatedListener
            when (state.status()) {
                SplitInstallSessionStatus.DOWNLOADING -> {
                    val total = state.totalBytesToDownload()
                    val percent = if (total > 0) ((state.bytesDownloaded() * 100) / total).toInt() else 0
                    dialog?.setMessage(context.getString(R.string.asr_downloading, percent))
                }
                SplitInstallSessionStatus.INSTALLING ->
                    dialog?.setMessage(context.getString(R.string.asr_installing))
                SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                    dialog?.setMessage(context.getString(R.string.asr_install_confirm))
                    val activity = context.findActivity()
                    if (activity != null) {
                        runCatching {
                            manager.startConfirmationDialogForResult(state, activity, CONFIRM_REQUEST)
                        }
                    }
                }
                SplitInstallSessionStatus.INSTALLED -> {
                    finished = true
                    cleanup(manager, listener, dialog)
                    startFeature(context, range)
                }
                SplitInstallSessionStatus.FAILED,
                SplitInstallSessionStatus.CANCELED -> {
                    finished = true
                    cleanup(manager, listener, dialog)
                    if (state.status() == SplitInstallSessionStatus.FAILED) {
                        toast(context, R.string.asr_install_failed)
                    }
                }
            }
        }
        dialog?.setOnDismissListener {
            if (!finished) {
                finished = true
                if (sessionId != 0) runCatching { manager.cancelInstall(sessionId) }
                listener?.let { runCatching { manager.unregisterListener(it) } }
                installInProgress = false
            }
        }
        dialog?.show()
        manager.registerListener(listener!!)
        manager.startInstall(
            SplitInstallRequest.newBuilder().addModule(MODULE_NAME).build()
        ).addOnSuccessListener { id ->
            sessionId = id
        }.addOnFailureListener { error ->
            finished = true
            cleanup(manager, listener, dialog)
            toast(context, installErrorMessage(error))
        }
    }

    private fun installDialog(context: Context): AlertDialog? {
        val activity = context.findActivity() ?: return null
        if (activity.isFinishing) return null
        return AlertDialog.Builder(activity)
            .setTitle(R.string.asr_install_title)
            .setMessage(R.string.asr_install_message)
            .setNegativeButton(R.string.asr_install_cancel, null)
            .create()
    }

    private fun cleanup(
        manager: SplitInstallManager,
        listener: SplitInstallStateUpdatedListener?,
        dialog: AlertDialog?,
    ) {
        installInProgress = false
        listener?.let { runCatching { manager.unregisterListener(it) } }
        if (dialog?.isShowing == true) dialog.dismiss()
    }

    private fun installErrorMessage(error: Exception): Int {
        val code = (error as? SplitInstallException)?.errorCode
        return when (code) {
            SplitInstallErrorCode.NETWORK_ERROR -> R.string.asr_install_network
            SplitInstallErrorCode.MODULE_UNAVAILABLE,
            SplitInstallErrorCode.API_NOT_AVAILABLE,
            SplitInstallErrorCode.PLAY_STORE_NOT_FOUND,
            SplitInstallErrorCode.ACCESS_DENIED -> R.string.asr_install_unavailable
            else -> R.string.asr_install_failed
        }
    }

    private fun toast(context: Context, message: Int) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun Context.findActivity(): Activity? {
        var current = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    private fun startFeature(context: Context, range: RecitationRange?) {
        val intent = Intent().setClassName(context.packageName, FEATURE_ACTIVITY)
        if (range != null) {
            intent.putExtra(EXTRA_RANGE_LABEL, range.label)
            intent.putExtra(EXTRA_AYAH_TEXT, range.ayahText)
            intent.putExtra(EXTRA_START_SURA, range.startSura)
            intent.putExtra(EXTRA_START_AYAH, range.startAyah)
            intent.putExtra(EXTRA_END_SURA, range.endSura)
            intent.putExtra(EXTRA_END_AYAH, range.endAyah)
        }
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
