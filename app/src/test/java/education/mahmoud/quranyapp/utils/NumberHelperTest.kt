package education.mahmoud.quranyapp.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberHelperTest {

    @Test
    fun getArabicNumber_returnsArabicIndicDigits() {
        assertEquals("١", NumberHelper.getArabicNumber(1))
        assertEquals("١٢", NumberHelper.getArabicNumber(12))
        assertEquals("٢٨٦", NumberHelper.getArabicNumber(286))
    }

    @Test
    fun formatAyahWithNumber_keepsMarkerClosedAndAttached() {
        assertEquals("الم\u00A0﴿١﴾ ", NumberHelper.formatAyahWithNumber("الم ", 1))
    }
}
