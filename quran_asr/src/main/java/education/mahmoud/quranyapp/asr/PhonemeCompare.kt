package education.mahmoud.quranyapp.asr

import kotlin.math.min

/**
 * The recognizer writes tajweed, not plain spelling: shadda, madd, qalqalah,
 * ghunnah, iqlab and saktah are separate symbols. This compares those rulings
 * with the expected phonemes of each word.
 */
object PhonemeCompare {

    enum class Status { OK, MISSING, SHORT, WAIT }

    data class Line(
        val arabic: String,
        val rule: String,
        val status: Status,
        val newAyah: Boolean,
        val matched: Boolean,
    )

    private data class Rule(val name: String, val amount: Int)

    data class Grade(val lines: List<Line>, val reachedWords: Int)

    fun grade(heard: String, words: List<AyahReference.Word>, finished: Boolean): Grade {
        val audio = normalize(heard)
        var pos = 0
        var reachedWords = 0
        val lines = ArrayList<Line>()
        var stalled = false
        for ((index, word) in words.withIndex()) {
            if (stalled && !finished) break
            val target = normalize(word.phonemes)
            val (cost, used) = alignPrefix(audio, pos, target)
            val reached = !stalled && target.isNotEmpty() && used > 0 && cost * 3 <= target.length
            if (reached) {
                pos += used
                reachedWords = index + 1
            } else {
                stalled = true
            }
            val expected = extract(word.phonemes, word.arabic)
            if (expected.isEmpty() || (stalled && !finished)) continue
            val heardRules = if (reached) {
                extract(audio.substring(pos - used, pos), word.arabic)
            } else {
                emptyList()
            }
            val remaining = heardRules.toMutableList()
            var first = true
            for (rule in expected) {
                val status = when {
                    !reached && !finished -> Status.WAIT
                    !reached -> Status.MISSING
                    else -> takeStatus(rule, remaining)
                }
                lines.add(
                    Line(
                        arabic = word.arabic,
                        rule = rule.name,
                        status = status,
                        newAyah = first && word.newAyah && lines.isNotEmpty(),
                        matched = reached,
                    ),
                )
                first = false
            }
        }
        return Grade(lines, reachedWords)
    }

    private fun takeStatus(rule: Rule, heard: MutableList<Rule>): Status {
        val index = heard.indexOfFirst { it.name == rule.name }
        if (index < 0) return Status.MISSING
        val match = heard.removeAt(index)
        if (rule.name == "مد" && match.amount + 1 < rule.amount && match.amount * 2 < rule.amount) {
            return Status.SHORT
        }
        return Status.OK
    }

    private fun extract(phonemes: String, arabic: String): List<Rule> {
        val text = normalize(phonemes)
        val rules = ArrayList<Rule>()
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            var j = i + 1
            while (j < text.length && text[j] == ch) j++
            val run = j - i
            when {
                ch == QALQALAH -> rules.add(Rule("قلقلة", 1))
                ch == SAKTAH -> rules.add(Rule("سكتة", 1))
                ch == IQLAB_MARK -> rules.add(Rule(iqlabName(arabic, text), 1))
                ch == GHUNNAH_NOON && run >= 2 -> rules.add(Rule("غنة", run))
                ch in MADD && run >= 2 -> rules.add(Rule("مد", run))
                ch !in MADD && !ch.isHaraka() && run >= 2 -> {
                    if ((ch == 'م' || ch == 'ن') && run >= 3) rules.add(Rule("غنة", run))
                    else rules.add(Rule("شدة", run))
                }
            }
            i = j
        }
        val merged = LinkedHashMap<String, Int>()
        for (rule in rules) {
            val previous = merged[rule.name]
            if (previous == null || rule.amount > previous) merged[rule.name] = rule.amount
        }
        return merged.map { Rule(it.key, it.value) }
    }

    private fun iqlabName(arabic: String, phonemes: String): String {
        val noon = 'ن' in phonemes || 'ں' in phonemes || arabic.any { it == 'ً' || it == 'ٌ' || it == 'ٍ' || it == 'ۢ' }
        if (!noon && arabic.trimHaraka().endsWith("م")) return "إخفاء شفوي"
        return "إقلاب"
    }

    private fun String.trimHaraka(): String = filterNot { it.isHaraka() || it == 'ٰ' || it == 'ـ' }

    private fun Char.isHaraka(): Boolean = this == 'َ' || this == 'ُ' || this == 'ِ' || this == 'ْ'

    private fun normalize(text: String): String {
        val out = StringBuilder(text.length)
        for (ch in text) {
            if (ch == '\u0640' || ch == '\u200f' || ch == '\u200e' || ch.isWhitespace()) continue
            out.append(ch)
        }
        return out.toString()
    }

    /** Min edit cost to consume [expected], and how many heard characters that used. */
    private fun alignPrefix(heard: String, start: Int, expected: String): Pair<Int, Int> {
        if (expected.isEmpty()) return 0 to 0
        val remain = heard.length - start
        if (remain <= 0) return expected.length to 0
        val columns = min(remain, expected.length + 8)
        val rows = expected.length
        var prev = IntArray(columns + 1) { it }
        var next = IntArray(columns + 1)
        for (i in 1..rows) {
            next[0] = i
            val exp = expected[i - 1]
            for (j in 1..columns) {
                val sub = if (exp == heard[start + j - 1]) 0 else 1
                next[j] = min(prev[j - 1] + sub, min(prev[j] + 1, next[j - 1] + 1))
            }
            val swap = prev
            prev = next
            next = swap
        }
        var bestCost = prev[0]
        var bestUsed = 0
        for (j in 1..columns) {
            if (prev[j] < bestCost) {
                bestCost = prev[j]
                bestUsed = j
            }
        }
        return bestCost to bestUsed
    }

    private val MADD = charArrayOf('ا', 'و', 'ي', 'ۦ', 'ۥ').toSet()
    private const val QALQALAH = 'ڇ'
    private const val SAKTAH = 'ۜ'
    private const val IQLAB_MARK = '۾'
    private const val GHUNNAH_NOON = 'ں'
}
