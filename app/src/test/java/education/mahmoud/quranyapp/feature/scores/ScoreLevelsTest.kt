package education.mahmoud.quranyapp.feature.scores

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import education.mahmoud.quranyapp.utils.NumberHelper
import org.junit.Test

class ScoreLevelsTest {

    @Test
    fun zeroAndNegativeScoresHaveNoLevel() {
        assertEquals(ScoreLevel.NONE, ScoreLevels.levelFor(0))
        assertEquals(ScoreLevel.NONE, ScoreLevels.levelFor(-5))
        val p = ScoreLevels.progress(-5)
        assertEquals(0L, p.score)
        assertEquals(ScoreLevel.BEGINNER, p.next)
        assertEquals(1L, p.remaining)
        assertEquals(0, p.percent)
    }

    @Test
    fun levelBoundaries() {
        assertEquals(ScoreLevel.BEGINNER, ScoreLevels.levelFor(1))
        assertEquals(ScoreLevel.BEGINNER, ScoreLevels.levelFor(49))
        assertEquals(ScoreLevel.DILIGENT, ScoreLevels.levelFor(50))
        assertEquals(ScoreLevel.DILIGENT, ScoreLevels.levelFor(149))
        assertEquals(ScoreLevel.ADVANCED, ScoreLevels.levelFor(150))
        assertEquals(ScoreLevel.PROFICIENT, ScoreLevels.levelFor(400))
        assertEquals(ScoreLevel.DISTINGUISHED, ScoreLevels.levelFor(1000))
        assertEquals(ScoreLevel.DISTINGUISHED, ScoreLevels.levelFor(Long.MAX_VALUE))
    }

    @Test
    fun progressInsideLevel() {
        // 12 points (what one partly wrong + one perfect Al-Fatiha ayah gives): beginner, 38 to go
        val p = ScoreLevels.progress(12)
        assertEquals(ScoreLevel.BEGINNER, p.level)
        assertEquals(ScoreLevel.DILIGENT, p.next)
        assertEquals(38L, p.remaining)
        assertEquals(22, p.percent) // 11 of 49

        val q = ScoreLevels.progress(275)
        assertEquals(ScoreLevel.ADVANCED, q.level)
        assertEquals(125L, q.remaining)
        assertEquals(50, q.percent)
    }

    @Test
    fun progressNeverShowsFullBeforeNextLevel() {
        assertEquals(95, ScoreLevels.progress(970).percent)
        assertEquals(99, ScoreLevels.progress(999).percent)
        assertEquals(0, ScoreLevels.progress(1).percent)
    }

    @Test
    fun topLevelIsComplete() {
        val p = ScoreLevels.progress(5000)
        assertEquals(ScoreLevel.DISTINGUISHED, p.level)
        assertNull(p.next)
        assertEquals(0L, p.remaining)
        assertEquals(100, p.percent)
    }

    @Test
    fun rankedLevelsExcludeNone() {
        assertEquals(5, ScoreLevels.ranked.size)
        assertEquals(ScoreLevel.BEGINNER, ScoreLevels.ranked.first())
    }

    @Test
    fun longPointsUseArabicDigits() {
        assertEquals("١٢", NumberHelper.getArabicNumber(12L))
        assertEquals("٣٠٠٠٠٠٠٠٠٠", NumberHelper.getArabicNumber(3_000_000_000L))
    }

    @Test
    fun pointsLabelUsesArabicCountedNouns() {
        assertEquals("٠ نقطة", ScoreLevels.pointsLabel(0))
        assertEquals("نقطة واحدة", ScoreLevels.pointsLabel(1))
        assertEquals("نقطتان", ScoreLevels.pointsLabel(2))
        assertEquals("٣ نقاط", ScoreLevels.pointsLabel(3))
        assertEquals("١٠ نقاط", ScoreLevels.pointsLabel(10))
        assertEquals("١١ نقطة", ScoreLevels.pointsLabel(11))
        assertEquals("١٠٥ نقاط", ScoreLevels.pointsLabel(105))
        assertEquals("١٢٥٠ نقطة", ScoreLevels.pointsLabel(1250))
    }
}
