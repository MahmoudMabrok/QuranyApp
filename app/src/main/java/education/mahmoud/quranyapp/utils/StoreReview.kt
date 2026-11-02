package education.mahmoud.quranyapp.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AlertDialog
import education.mahmoud.quranyapp.R

/**
 * Asks for a Play Store rating two days after install.
 * Skipping it schedules the next ask after a week, then after two weeks.
 * Opening the store, from the dialog or from settings, stops the asks.
 */
object StoreReview {

    private const val PREFS = "store_review"
    private const val STAGE = "stage"
    private const val NEXT_AT = "next_at"
    private const val WENT = "went_to_store"
    private const val DAY = 24L * 60L * 60L * 1000L
    private val delays = longArrayOf(2 * DAY, 7 * DAY, 14 * DAY)

    private var dialog: AlertDialog? = null

    fun versionName(context: Context): String {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.PackageInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        return info.versionName.orEmpty()
    }

    fun maybeAsk(activity: Activity) {
        if (activity.isFinishing || dialog?.isShowing == true) return
        val prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(WENT, false)) return
        val stage = prefs.getInt(STAGE, 0)
        if (stage >= delays.size) return
        val nextAt = prefs.getLong(NEXT_AT, 0L)
        val due = if (nextAt == 0L) installTime(activity) + delays[0] else nextAt
        if (System.currentTimeMillis() < due) {
            if (nextAt == 0L) prefs.edit().putLong(NEXT_AT, due).apply()
            return
        }
        dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.review_title)
            .setMessage(R.string.review_message)
            .setPositiveButton(R.string.review_rate) { _, _ -> openStore(activity) }
            .setNegativeButton(R.string.review_later) { _, _ -> postpone(activity) }
            .setOnCancelListener { postpone(activity) }
            .create()
            .also { it.show() }
    }

    fun openStore(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = context.packageName
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id"))
            .setPackage("com.android.vending")
        val opened = try {
            context.startActivity(market)
            true
        } catch (_: ActivityNotFoundException) {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$id")),
                )
                true
            } catch (_: ActivityNotFoundException) {
                false
            }
        }
        if (opened) prefs.edit().putBoolean(WENT, true).apply()
    }

    private fun postpone(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stage = prefs.getInt(STAGE, 0) + 1
        val edit = prefs.edit().putInt(STAGE, stage)
        if (stage < delays.size) {
            edit.putLong(NEXT_AT, System.currentTimeMillis() + delays[stage])
        }
        edit.apply()
    }

    private fun installTime(context: Context): Long {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.PackageInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        return info.firstInstallTime
    }
}
