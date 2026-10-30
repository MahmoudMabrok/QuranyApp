package education.mahmoud.quranyapp.feature.showSuraAyas.mushaf

import education.mahmoud.quranyapp.utils.NumberHelper

/** Sura header look on reading pages, chosen in Settings. */
enum class SuraHeaderStyle(val id: Int) {
    /** Classic gold frame with a dark teal cartouche (like a printed Mushaf). */
    CLASSIC(0),

    /** Light modern teal pill. */
    MODERN(1),

    /** The old look: just the sura name as a text line. */
    PLAIN(2);

    companion object {
        val DEFAULT = CLASSIC
        fun fromId(id: Int): SuraHeaderStyle = values().firstOrNull { it.id == id } ?: DEFAULT
    }
}

/** What a sura header shows: name, order in the Mushaf, Makki/Madani and ayah count. */
data class SuraHeaderInfo(
    val index: Int,
    val name: String,
    val revelationType: String?,
    val numOfAyahs: Int,
) {
    val isMedinan: Boolean get() = revelationType.equals("Medinan", ignoreCase = true)

    /** "سورة الطارق" (names in the db may or may not already start with "سورة"). */
    val displayName: String
        get() {
            val clean = name.trim()
            return if (clean.startsWith("سورة") || clean.startsWith("سُورَةُ")) clean else "سورة $clean"
        }

    val revelationLabel: String get() = if (isMedinan) "مدنية" else "مكية"
    val orderLabel: String get() = "ترتيبها ${NumberHelper.getArabicNumber(index)}"
    val ayahCountArabic: String get() = NumberHelper.getArabicNumber(numOfAyahs)
}

/**
 * Start of a hizb quarter on a page.
 * @param ayahPosition position of the ayah that starts the quarter inside the page ayah list.
 * @param hizbQuarter global quarter number 1..240 (as stored on ayahs).
 */
data class HizbMark(val ayahPosition: Int, val hizbQuarter: Int) {
    /** 0 = start of a hizb, 1 = ¼, 2 = ½, 3 = ¾ */
    val quarterInHizb: Int get() = (hizbQuarter - 1) % 4
    val hizbNumber: Int get() = (hizbQuarter - 1) / 4 + 1

    /** Top line of the medallion, e.g. "الحزب" or "½ الحزب". */
    val title: String
        get() = when (quarterInHizb) {
            1 -> "¼ الحزب"
            2 -> "½ الحزب"
            3 -> "¾ الحزب"
            else -> "الحزب"
        }

    val numberArabic: String get() = NumberHelper.getArabicNumber(hizbNumber)

    companion object {
        /**
         * Finds the first hizb quarter that starts on a page.
         * @param previousQuarter quarter of the last ayah of the previous page (0 for the first page).
         */
        fun find(quarters: List<Int>, previousQuarter: Int): HizbMark? {
            var prev = previousQuarter
            quarters.forEachIndexed { i, q ->
                if (q > 0 && q != prev) return HizbMark(i, q)
                prev = q
            }
            return null
        }
    }
}

object JuzNames {
    private val names = listOf(
        "الأول", "الثاني", "الثالث", "الرابع", "الخامس", "السادس", "السابع", "الثامن", "التاسع", "العاشر",
        "الحادي عشر", "الثاني عشر", "الثالث عشر", "الرابع عشر", "الخامس عشر", "السادس عشر", "السابع عشر",
        "الثامن عشر", "التاسع عشر", "العشرون", "الحادي والعشرون", "الثاني والعشرون", "الثالث والعشرون",
        "الرابع والعشرون", "الخامس والعشرون", "السادس والعشرون", "السابع والعشرون", "الثامن والعشرون",
        "التاسع والعشرون", "الثلاثون",
    )

    /** "الجزء الثلاثون" */
    fun of(juz: Int): String = if (juz in 1..30) "الجزء ${names[juz - 1]}" else "الجزء ${NumberHelper.getArabicNumber(juz)}"
}

/** In the printed Mushaf odd pages are on the right, even pages on the left. */
fun isRightPage(pageNum: Int): Boolean = pageNum % 2 == 1
