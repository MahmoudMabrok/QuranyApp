package education.mahmoud.quranyapp.feature.read_log

import education.mahmoud.quranyapp.utils.NumberHelper
import java.util.Calendar

/**
 * Pure text helpers for the reading log rows (no Android types, unit tested).
 */
object ReadLogFormat {

    private const val ARABIC_COMMA = "، "
    private const val EN_DASH = "–"

    private val MONTHS = arrayOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر",
    )

    private val DAYS = arrayOf(
        "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت",
    )

    /**
     * Sorted page numbers grouped into runs: [1,2,3,7,9,10] -> "١–٣، ٧، ٩–١٠".
     */
    fun pageRanges(pages: Collection<Int>?): String {
        val sorted = pages.orEmpty().filter { it > 0 }.toSortedSet().toList()
        if (sorted.isEmpty()) return ""
        val parts = mutableListOf<String>()
        var start = sorted[0]
        var prev = start
        fun close() {
            parts += if (start == prev) {
                NumberHelper.getArabicNumber(start)
            } else {
                NumberHelper.getArabicNumber(start) + EN_DASH + NumberHelper.getArabicNumber(prev)
            }
        }
        for (i in 1 until sorted.size) {
            val p = sorted[i]
            if (p != prev + 1) {
                close()
                start = p
            }
            prev = p
        }
        close()
        return parts.joinToString(ARABIC_COMMA)
    }

    /**
     * Count of pages with the Arabic number agreement:
     * 1 صفحة واحدة, 2 صفحتان, 3–10 صفحات, 11+ صفحة.
     */
    fun pagesCount(count: Int): String = when {
        count <= 0 -> "لا صفحات"
        count == 1 -> "صفحة واحدة"
        count == 2 -> "صفحتان"
        count % 100 in 3..10 -> "${NumberHelper.getArabicNumber(count)} صفحات"
        else -> "${NumberHelper.getArabicNumber(count)} صفحة"
    }

    /** Same agreement rules for days: يوم واحد, يومان, ٣ أيام, ١١ يومًا. */
    fun daysCount(count: Int): String = when {
        count <= 0 -> "لا أيام"
        count == 1 -> "يوم واحد"
        count == 2 -> "يومان"
        count % 100 in 3..10 -> "${NumberHelper.getArabicNumber(count)} أيام"
        else -> "${NumberHelper.getArabicNumber(count)} يومًا"
    }

    /**
     * "الجمعة ٣٠ أكتوبر ٢٠٢٦" built by hand (Locale("ar") formats with Latin digits on
     * newer Android versions, and month names differ between regions).
     */
    fun fullDate(millis: Long, calendar: Calendar = Calendar.getInstance()): String {
        calendar.timeInMillis = millis
        val day = DAYS[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        val dayOfMonth = NumberHelper.getArabicNumber(calendar.get(Calendar.DAY_OF_MONTH))
        val month = MONTHS[calendar.get(Calendar.MONTH)]
        val year = NumberHelper.getArabicNumber(calendar.get(Calendar.YEAR))
        return "$day $dayOfMonth $month $year"
    }

    /** "اليوم" / "أمس" for the two most recent days, otherwise null. */
    fun relativeDay(millis: Long, now: Long = System.currentTimeMillis()): String? {
        val day = Calendar.getInstance().apply { timeInMillis = millis }
        val today = Calendar.getInstance().apply { timeInMillis = now }
        if (sameDay(day, today)) return "اليوم"
        day.add(Calendar.DAY_OF_YEAR, 1)
        if (sameDay(day, today)) return "أمس"
        return null
    }

    private fun sameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}
