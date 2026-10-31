package education.mahmoud.quranyapp.feature.ayah_actions

import android.app.Activity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.material.bottomsheet.BottomSheetDialog
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.utils.NumberHelper
import education.mahmoud.quranyapp.utils.UiFont

/**
 * Options for one ayah, opened by a long press on it in the reader: tafseer, highlight as a
 * reference point (or remove it), copy, share and play its recitation when downloaded.
 */
object AyahActionsSheet {

    interface Listener {
        fun onShowTafseer(ayah: AyahItem)
        fun onToggleHighlight(ayah: AyahItem, highlight: Boolean)
        fun onPlay(ayah: AyahItem)
    }

    fun show(activity: Activity, ayah: AyahItem, highlighted: Boolean, night: Boolean, listener: Listener) {
        val palette = AyahSheetPalette(night)
        val view = LayoutInflater.from(activity).inflate(R.layout.sheet_ayah_actions, null)
        val dialog = BottomSheetDialog(activity)

        AyahSheetStyle.styleHandle(view.findViewById(R.id.sheetHandle), palette)

        val title = view.findViewById<TextView>(R.id.tvSheetTitle)
        UiFont.applyBold(title)
        title.setTextColor(palette.onSurface)
        title.text = AyahShare.suraName(ayah)

        val chip = view.findViewById<TextView>(R.id.chipAyah)
        AyahSheetStyle.styleChip(chip, palette)
        chip.text = activity.getString(R.string.ayah_chip_ayah, NumberHelper.getArabicNumber(ayah.ayahInSurahIndex))

        val ayahTv = view.findViewById<TextView>(R.id.tvSheetAyah)
        AyahSheetStyle.styleAyah(ayahTv, palette)
        ayahTv.text = NumberHelper.formatAyahWithNumber(ayah.text, ayah.ayahInSurahIndex).trim()

        val container = view.findViewById<LinearLayout>(R.id.actionsContainer)
        fun add(@DrawableRes icon: Int, @StringRes label: Int, tint: Int = palette.primary, action: () -> Unit) {
            val row = LayoutInflater.from(activity).inflate(R.layout.item_ayah_action, container, false)
            row.findViewById<ImageView>(R.id.imActionIcon).apply {
                setImageResource(icon)
                AyahSheetStyle.styleIcon(this, tint)
            }
            row.findViewById<TextView>(R.id.tvActionLabel).apply {
                UiFont.apply(this)
                setTextColor(palette.onSurface)
                setText(label)
            }
            row.setOnClickListener {
                dialog.dismiss()
                action()
            }
            container.addView(row)
        }

        add(R.drawable.ic_sheet_tafseer, R.string.ayah_action_tafseer) { listener.onShowTafseer(ayah) }
        if (highlighted) {
            add(R.drawable.ic_sheet_highlight, R.string.ayah_action_remove_highlight, palette.muted) {
                listener.onToggleHighlight(ayah, false)
            }
        } else {
            add(R.drawable.ic_sheet_highlight, R.string.ayah_action_highlight, HIGHLIGHT_ICON) {
                listener.onToggleHighlight(ayah, true)
            }
        }
        add(R.drawable.ic_sheet_copy, R.string.ayah_action_copy) { AyahShare.copy(activity, ayah, false) }
        add(R.drawable.ic_sheet_share, R.string.ayah_action_share) { AyahShare.share(activity, ayah, false) }
        if (ayah.audioPath != null) {
            add(R.drawable.ic_sheet_play, R.string.ayah_action_play) { listener.onPlay(ayah) }
        }

        dialog.setContentView(view)
        AyahSheetStyle.prepare(dialog, view, palette)
        dialog.show()
    }

    private const val HIGHLIGHT_ICON = 0xFFF2A900.toInt()
}
