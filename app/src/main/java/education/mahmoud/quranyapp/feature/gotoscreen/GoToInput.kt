package education.mahmoud.quranyapp.feature.gotoscreen

/**
 * Pure validation helpers for the quick-jump dialog, kept free of Android types so
 * they can be unit tested.
 */
object GoToInput {
    const val SURA_COUNT = 114
    const val JUZ_COUNT = 30
    const val PAGE_COUNT = 604

    sealed class Result {
        object Empty : Result()
        data class Valid(val value: Int) : Result()
        data class OutOfRange(val min: Int, val max: Int) : Result()
    }

    /** Keeps only ASCII and Arabic-Indic digits (converted to ASCII), max [maxLength] chars. */
    fun sanitize(raw: String, maxLength: Int = 3): String = raw
        .map { ch ->
            when (ch) {
                in '0'..'9' -> ch
                in '\u0660'..'\u0669' -> '0' + (ch - '\u0660')
                in '\u06F0'..'\u06F9' -> '0' + (ch - '\u06F0')
                else -> null
            }
        }
        .filterNotNull()
        .joinToString("")
        .take(maxLength)

    fun parse(text: String, min: Int, max: Int): Result {
        val trimmed = text.trim()
        val value = trimmed.toIntOrNull()
        return when {
            trimmed.isEmpty() -> Result.Empty
            value != null && value in min..max -> Result.Valid(value)
            else -> Result.OutOfRange(min, max)
        }
    }
}
