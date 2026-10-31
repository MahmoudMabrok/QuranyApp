package education.mahmoud.quranyapp.utils

class NumberHelper {

    companion object {

        private const val ARABIC_INDIC_ZERO = '\u0660'

        /** Ornate parentheses used by me_quran to draw the closed ayah medallion. */
        private const val AYAH_OPEN = '\uFD3F' // ﴿
        private const val AYAH_CLOSE = '\uFD3E' // ﴾

        /** No-break space: keeps the marker on the same line as its ayah. */
        private const val NBSP = '\u00A0'

        /**
         * Always returns Arabic-Indic digits (٠-٩).
         *
         * Do not rely on Locale("ar"): newer ICU/CLDR data (Android 14+) formats it with
         * Latin digits, which me_quran.ttf has no glyphs for. The fallback font then breaks
         * the ornate-bracket shaping and the ayah marker renders as two open halves.
         */
        fun getArabicNumber(num: Int): String =
            num.toString().map { c ->
                if (c in '0'..'9') ARABIC_INDIC_ZERO + (c - '0') else c
            }.joinToString("")

        /** Same as [getArabicNumber] for Long values (e.g. the saved points total). */
        fun getArabicNumber(num: Long): String =
            num.toString().map { c ->
                if (c in '0'..'9') ARABIC_INDIC_ZERO + (c - '0') else c
            }.joinToString("")

        /** Ayah text followed by a closed, non-wrapping ayah-number marker, e.g. "… ﴿١٢﴾ ". */
        fun formatAyahWithNumber(text: String, ayahNumber: Int): String =
            "${text.trimEnd()}$NBSP$AYAH_OPEN${getArabicNumber(ayahNumber)}$AYAH_CLOSE "
    }
}
