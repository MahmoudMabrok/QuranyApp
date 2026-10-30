package education.mahmoud.quranyapp.feature.readlog

import education.mahmoud.quranyapp.feature.read_log.ReadLogFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class ReadLogFormatTest {

    @Test
    fun `pageRanges groups consecutive pages and sorts`() {
        assertEquals("١–٣، ٧، ٩–١٠", ReadLogFormat.pageRanges(listOf(10, 2, 1, 3, 9, 7)))
    }

    @Test
    fun `pageRanges single page and empty`() {
        assertEquals("٦٠٤", ReadLogFormat.pageRanges(setOf(604)))
        assertEquals("", ReadLogFormat.pageRanges(emptyList()))
        assertEquals("", ReadLogFormat.pageRanges(null))
    }

    @Test
    fun `pageRanges ignores duplicates and invalid pages`() {
        assertEquals("١–٢", ReadLogFormat.pageRanges(listOf(0, 1, 1, 2, -5)))
    }

    @Test
    fun `pagesCount follows Arabic number agreement`() {
        assertEquals("صفحة واحدة", ReadLogFormat.pagesCount(1))
        assertEquals("صفحتان", ReadLogFormat.pagesCount(2))
        assertEquals("٣ صفحات", ReadLogFormat.pagesCount(3))
        assertEquals("١٠ صفحات", ReadLogFormat.pagesCount(10))
        assertEquals("١١ صفحة", ReadLogFormat.pagesCount(11))
        assertEquals("١٠٣ صفحات", ReadLogFormat.pagesCount(103))
    }

    @Test
    fun `daysCount follows Arabic number agreement`() {
        assertEquals("يوم واحد", ReadLogFormat.daysCount(1))
        assertEquals("يومان", ReadLogFormat.daysCount(2))
        assertEquals("٤ أيام", ReadLogFormat.daysCount(4))
        assertEquals("٢٠ يومًا", ReadLogFormat.daysCount(20))
    }

    @Test
    fun `fullDate uses Arabic day, month and digits`() {
        val c = Calendar.getInstance().apply { clear(); set(2026, Calendar.OCTOBER, 30, 10, 0) }
        assertEquals("الجمعة ٣٠ أكتوبر ٢٠٢٦", ReadLogFormat.fullDate(c.timeInMillis))
    }

    @Test
    fun `relativeDay today, yesterday, across new year and older`() {
        fun at(y: Int, m: Int, d: Int, h: Int = 12) =
            Calendar.getInstance().apply { clear(); set(y, m, d, h, 0) }.timeInMillis
        val now = at(2026, Calendar.OCTOBER, 30, 9)
        assertEquals("اليوم", ReadLogFormat.relativeDay(at(2026, Calendar.OCTOBER, 30, 1), now))
        assertEquals("أمس", ReadLogFormat.relativeDay(at(2026, Calendar.OCTOBER, 29, 23), now))
        assertNull(ReadLogFormat.relativeDay(at(2026, Calendar.OCTOBER, 28), now))
        assertEquals("أمس", ReadLogFormat.relativeDay(at(2025, Calendar.DECEMBER, 31), at(2026, Calendar.JANUARY, 1)))
    }

    @Test
    fun `parseStrDate reads d-M-yyyy and rejects junk`() {
        val expected = Calendar.getInstance().apply { clear(); set(2026, Calendar.OCTOBER, 30) }.timeInMillis
        assertEquals(expected, ReadLogFormat.parseStrDate("30-10-2026"))
        assertNull(ReadLogFormat.parseStrDate("2026/10/30"))
        assertNull(ReadLogFormat.parseStrDate("31-13-2026"))
        assertNull(ReadLogFormat.parseStrDate(null))
    }

    @Test
    fun `dayMillis prefers strDate over the stored millis`() {
        val oct30 = ReadLogFormat.parseStrDate("30-10-2026")
        val nov30 = Calendar.getInstance().apply { clear(); set(2026, Calendar.NOVEMBER, 30) }.timeInMillis
        assertEquals(oct30, ReadLogFormat.dayMillis("30-10-2026", nov30))
        assertEquals(nov30, ReadLogFormat.dayMillis("", nov30))
        assertNull(ReadLogFormat.dayMillis(null, 0))
    }
}
