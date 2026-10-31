package education.mahmoud.quranyapp.feature.ayah_actions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.utils.Data
import education.mahmoud.quranyapp.utils.NumberHelper

/** Copy / share text for an ayah, optionally with its tafseer. */
internal object AyahShare {

    fun suraName(ayah: AyahItem): String = Data.SURA_NAMES.getOrNull(ayah.surahIndex - 1).orEmpty()

    fun reference(context: Context, ayah: AyahItem): String =
        context.getString(R.string.ayah_reference, suraName(ayah), NumberHelper.getArabicNumber(ayah.ayahInSurahIndex))

    fun text(context: Context, ayah: AyahItem, withTafseer: Boolean): String = buildString {
        append("﴿").append(ayah.text.trim()).append("﴾")
        append("\n").append(reference(context, ayah))
        if (withTafseer && ayah.tafseer.isNotBlank()) {
            append("\n\n").append(context.getString(R.string.tafseer)).append(":\n")
            append(ayah.tafseer.trim())
        }
    }

    fun copy(context: Context, ayah: AyahItem, withTafseer: Boolean) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText(reference(context, ayah), text(context, ayah, withTafseer)))
        Toast.makeText(context, R.string.ayah_copied, Toast.LENGTH_SHORT).show()
    }

    fun share(context: Context, ayah: AyahItem, withTafseer: Boolean) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text(context, ayah, withTafseer))
        }
        context.startActivity(Intent.createChooser(send, context.getString(R.string.ayah_share_title)))
    }
}
