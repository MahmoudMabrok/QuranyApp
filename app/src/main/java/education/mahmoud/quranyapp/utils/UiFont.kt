package education.mahmoud.quranyapp.utils

import android.graphics.Typeface
import android.widget.TextView

/**
 * The app theme sets `fontFamily` to me_quran for every view. That font is made for Quran
 * text: it has no Arabic-Indic digits for UI use and almost no word spacing in plain labels
 * ("عددالصفحات"). Plain UI labels (headers, chips, empty states) use the system sans font,
 * the same one the Compose screens use.
 *
 * The theme attribute wins over `android:fontFamily` in XML, so this is applied in code.
 */
object UiFont {
    @JvmStatic
    fun apply(vararg views: TextView) {
        views.forEach { tv ->
            val bold = tv.typeface?.isBold == true || (tv.typeface?.style ?: 0) and Typeface.BOLD != 0
            tv.typeface = Typeface.create(Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
        }
    }

    @JvmStatic
    fun applyBold(vararg views: TextView) {
        views.forEach { it.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) }
    }
}
