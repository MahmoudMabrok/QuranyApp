package education.mahmoud.quranyapp.feature.test_quran

import kotlin.random.Random

/**
 * Pure logic of the memorization (حفظ) test, free of Android types so it is unit tested.
 *
 * The user's answer is compared with the ayah word by word (longest common subsequence),
 * after normalizing both sides: tashkeel, Quranic marks and tatweel removed, and the
 * letter forms people commonly type differently unified (أ إ آ ٱ -> ا, ى -> ي, ة -> ه).
 */
object MemorizationTest {

    enum class WordState { CORRECT, MISSING, EXTRA }

    data class Word(val text: String, val state: WordState)

    data class Grade(
        /** expected words in order, interleaved with extra words the user typed */
        val words: List<Word>,
        val expectedCount: Int,
        val correctCount: Int,
        val missingCount: Int,
        val extraCount: Int,
    ) {
        /** 0..100; extra words count against the score like missing ones */
        val accuracy: Int
            get() {
                val total = expectedCount + extraCount
                return if (total == 0) 0 else (correctCount * 100 + total / 2) / total
            }

        /** points added to the user's total: 2 per correct word, -1 per mistake, never negative */
        val points: Int get() = maxOf(0, correctCount * 2 - missingCount - extraCount)

        val isPerfect: Boolean get() = expectedCount > 0 && correctCount == expectedCount && extraCount == 0
    }

    enum class Level { EXCELLENT, VERY_GOOD, GOOD, NEEDS_REVIEW }

    fun level(accuracy: Int): Level = when {
        accuracy >= 90 -> Level.EXCELLENT
        accuracy >= 75 -> Level.VERY_GOOD
        accuracy >= 50 -> Level.GOOD
        else -> Level.NEEDS_REVIEW
    }

    /** One question: optionally show [promptIndex] (an index into the range), ask for [answerIndex]. */
    data class Question(val promptIndex: Int?, val answerIndex: Int)

    /** Full test: every ayah of the range in order; each one is prompted by the ayah before it. */
    fun fullQuestions(count: Int): List<Question> =
        (0 until count).map { i -> Question(if (i == 0) null else i - 1, i) }

    /** Random test: show a random ayah of the range and ask for the one after it. */
    fun randomQuestion(count: Int, random: Random = Random.Default): Question? {
        if (count < 2) return null
        val prompt = random.nextInt(count - 1)
        return Question(prompt, prompt + 1)
    }

    fun grade(expected: String, answer: String): Grade {
        val exp = words(expected)
        val ans = words(answer)
        val expKeys = exp.map { normalizeWord(it) }
        val ansKeys = ans.map { normalizeWord(it) }

        // LCS table
        val n = exp.size
        val m = ans.size
        val lcs = Array(n + 1) { IntArray(m + 1) }
        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                lcs[i][j] = if (expKeys[i] == ansKeys[j]) lcs[i + 1][j + 1] + 1
                else maxOf(lcs[i + 1][j], lcs[i][j + 1])
            }
        }
        val out = mutableListOf<Word>()
        var i = 0
        var j = 0
        var correct = 0
        var missing = 0
        var extra = 0
        while (i < n || j < m) {
            when {
                i < n && j < m && expKeys[i] == ansKeys[j] -> {
                    out += Word(exp[i], WordState.CORRECT); correct++; i++; j++
                }
                j < m && (i == n || lcs[i][j + 1] >= lcs[i + 1][j]) -> {
                    out += Word(ans[j], WordState.EXTRA); extra++; j++
                }
                else -> {
                    out += Word(exp[i], WordState.MISSING); missing++; i++
                }
            }
        }
        return Grade(out, n, correct, missing, extra)
    }

    fun words(text: String?): List<String> =
        text.orEmpty().trim().split(WHITESPACE).filter { normalizeWord(it).isNotEmpty() }

    fun normalizeWord(word: String): String {
        val sb = StringBuilder(word.length)
        for (c in word) {
            when {
                c in '\u064B'..'\u065F' || c == '\u0670' || c in '\u06D6'..'\u06ED' || c == '\u0640' -> Unit
                c == 'أ' || c == 'إ' || c == 'آ' || c == 'ٱ' -> sb.append('ا')
                c == 'ى' -> sb.append('ي')
                c == 'ة' -> sb.append('ه')
                c.isLetterOrDigit() -> sb.append(c)
                else -> Unit // punctuation
            }
        }
        return sb.toString()
    }

    /** "أول كلمتين…" hint: the first [count] words of the ayah. */
    fun hint(expected: String, count: Int = 2): String {
        val w = words(expected)
        return if (w.size <= count) w.joinToString(" ") else w.take(count).joinToString(" ") + " …"
    }

    private val WHITESPACE = Regex("\\s+")
}
