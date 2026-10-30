package education.mahmoud.quranyapp.feature.ayahs_search

import education.mahmoud.quranyapp.utils.RemoveTashkeel

/**
 * Pure helpers for the ayah search screen (kept free of Android types so they are unit tested).
 */
object SearchText {

    /**
     * Turns what the user typed into the text matched against `textClean`:
     * tashkeel removed (textClean has none), runs of whitespace collapsed, ends trimmed.
     */
    fun normalizeQuery(raw: CharSequence?): String {
        if (raw == null) return ""
        return RemoveTashkeel.removeTashkeel(raw.toString())
            .replace(WHITESPACE, " ")
            .trim()
    }

    /**
     * Every occurrence of [word] in [text] as [start, end) index pairs. Plain text matching:
     * the query is never compiled as a regex, so input such as "(" or "*" can't crash.
     */
    fun matchRanges(text: String, word: String): List<IntRange> {
        if (word.isEmpty() || text.isEmpty()) return emptyList()
        val ranges = mutableListOf<IntRange>()
        var from = 0
        while (from <= text.length - word.length) {
            val index = text.indexOf(word, from)
            if (index < 0) break
            ranges += index until index + word.length
            from = index + word.length
        }
        return ranges
    }

    private val WHITESPACE = Regex("\\s+")
}
