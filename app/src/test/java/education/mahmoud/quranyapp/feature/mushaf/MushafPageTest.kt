package education.mahmoud.quranyapp.feature.mushaf

import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.feature.showSuraAyas.Page
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.HizbMark
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.JuzNames
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderInfo
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderStyle
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.isRightPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MushafPageTest {

    private val basmala = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"

    @Test
    fun oddPagesAreRightPages() {
        assertTrue(isRightPage(591))
        assertTrue(!isRightPage(592))
        assertTrue(isRightPage(1))
    }

    @Test
    fun headerStyleFromId_fallsBackToClassic() {
        assertEquals(SuraHeaderStyle.MODERN, SuraHeaderStyle.fromId(1))
        assertEquals(SuraHeaderStyle.PLAIN, SuraHeaderStyle.fromId(2))
        assertEquals(SuraHeaderStyle.CLASSIC, SuraHeaderStyle.fromId(42))
    }

    @Test
    fun suraHeaderInfo_labels() {
        val tariq = SuraHeaderInfo(86, "الطارق", "Meccan", 17)
        assertEquals("سورة الطارق", tariq.displayName)
        assertEquals("مكية", tariq.revelationLabel)
        assertEquals("ترتيبها ٨٦", tariq.orderLabel)
        assertEquals("١٧", tariq.ayahCountArabic)
        assertEquals("مدنية", SuraHeaderInfo(2, "البقرة", "Medinan", 286).revelationLabel)
    }

    @Test
    fun hizbMark_findsFirstNewQuarter() {
        assertNull(HizbMark.find(listOf(236, 236, 236), 236))
        val mark = HizbMark.find(listOf(236, 236, 237, 237), 236)!!
        assertEquals(2, mark.ayahPosition)
        assertEquals("الحزب", mark.title)
        assertEquals("٦٠", mark.numberArabic)
        assertEquals("½ الحزب", HizbMark(0, 3).title)
        // first page of the Quran starts quarter 1
        assertEquals(0, HizbMark.find(listOf(1, 1), 0)!!.ayahPosition)
    }

    @Test
    fun juzNames() {
        assertEquals("الجزء الأول", JuzNames.of(1))
        assertEquals("الجزء الثلاثون", JuzNames.of(30))
    }

    @Test
    fun splitBasmala_whateverDiacriticsOrder() {
        val split = Page.splitBasmala("$basmala وَٱلسَّمَآءِ وَٱلطَّارِقِ")!!
        assertEquals(basmala, split.first)
        assertEquals("وَٱلسَّمَآءِ وَٱلطَّارِقِ", split.second)
        assertNull(Page.splitBasmala("بَرَآءَةٌ مِّنَ ٱللَّهِ وَرَسُولِهِۦٓ إِلَى ٱلَّذِينَ"))
    }

    @Test
    fun buildContent_marksHeadersAndAyahStarts() {
        val page = Page(
            listOf(
                AyahItem(surahIndex = 113, ayahInSurahIndex = 5, text = "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ"),
                AyahItem(surahIndex = 114, ayahInSurahIndex = 1, text = "$basmala قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ"),
                AyahItem(surahIndex = 114, ayahInSurahIndex = 2, text = "مَلِكِ ٱلنَّاسِ"),
            ),
            pageNum = 604,
        )
        val content = page.buildContent()
        assertEquals(1, content.headers.size)
        val header = content.headers[0]
        assertEquals(114, header.surahIndex)
        assertEquals("الناس", content.text.substring(header.start, header.end))
        assertEquals(3, content.ayahStarts.size)
        // ayah 1 of a sura starts at its header line
        assertEquals(header.start, content.ayahStarts[1])
        assertTrue(content.text.contains("الناس\n$basmala\nقُلْ"))
        assertEquals(content.text, page.getText())
    }
}
