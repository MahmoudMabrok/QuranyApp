package education.mahmoud.quranyapp.feature.listen

import education.mahmoud.quranyapp.feature.listen.playback.PlaybackSpeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackSpeedTest {

    @Test
    fun `options are sorted, unique and include normal speed`() {
        assertEquals(listOf(0.8f, 0.9f, 1f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f), PlaybackSpeed.OPTIONS)
        assertTrue(PlaybackSpeed.DEFAULT in PlaybackSpeed.OPTIONS)
    }

    @Test
    fun `normalize keeps supported values`() {
        PlaybackSpeed.OPTIONS.forEach { assertEquals(it, PlaybackSpeed.normalize(it)) }
    }

    @Test
    fun `normalize snaps unsupported values to closest option`() {
        assertEquals(1.3f, PlaybackSpeed.normalize(1.27f))
        assertEquals(0.8f, PlaybackSpeed.normalize(0.1f))
        assertEquals(1.5f, PlaybackSpeed.normalize(5f))
    }

    @Test
    fun `normalize falls back to default for invalid values`() {
        assertEquals(1f, PlaybackSpeed.normalize(0f))
        assertEquals(1f, PlaybackSpeed.normalize(-1f))
        assertEquals(1f, PlaybackSpeed.normalize(Float.NaN))
        assertEquals(1f, PlaybackSpeed.normalize(Float.POSITIVE_INFINITY))
    }

    @Test
    fun `label formats speeds`() {
        assertEquals("0.8x", PlaybackSpeed.label(0.8f))
        assertEquals("0.9x", PlaybackSpeed.label(0.9f))
        assertEquals("1x", PlaybackSpeed.label(1f))
        assertEquals("1.1x", PlaybackSpeed.label(1.1f))
        assertEquals("1.2x", PlaybackSpeed.label(1.2f))
        assertEquals("1.3x", PlaybackSpeed.label(1.3f))
        assertEquals("1.4x", PlaybackSpeed.label(1.4f))
        assertEquals("1.5x", PlaybackSpeed.label(1.5f))
    }

    @Test
    fun `next cycles through options and wraps`() {
        assertEquals(1.1f, PlaybackSpeed.next(1f))
        assertEquals(0.8f, PlaybackSpeed.next(1.5f))
    }

    @Test
    fun `isDefault only for normal speed`() {
        assertTrue(PlaybackSpeed.isDefault(1f))
        assertFalse(PlaybackSpeed.isDefault(1.5f))
    }
}
