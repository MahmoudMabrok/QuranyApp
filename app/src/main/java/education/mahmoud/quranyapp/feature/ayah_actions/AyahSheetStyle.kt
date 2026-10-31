package education.mahmoud.quranyapp.feature.ayah_actions

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.utils.EdgeToEdge
import education.mahmoud.quranyapp.utils.UiFont

/**
 * Colours for the ayah bottom sheets. The XML screens have no night resources, so the sheets
 * pick their palette in code: the reader passes its own night-mode setting, other screens
 * follow the system theme (like the Compose screens).
 */
internal class AyahSheetPalette(val night: Boolean) {
    val surface = if (night) 0xFF1C1F1D.toInt() else Color.WHITE
    val onSurface = if (night) 0xFFEDEDED.toInt() else 0xFF1B1B1B.toInt()
    val muted = if (night) 0xFFA9B4AF.toInt() else 0xFF5F6B66.toInt()
    val primary = if (night) 0xFF79BD8F.toInt() else 0xFF00A388.toInt()
    val chipBg = if (night) 0xFF23302B.toInt() else 0xFFE6F6F2.toInt()
    val chipText = if (night) 0xFFBEEB9F.toInt() else 0xFF00735F.toInt()
    val ayahCard = if (night) 0xFF252B28.toInt() else 0xFFF4F8F1.toInt()
    val ayahText = if (night) Color.WHITE else Color.BLACK
    val handle = if (night) 0xFF4A544F.toInt() else 0xFFCFD6D2.toInt()

    companion object {
        /** Reader-independent screens follow the system light / dark theme. */
        fun forSystem(activity: Activity) = AyahSheetPalette(EdgeToEdge.isSystemNight(activity))
    }
}

internal object AyahSheetStyle {

    fun dp(view: View, value: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, view.resources.displayMetrics)

    fun rounded(view: View, color: Int, radiusDp: Float): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(view, radiusDp)
        }

    fun styleHandle(handle: View, palette: AyahSheetPalette) {
        handle.background = rounded(handle, palette.handle, 2f)
    }

    /** Pill label with system-font digits ("الآية ٦١"). */
    fun styleChip(chip: TextView, palette: AyahSheetPalette) {
        UiFont.apply(chip)
        chip.setTextColor(palette.chipText)
        chip.background = rounded(chip, palette.chipBg, 50f)
    }

    fun styleIcon(icon: ImageView, color: Int) {
        icon.imageTintList = ColorStateList.valueOf(color)
    }

    /** Ayah text in the Quran font on a soft card. */
    fun styleAyah(tv: TextView, palette: AyahSheetPalette) {
        ResourcesCompat.getFont(tv.context, R.font.me_quran)?.let { tv.typeface = it }
        tv.setTextColor(palette.ayahText)
        tv.background = rounded(tv, palette.ayahCard, 16f)
    }

    /**
     * Rounded top corners in the palette colour, opens fully expanded and keeps the content
     * above the navigation bar. [maxHeightFraction] caps the sheet so long tafseer scrolls.
     */
    fun prepare(dialog: BottomSheetDialog, content: View, palette: AyahSheetPalette, maxHeightFraction: Float = 0.88f) {
        EdgeToEdge.applyPadding(content, top = false)
        dialog.setOnShowListener {
            val sheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return@setOnShowListener
            val radius = dp(sheet, 24f)
            sheet.background = GradientDrawable().apply {
                setColor(palette.surface)
                cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
            }
            sheet.clipToOutline = true
        }
        dialog.behavior.apply {
            maxHeight = (content.resources.displayMetrics.heightPixels * maxHeightFraction).toInt()
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
}
