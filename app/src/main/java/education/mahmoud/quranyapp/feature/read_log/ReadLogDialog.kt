package education.mahmoud.quranyapp.feature.read_log

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.utils.UiFont

/**
 * Details of one reading-log day: the full list of pages (the card shows two lines at most).
 */
object ReadLogDialog {

    @JvmStatic
    fun show(context: Context, date: String, count: String, ranges: String) {
        val dialog = AlertDialog.Builder(context)
            .setTitle(date)
            .setMessage(context.getString(R.string.read_log_dialog_message, count, ranges))
            .setPositiveButton(R.string.read_log_dialog_ok, null)
            .create()
        dialog.setOnShowListener {
            dialog.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_RTL
            listOfNotNull(
                dialog.findViewById<TextView>(android.R.id.message),
                dialog.getButton(AlertDialog.BUTTON_POSITIVE),
            ).forEach { UiFont.apply(it) }
            dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)?.let { UiFont.applyBold(it) }
        }
        dialog.show()
    }
}
