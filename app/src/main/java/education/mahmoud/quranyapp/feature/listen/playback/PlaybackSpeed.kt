package education.mahmoud.quranyapp.feature.listen.playback

import java.util.Locale
import kotlin.math.abs

/** The playback speeds offered on every listening screen, and how they are stored. */
object PlaybackSpeed {
    const val DEFAULT = 1f

    val OPTIONS: List<Float> = listOf(0.8f, 0.9f, 1f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f)

    /**
     * Snaps any stored or requested value to the closest supported speed, so a corrupt or
     * out-of-range preference can never reach the player. Non-finite or non-positive values
     * fall back to [DEFAULT].
     */
    fun normalize(speed: Float): Float {
        if (!speed.isFinite() || speed <= 0f) return DEFAULT
        return OPTIONS.minByOrNull { abs(it - speed) } ?: DEFAULT
    }

    /** Short label such as "1x", "0.8x" or "1.5x" (always Latin digits). */
    fun label(speed: Float): String {
        val s = normalize(speed)
        val text = if (s % 1f == 0f) s.toInt().toString() else String.format(Locale.US, "%.1f", s).trimEnd('0').trimEnd('.')
        return "${text}x"
    }

    /** The next speed in [OPTIONS], wrapping around after the fastest one. */
    fun next(speed: Float): Float {
        val index = OPTIONS.indexOf(normalize(speed))
        return OPTIONS[(index + 1) % OPTIONS.size]
    }

    fun isDefault(speed: Float): Boolean = normalize(speed) == DEFAULT
}
