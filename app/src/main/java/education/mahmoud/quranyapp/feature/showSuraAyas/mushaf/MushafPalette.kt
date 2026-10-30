package education.mahmoud.quranyapp.feature.showSuraAyas.mushaf

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import education.mahmoud.quranyapp.R

/** Colours used to draw the sura headers and the page frame. */
data class MushafPalette(
    @ColorInt val paper: Int,
    @ColorInt val ink: Int,
    /** frame lines and ornaments */
    @ColorInt val line: Int,
    /** fill inside header boxes and medallions */
    @ColorInt val cream: Int,
    /** cartouche / pill fill */
    @ColorInt val cartouche: Int,
    /** text on the cartouche / pill */
    @ColorInt val cartoucheText: Int,
    /** fill of the ornamental band */
    @ColorInt val band: Int,
    /** light fill of the modern header */
    @ColorInt val tint: Int,
    /** value text (Makki/Madani, ayah count) */
    @ColorInt val value: Int,
    /** small captions */
    @ColorInt val muted: Int,
) {
    companion object {
        /**
         * @param style header style; the frame follows the same palette (plain uses the classic gold frame).
         * @param paper page background colour (user's choice or night background).
         */
        fun of(style: SuraHeaderStyle, night: Boolean, @ColorInt paper: Int, @ColorInt ink: Int): MushafPalette {
            val modern = style == SuraHeaderStyle.MODERN
            return when {
                night && modern -> MushafPalette(
                    paper = paper, ink = ink, line = c("#3CC4A8"), cream = c("#1A211D"), cartouche = c("#3CC4A8"),
                    cartoucheText = c("#08281F"), band = c("#16302A"), tint = c("#16302A"), value = c("#3CC4A8"),
                    muted = c("#9FB8B0"),
                )
                night -> MushafPalette(
                    paper = paper, ink = ink, line = c("#C3A04A"), cream = c("#1A211D"), cartouche = c("#0F5245"),
                    cartoucheText = c("#F3D98B"), band = c("#1E2A24"), tint = c("#16302A"), value = c("#C3A04A"),
                    muted = c("#A89A70"),
                )
                modern -> MushafPalette(
                    paper = paper, ink = ink, line = c("#00A388"), cream = c("#FFFFFF"), cartouche = c("#00A388"),
                    cartoucheText = Color.WHITE, band = c("#E6F5F1"), tint = c("#E6F5F1"), value = c("#00A388"),
                    muted = c("#5F7A72"),
                )
                else -> MushafPalette(
                    paper = paper, ink = ink, line = c("#B8902F"), cream = c("#FFF6DE"), cartouche = c("#0F5245"),
                    cartoucheText = c("#F3D98B"), band = c("#FFF1CC"), tint = c("#E6F5F1"), value = c("#0F5245"),
                    muted = c("#7A6A43"),
                )
            }
        }

        private fun c(hex: String) = Color.parseColor(hex)
    }
}

/** Typefaces bundled with the app, loaded once. */
class MushafFonts private constructor(
    val name: Typeface,
    val caption: Typeface,
    val value: Typeface,
    val valueBold: Typeface,
) {
    companion object {
        @Volatile
        private var instance: MushafFonts? = null

        fun get(context: Context): MushafFonts = instance ?: synchronized(this) {
            instance ?: load(context.applicationContext).also { instance = it }
        }

        private fun load(context: Context): MushafFonts {
            fun font(id: Int, fallback: Typeface) = try {
                ResourcesCompat.getFont(context, id) ?: fallback
            } catch (e: Exception) {
                fallback
            }
            return MushafFonts(
                name = font(R.font.kfgqp, Typeface.SERIF),
                caption = font(R.font.ge_medium, Typeface.DEFAULT),
                value = font(R.font.amiri_regular, Typeface.SERIF),
                valueBold = font(R.font.ge_bold, Typeface.DEFAULT_BOLD),
            )
        }
    }
}
