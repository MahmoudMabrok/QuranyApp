package education.mahmoud.quranyapp.feature.gotoscreen

import org.junit.Assert.assertEquals
import org.junit.Test

class GoToInputTest {

    @Test
    fun `sanitize keeps digits and converts arabic indic digits`() {
        assertEquals("12", GoToInput.sanitize("1a2"))
        assertEquals("255", GoToInput.sanitize("\u0662\u0665\u0665"))
        assertEquals("604", GoToInput.sanitize("6045"))
        assertEquals("", GoToInput.sanitize("-."))
    }

    @Test
    fun `parse validates range`() {
        assertEquals(GoToInput.Result.Empty, GoToInput.parse("", 1, 114))
        assertEquals(GoToInput.Result.Valid(114), GoToInput.parse("114", 1, 114))
        assertEquals(GoToInput.Result.OutOfRange(1, 114), GoToInput.parse("0", 1, 114))
        assertEquals(GoToInput.Result.OutOfRange(1, 30), GoToInput.parse("31", 1, 30))
    }
}
