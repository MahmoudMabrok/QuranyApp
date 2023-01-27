package education.mahmoud.quranyapp.feature.showSuraAyas

import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.utils.NumberHelper
import education.mahmoud.quranyapp.utils.getSuraNameFromIndex
import java.text.MessageFormat

data class Page(
    var ayhas: List<AyahItem>,
    var pageNum: Int = 0,
    var rubHizb: Int = 0,
    var juz: Int = 0
) {

    fun getText(): String {
        val builder = StringBuilder()
        var aya: String
        var tempSuraName: String
        var isFirst = true
        ayhas.let { ayahs ->
            for (ayahItem in ayahs) {
                aya = ayahItem.text
                // add sura name
                if (ayahItem.ayahInSurahIndex == 1) {
                    tempSuraName = (ayahItem.surahIndex).getSuraNameFromIndex()
                    if (isFirst) {
                        // handle first name in page that not need a previous new line
                        builder.append(tempSuraName + "\n")
                    } else {
                        builder.append("\n" + tempSuraName + "\n")
                    }
                    // AlFatiha(index = 1 ) has a Basmallah in first ayah.
                    if (ayahItem.surahIndex != 1) {
                        var pos = aya.indexOf("ٱلرَّحِيم")
                        pos += "ٱلرَّحِيم".length
                        // insert  البسملة
                        builder.append(aya.substring(0, pos + 1)) // +1 as substring upper bound is excluded
                        builder.append("\n")
                        // cute ayah
                        aya = aya.substring(pos + 1) // +1 to start with new character after البسملة
                    }
                }
                isFirst = false
                builder.append(MessageFormat.format("{0}   ﴿{1}﴾  ", aya, NumberHelper.getArabicNumber(ayahItem.ayahInSurahIndex)))
            }
        }

        return builder.toString()
    }

    /**
     * Returns name of sura represented by first ayah at page.
     */
    fun getSuraName(): String {
       return ayhas.first().surahIndex.getSuraNameFromIndex()
    }
}
