package education.mahmoud.quranyapp.feature.showSuraAyas

import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.HizbMark
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderInfo
import education.mahmoud.quranyapp.utils.NumberHelper
import education.mahmoud.quranyapp.utils.getSuraNameFromIndex

data class Page(
    var ayhas: List<AyahItem>,
    var pageNum: Int = 0,
    var rubHizb: Int = 0,
    var juz: Int = 0,
    /** Header info (Makki/Madani, ayah count, order) for suras that start on this page. */
    var suraInfos: Map<Int, SuraHeaderInfo> = emptyMap(),
    /** First hizb quarter that starts on this page, if any. */
    var hizbMark: HizbMark? = null,
) {

    /** Where a sura-name line sits inside [getText], so it can be drawn as a Mushaf header. */
    data class HeaderRange(val surahIndex: Int, val start: Int, val end: Int)

    /**
     * Page text plus the ranges of the sura-name lines and the start offset of every ayah.
     * [ayahStarts] includes a sura header / Basmala before an ayah 1 (used for touch lookup);
     * [ayahTextRanges] covers only the ayah words and its number marker (used for highlights).
     */
    class Content(
        val text: String,
        val headers: List<HeaderRange>,
        val ayahStarts: List<Int>,
        val ayahTextRanges: List<IntRange> = emptyList(),
    ) {
        /** Index in [ayhas] of the ayah at [offset] in [text], or -1. */
        fun ayahAt(offset: Int): Int {
            if (ayahStarts.isEmpty() || offset < 0) return -1
            var found = -1
            for (i in ayahStarts.indices) {
                if (ayahStarts[i] <= offset) found = i else break
            }
            return found
        }
    }

    fun getText(): String = buildContent().text

    fun buildContent(): Content {
        val builder = StringBuilder()
        val headers = mutableListOf<HeaderRange>()
        val ayahStarts = mutableListOf<Int>()
        val ayahTextRanges = mutableListOf<IntRange>()
        var aya: String
        var isFirst = true
        for (ayahItem in ayhas) {
            aya = ayahItem.text
            // add sura name
            if (ayahItem.ayahInSurahIndex == 1) {
                val tempSuraName = ayahItem.surahIndex.getSuraNameFromIndex()
                // handle first name in page that not need a previous new line
                if (!isFirst) builder.append("\n")
                ayahStarts.add(builder.length)
                val start = builder.length
                builder.append(tempSuraName)
                headers.add(HeaderRange(ayahItem.surahIndex, start, builder.length))
                builder.append("\n")
                // AlFatiha(index = 1 ) has the Basmallah as its first ayah; others carry it before ayah 1
                if (ayahItem.surahIndex != 1) {
                    val split = splitBasmala(aya)
                    if (split != null) {
                        builder.append(split.first)
                        builder.append("\n")
                        aya = split.second
                    }
                }
            } else {
                ayahStarts.add(builder.length)
            }
            isFirst = false
            val textStart = builder.length
            val formatted = NumberHelper.formatAyahWithNumber(aya, ayahItem.ayahInSurahIndex)
            builder.append(formatted)
            // without the trailing space, so neighbouring highlights do not merge
            ayahTextRanges.add(textStart until textStart + formatted.trimEnd().length)
        }
        return Content(builder.toString(), headers, ayahStarts, ayahTextRanges)
    }

    companion object {
        private val DIACRITICS = Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")

        /**
         * Splits "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ <ayah>" into the Basmala (first four words)
         * and the ayah text, whatever diacritics order the text uses. Returns null when the
         * ayah does not start with a Basmala.
         */
        fun splitBasmala(aya: String): Pair<String, String>? {
            val words = aya.trim().split(" ", limit = 5)
            if (words.size < 5) return null
            if (words[0].replace(DIACRITICS, "") != "بسم") return null
            return words.take(4).joinToString(" ") to words[4]
        }
    }

    /**
     * Returns name of sura represented by first ayah at page.
     */
    fun getSuraName(): String {
        return ayhas.first().surahIndex.getSuraNameFromIndex()
    }

    /** Name of the sura the page ends with (used for the top bar, like the printed Mushaf). */
    fun getLastSuraName(): String = ayhas.last().surahIndex.getSuraNameFromIndex()
}
