package education.mahmoud.quranyapp.feature.mushaf

import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.HizbMark
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.JuzNames
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderInfo
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderStyle
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.isRightPage
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafLook
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafPageTurn
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafReaderSettings
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafSpreadMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MushafPageTest {

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
    fun readerSettings_unknownIdsKeepThePreviousReader() {
        assertEquals(MushafLook.ORNATE, MushafLook.fromId(99))
        assertEquals(MushafPageTurn.SLIDE, MushafPageTurn.fromId(-1))
        assertEquals(MushafSpreadMode.SINGLE, MushafSpreadMode.fromId(7))
        val stored = MushafReaderSettings(
            headerStyle = SuraHeaderStyle.MODERN,
            pageFrame = false,
        )
        assertEquals(SuraHeaderStyle.MODERN, stored.headerStyle)
        assertTrue(!stored.pageFrame)
        assertTrue(!stored.drawDivisionMarks)
        assertTrue(!stored.libraryDrawsPageNumber)
        assertTrue(!stored.colorNaturalMadd)
        assertTrue(stored.ornateControlsApply)
    }

    @Test
    fun readerSettings_divisionMarksNeedTheOrnateFrame() {
        val marksOn = MushafReaderSettings(divisionMarks = true, pageFrame = true)
        assertTrue(marksOn.drawDivisionMarks)
        assertTrue(marksOn.libraryDrawsPageNumber)
        val frameOff = marksOn.copy(pageFrame = false)
        assertTrue(!frameOff.drawDivisionMarks)
        val cairo = marksOn.copy(look = MushafLook.CAIRO)
        assertTrue(!cairo.drawDivisionMarks)
        assertTrue(!cairo.ornateControlsApply)
        assertTrue(cairo.libraryDrawsPageNumber)
    }

    @Test
    fun readerSettings_naturalMaddFollowsTajweed() {
        val madd = MushafReaderSettings(naturalMadd = true)
        assertTrue(!madd.colorNaturalMadd)
        assertTrue(madd.copy(tajweed = true).colorNaturalMadd)
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
}
