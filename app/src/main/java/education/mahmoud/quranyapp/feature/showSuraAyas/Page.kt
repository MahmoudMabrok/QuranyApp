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

    /** Page text plus the ranges of the sura-name lines and the start offset of every ayah. */
    class Content(val text: String, val headers: List<HeaderRange>, val ayahStarts: List<Int>)

    fun getText(): String = buildContent().text

    fun buildContent(): Content {
        val builder = StringBuilder()
        val headers = mutableListOf<HeaderRange>()
        val ayahStarts = mutableListOf<Int>()
        var aya: String
        var isFirst = true
        for (ayahItem in ayhas) {
            aya = ayahItem.text
            ayahStarts.add(builder.length)
            // add sura name
            if (ayahItem.ayahInSurahIndex == 1) {
                val tempSuraName = ayahItem.surahIndex.getSuraNameFromIndex()
                // handle first name in page that not need a previous new line
                if (!isFirst) builder.append("\n")
                val start = builder.length
                builder.append(tempSuraName)
                headers.add(HeaderRange(ayahItem.surahIndex, start, builder.length))
                builder.append("\n")
                // AlFatiha(index = 1 ) has a Basmallah in first ayah.
                if (ayahItem.surahIndex != 1) {
                    var pos = aya.indexOf("ٱلرَّحِيم")
                    if (pos >= 0) {
                        pos += "ٱلرَّحِيم".length
                        // insert  البسملة
                        builder.append(aya.substring(0, minOf(pos + 1, aya.length))) // +1 as substring upper bound is excluded
                        builder.append("\n")
                        // cut ayah
                        aya = if (pos + 1 < aya.length) aya.substring(pos + 1) else "" // start after البسملة
                    }
                }
            }
            isFirst = false
            builder.append(NumberHelper.formatAyahWithNumber(aya, ayahItem.ayahInSurahIndex))
        }
        return Content(builder.toString(), headers, ayahStarts)
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
