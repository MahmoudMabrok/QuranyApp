package education.mahmoud.quranyapp.feature.scores

import education.mahmoud.quranyapp.utils.NumberHelper

/**
 * Levels for the saved memorization-test points ([education.mahmoud.quranyapp.datalayer.QuranRepository.score]).
 * A perfect 4-word ayah is worth 8 points, so the first levels come quickly and the last ones
 * need regular practice.
 */
enum class ScoreLevel(val minPoints: Long) {
    NONE(0),
    BEGINNER(1),
    DILIGENT(50),
    ADVANCED(150),
    PROFICIENT(400),
    DISTINGUISHED(1000),
}

/**
 * Where [score] sits: its [level], the [next] level (null at the top), the points still
 * [remaining] to reach it and the [percent] (0..100) done inside the current level.
 */
data class ScoreProgress(
    val score: Long,
    val level: ScoreLevel,
    val next: ScoreLevel?,
    val remaining: Long,
    val percent: Int,
)

object ScoreLevels {

    /** Counted noun after a number: ٣–١٠ take the plural "نقاط", everything else "نقطة". */
    @JvmStatic
    fun pointsUnit(points: Long): String = if (points.mod(100L) in 3L..10L) "نقاط" else "نقطة"

    /** "١٢ نقطة", "٥ نقاط", with the dual and singular forms for 2 and 1. */
    @JvmStatic
    fun pointsLabel(points: Long): String = when (points) {
        1L -> "نقطة واحدة"
        2L -> "نقطتان"
        else -> "${NumberHelper.getArabicNumber(points)} ${pointsUnit(points)}"
    }

    /** Ranked levels a user can earn (NONE is the empty state, not a level). */
    @JvmStatic
    val ranked: List<ScoreLevel> = ScoreLevel.values().filter { it != ScoreLevel.NONE }

    @JvmStatic
    fun levelFor(score: Long): ScoreLevel {
        val s = score.coerceAtLeast(0)
        return ScoreLevel.values().last { s >= it.minPoints }
    }

    @JvmStatic
    fun progress(score: Long): ScoreProgress {
        val s = score.coerceAtLeast(0)
        val level = levelFor(s)
        val next = ScoreLevel.values().getOrNull(level.ordinal + 1)
            ?: return ScoreProgress(s, level, null, 0, 100)
        val span = next.minPoints - level.minPoints
        val done = s - level.minPoints
        val percent = (done * 100 / span).toInt().coerceIn(0, 99)
        return ScoreProgress(s, level, next, next.minPoints - s, percent)
    }
}
