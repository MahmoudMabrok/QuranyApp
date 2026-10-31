package education.mahmoud.quranyapp.feature.ayah_actions

import android.app.Activity
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.utils.NumberHelper
import education.mahmoud.quranyapp.utils.UiFont

/**
 * Bottom sheet with the tafseer of one ayah: sura name, ayah / page / juz chips, the ayah in
 * the Quran font and the tafseer in a readable system font. Replaces the old green dialog.
 */
object TafseerSheet {

    @JvmStatic
    fun show(activity: Activity, ayah: AyahItem) = show(activity, ayah, AyahSheetPalette.forSystem(activity).night)

    @JvmStatic
    fun show(activity: Activity, ayah: AyahItem, night: Boolean) {
        val palette = AyahSheetPalette(night)
        val view = LayoutInflater.from(activity).inflate(R.layout.sheet_ayah_tafseer, null)
        val dialog = BottomSheetDialog(activity)

        AyahSheetStyle.styleHandle(view.findViewById(R.id.sheetHandle), palette)

        val title = view.findViewById<TextView>(R.id.tvSheetTitle)
        UiFont.applyBold(title)
        title.setTextColor(palette.onSurface)
        title.text = AyahShare.suraName(ayah)

        chip(view.findViewById(R.id.chipAyah), activity.getString(R.string.ayah_chip_ayah, NumberHelper.getArabicNumber(ayah.ayahInSurahIndex)), palette)
        chip(view.findViewById(R.id.chipPage), activity.getString(R.string.ayah_chip_page, NumberHelper.getArabicNumber(ayah.pageNum)), palette)
        chip(view.findViewById(R.id.chipJuz), activity.getString(R.string.ayah_chip_juz, NumberHelper.getArabicNumber(ayah.juz)), palette)

        val ayahTv = view.findViewById<TextView>(R.id.tvSheetAyah)
        AyahSheetStyle.styleAyah(ayahTv, palette)
        ayahTv.text = NumberHelper.formatAyahWithNumber(ayah.text, ayah.ayahInSurahIndex).trim()

        val label = view.findViewById<TextView>(R.id.tvTafseerLabel)
        UiFont.applyBold(label)
        label.setTextColor(palette.primary)

        val body = view.findViewById<TextView>(R.id.tvSheetTafseer)
        UiFont.apply(body)
        body.setTextColor(if (ayah.tafseer.isBlank()) palette.muted else palette.onSurface)
        body.text = ayah.tafseer.trim().ifEmpty { activity.getString(R.string.tafseer_empty) }

        val hasTafseer = ayah.tafseer.isNotBlank()
        view.findViewById<ImageButton>(R.id.btnSheetCopy).apply {
            AyahSheetStyle.styleIcon(this, palette.muted)
            setOnClickListener { AyahShare.copy(activity, ayah, hasTafseer) }
        }
        view.findViewById<ImageButton>(R.id.btnSheetShare).apply {
            AyahSheetStyle.styleIcon(this, palette.muted)
            setOnClickListener { AyahShare.share(activity, ayah, hasTafseer) }
        }
        view.findViewById<ImageButton>(R.id.btnSheetClose).apply {
            AyahSheetStyle.styleIcon(this, palette.muted)
            setOnClickListener { dialog.dismiss() }
        }

        dialog.setContentView(view)
        AyahSheetStyle.prepare(dialog, view, palette)
        dialog.show()
    }

    private fun chip(tv: TextView, text: String, palette: AyahSheetPalette) {
        AyahSheetStyle.styleChip(tv, palette)
        tv.text = text
    }
}
