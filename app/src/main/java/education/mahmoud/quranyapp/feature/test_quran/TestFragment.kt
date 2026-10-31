package education.mahmoud.quranyapp.feature.test_quran

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.BaseFragment
import education.mahmoud.quranyapp.databinding.FragmentTestBinding
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.datalayer.local.room.SuraItem
import education.mahmoud.quranyapp.feature.scores.ScoreActivity
import education.mahmoud.quranyapp.feature.scores.ScoreLevels
import education.mahmoud.quranyapp.feature.test_quran.MemorizationTest.Level
import education.mahmoud.quranyapp.feature.test_quran.MemorizationTest.WordState
import education.mahmoud.quranyapp.utils.Data
import education.mahmoud.quranyapp.utils.EdgeToEdge
import education.mahmoud.quranyapp.utils.KeyboardHelper
import education.mahmoud.quranyapp.utils.NumberHelper.Companion.getArabicNumber
import education.mahmoud.quranyapp.utils.UiFont
import org.koin.android.ext.android.inject

/**
 * Memorization (حفظ) test: pick a range, then write the ayahs from memory one at a time and
 * get word-by-word feedback, then a result summary. Points are added to the saved score.
 */
class TestFragment : BaseFragment() {

    private val quranRepository: QuranRepository by inject()
    private val binding by viewBinding(FragmentTestBinding::bind)

    private var suras: List<SuraItem> = emptyList()

    // current test
    private var rangeAyahs: List<AyahItem> = emptyList()
    private var questions: List<MemorizationTest.Question> = emptyList()
    private var current = 0
    private var isRandom = false
    private val grades = mutableListOf<MemorizationTest.Grade>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_test, container, false)

    override fun initViews(view: View) {
        super.initViews(view)
        with(binding) {
            // edge-to-edge: teal bar behind the status bar, content above the nav bar / keyboard
            EdgeToEdge.applyPadding(testToolbar, bottom = false)
            EdgeToEdge.applyPadding(
                testScroll, top = false,
                types = EdgeToEdge.SAFE_TYPES or WindowInsetsCompat.Type.ime(),
            )
            applyUiFont(root)
            UiFont.applyBold(testTitle, tvFeedbackTitle, tvResultLevel, tvResultPercent, tvQuestion)
            testBack.setOnClickListener { activity?.onBackPressedDispatcher?.onBackPressed() }
            // points pill -> the points screen (same saved total)
            testTotalPoints.setOnClickListener { startActivity(Intent(requireContext(), ScoreActivity::class.java)) }
        }
        activity?.let { EdgeToEdge.setStatusBarIconsDark(it, false) }
        updateTotalPoints()
        initSetup()
    }

    /** system font for every label; the theme font (me_quran) is kept only for nothing here */
    private fun applyUiFont(v: View) {
        val ayahViews = with(binding) { setOf(tvAyahToTestAfter, tvFeedbackDiff, tvCorrectAyah, edUserTextForAyahs) }
        when (v) {
            is TextView -> if (v !in ayahViews) UiFont.apply(v)
            is ViewGroup -> for (i in 0 until v.childCount) applyUiFont(v.getChildAt(i))
        }
    }

    // region setup
    private fun initSetup() {
        suras = (1..114).mapNotNull { runCatching { quranRepository.getSuraByIndex(it.toLong()) }.getOrNull() }
        if (suras.isEmpty() || quranRepository.totlaAyahs == 0) {
            show(State.EMPTY)
            return
        }
        val names = suras.map { s -> "${getArabicNumber(s.index)}. ${suraName(s)}" }
        with(binding) {
            spStartSura.adapter = suraAdapter(names)
            spEndSura.adapter = suraAdapter(names)
            spStartSura.onItemSelectedListener = selected {
                if (edStartSuraAyah.text.isNullOrEmpty()) edStartSuraAyah.setText("1")
                // keep the end at or after the start
                if (spEndSura.selectedItemPosition < it) spEndSura.setSelection(it)
                updateRangeSummary()
            }
            spEndSura.onItemSelectedListener = selected {
                edEndSuraAyah.setText(suras[it].numOfAyahs.toString())
                updateRangeSummary()
            }
            edStartSuraAyah.setText("1")
            edStartSuraAyah.doAfterTextChanged { updateRangeSummary() }
            edEndSuraAyah.doAfterTextChanged { updateRangeSummary() }
            btnTestSave.setOnClickListener { start(random = false) }
            btnTestSaveRandom.setOnClickListener { start(random = true) }
            btnCheckTest.setOnClickListener { check() }
            btnHint.setOnClickListener { showHint() }
            btnSkip.setOnClickListener { check(skipped = true) }
            btnNext.setOnClickListener { next() }
            btnRetry.setOnClickListener { start(isRandom) }
            btnNewTest.setOnClickListener { show(State.SETUP) }
        }
        show(State.SETUP)
    }

    private fun suraAdapter(names: List<String>) = object : ArrayAdapter<String>(requireContext(), R.layout.item_sura_spinner, names) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            super.getView(position, convertView, parent).also { UiFont.apply(it as TextView) }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
            super.getDropDownView(position, convertView, parent).also { UiFont.apply(it as TextView) }
    }

    private fun selected(block: (Int) -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = block(position)
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }

    private data class Range(val startSura: SuraItem, val startAyah: Int, val endSura: SuraItem, val endAyah: Int)

    /** validates the inputs; returns the range or an error message */
    private fun readRange(): Pair<Range?, String?> = with(binding) {
        val s = suras.getOrNull(spStartSura.selectedItemPosition) ?: return null to getString(R.string.sura_select_error)
        val e = suras.getOrNull(spEndSura.selectedItemPosition) ?: return null to getString(R.string.sura_select_error)
        val a = edStartSuraAyah.text.toString().toIntOrNull()
        val b = edEndSuraAyah.text.toString().toIntOrNull()
        if (a == null || a < 1 || b == null || b < 1) return null to getString(R.string.test_err_ayah_number)
        if (a > s.numOfAyahs) return null to getString(R.string.test_err_out_of_range, suraName(s), getArabicNumber(s.numOfAyahs))
        if (b > e.numOfAyahs) return null to getString(R.string.test_err_out_of_range, suraName(e), getArabicNumber(e.numOfAyahs))
        if (e.index < s.index || (e.index == s.index && b < a)) return null to getString(R.string.test_err_order)
        Range(s, a, e, b) to null
    }

    /** global ayah indexes (1..6236) of the range ends */
    private fun globalBounds(r: Range): Pair<Int, Int>? = runCatching {
        quranRepository.getAyahByInSurahIndex(r.startSura.index, r.startAyah).ayahIndex to
            quranRepository.getAyahByInSurahIndex(r.endSura.index, r.endAyah).ayahIndex
    }.getOrNull()

    private fun ayahCount(r: Range): Int = globalBounds(r)?.let { it.second - it.first + 1 } ?: 0

    private fun updateRangeSummary() {
        val (range, error) = readRange()
        with(binding) {
            tvSetupError.visibility = if (error != null) View.VISIBLE else View.GONE
            tvSetupError.text = error
            tvSetupRange.visibility = if (range != null) View.VISIBLE else View.GONE
            range?.let {
                tvSetupRange.text = getString(
                    R.string.test_range_summary, ayahsCount(ayahCount(it)),
                    suraName(it.startSura), getArabicNumber(it.startAyah),
                    suraName(it.endSura), getArabicNumber(it.endAyah),
                )
            }
        }
    }

    private fun start(random: Boolean) {
        val (range, error) = readRange()
        if (range == null) {
            binding.tvSetupError.visibility = View.VISIBLE
            binding.tvSetupError.text = error
            show(State.SETUP)
            return
        }
        val (first, last) = globalBounds(range) ?: run {
            showSetupError(getString(R.string.test_no_data))
            return
        }
        val count = last - first + 1
        if (!random && count > MAX_FULL_AYAHS) {
            showSetupError(getString(R.string.test_err_too_long, ayahsCount(count), getArabicNumber(MAX_FULL_AYAHS)))
            return
        }
        if (random && count < 2) {
            showSetupError(getString(R.string.test_err_random_short))
            return
        }
        rangeAyahs = quranRepository.getAyahSInRange(first, last)
        isRandom = random
        questions = if (random) listOfNotNull(MemorizationTest.randomQuestion(rangeAyahs.size))
        else MemorizationTest.fullQuestions(rangeAyahs.size)
        if (questions.isEmpty()) {
            showSetupError(getString(R.string.test_err_random_short))
            return
        }
        val rangeText = getString(
            R.string.test_range_short, suraName(range.startSura), getArabicNumber(range.startAyah),
            suraName(range.endSura), getArabicNumber(range.endAyah),
        )
        binding.tvTestRange.text = rangeText
        binding.tvResultRange.text = rangeText
        grades.clear()
        current = 0
        KeyboardHelper.hideKeyboard(binding.root)
        show(State.QUESTION)
        bindQuestion()
    }

    private fun showSetupError(message: String) {
        binding.tvSetupError.visibility = View.VISIBLE
        binding.tvSetupError.text = message
    }
    // endregion

    // region question
    private fun bindQuestion() = with(binding) {
        val q = questions[current]
        val answer = rangeAyahs[q.answerIndex]
        tvQuestionCounter.text = getString(R.string.test_question_counter, getArabicNumber(current + 1), getArabicNumber(questions.size))
        testProgress.max = questions.size
        testProgress.setProgressCompat(current, true)

        val prompt = q.promptIndex?.let { rangeAyahs[it] }
        promptCard.visibility = if (prompt != null) View.VISIBLE else View.GONE
        prompt?.let {
            tvPromptLabel.text = getString(
                if (isRandom) R.string.test_prompt_random else R.string.test_prompt_previous,
                suraNameOf(it), getArabicNumber(it.ayahInSurahIndex),
            )
            tvAyahToTestAfter.text = it.text
        }
        tvQuestion.text = if (prompt == null) {
            getString(R.string.test_question_first, suraNameOf(answer), getArabicNumber(answer.ayahInSurahIndex))
        } else {
            getString(R.string.test_question_next, suraNameOf(answer), getArabicNumber(answer.ayahInSurahIndex))
        }
        edUserTextForAyahs.setText("")
        edUserTextForAyahs.isEnabled = true
        tvHint.visibility = View.GONE
        answerButtons.visibility = View.VISIBLE
        feedbackCard.visibility = View.GONE
        btnNext.visibility = View.GONE
        testScroll.scrollTo(0, 0)
        edUserTextForAyahs.requestFocus()
    }

    private fun showHint() = with(binding) {
        val answer = rangeAyahs[questions[current].answerIndex]
        tvHint.text = getString(R.string.test_hint_text, MemorizationTest.hint(answer.textClean.ifEmpty { answer.text }))
        tvHint.visibility = View.VISIBLE
    }

    private fun check(skipped: Boolean = false) = with(binding) {
        val answer = rangeAyahs[questions[current].answerIndex]
        val typed = if (skipped) "" else edUserTextForAyahs.text.toString()
        val grade = MemorizationTest.grade(answer.textClean.ifEmpty { answer.text }, typed)
        grades += grade
        if (grade.points > 0) quranRepository.score = quranRepository.score + grade.points
        updateTotalPoints()

        KeyboardHelper.hideKeyboard(edUserTextForAyahs)
        edUserTextForAyahs.isEnabled = false
        edUserTextForAyahs.clearFocus()
        answerButtons.visibility = View.GONE
        bindFeedback(grade, answer)
        btnNext.visibility = View.VISIBLE
        btnNext.setText(if (current == questions.lastIndex) R.string.test_show_result else R.string.test_next)
        testScroll.post { testScroll.smoothScrollTo(0, feedbackCard.top) }
    }

    private fun bindFeedback(grade: MemorizationTest.Grade, answer: AyahItem) = with(binding) {
        val level = MemorizationTest.level(grade.accuracy)
        val color = color(levelColor(level))
        feedbackCard.visibility = View.VISIBLE
        feedbackCard.strokeColor = color
        tvFeedbackTitle.setText(levelText(level))
        tvFeedbackTitle.setTextColor(color)
        tvFeedbackScore.text = percent(grade.accuracy)
        tvFeedbackScore.background = pill(color)
        tvFeedbackStats.text = getString(
            R.string.test_feedback_stats, getArabicNumber(grade.correctCount),
            getArabicNumber(grade.missingCount), getArabicNumber(grade.extraCount), getArabicNumber(grade.points),
        )
        tvFeedbackDiff.text = if (grade.correctCount == 0 && grade.extraCount == 0) {
            SpannableStringBuilder(getString(R.string.test_empty_answer)).also {
                it.setSpan(ForegroundColorSpan(color(R.color.test_wrong)), 0, it.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        } else {
            diffText(grade)
        }
        tvLegend.text = legend()
        tvCorrectAyah.text = answer.text
    }

    /** word-level colours: correct green, missing red on pink, extra amber struck through */
    private fun diffText(grade: MemorizationTest.Grade): CharSequence {
        val sb = SpannableStringBuilder()
        grade.words.forEachIndexed { i, w ->
            if (i > 0) sb.append(' ')
            val start = sb.length
            sb.append(w.text)
            val end = sb.length
            when (w.state) {
                WordState.CORRECT -> sb.setSpan(ForegroundColorSpan(color(R.color.test_correct)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                WordState.MISSING -> {
                    sb.setSpan(ForegroundColorSpan(color(R.color.test_wrong)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    sb.setSpan(BackgroundColorSpan(color(R.color.test_wrong_bg)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                WordState.EXTRA -> {
                    sb.setSpan(ForegroundColorSpan(color(R.color.test_extra)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    sb.setSpan(StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        return sb
    }

    private fun legend(): CharSequence {
        val sb = SpannableStringBuilder()
        listOf(
            R.string.test_legend_correct to R.color.test_correct,
            R.string.test_legend_missing to R.color.test_wrong,
            R.string.test_legend_extra to R.color.test_extra,
        ).forEachIndexed { i, (text, c) ->
            if (i > 0) sb.append("   ")
            val start = sb.length
            sb.append(getString(text))
            sb.setSpan(ForegroundColorSpan(color(c)), start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return sb
    }

    private fun next() {
        if (current < questions.lastIndex) {
            current++
            bindQuestion()
        } else {
            showResult()
        }
    }
    // endregion

    // region result
    private fun showResult() = with(binding) {
        show(State.RESULT)
        val expected = grades.sumOf { it.expectedCount }
        val extra = grades.sumOf { it.extraCount }
        val correct = grades.sumOf { it.correctCount }
        val total = expected + extra
        val accuracy = if (total == 0) 0 else (correct * 100 + total / 2) / total
        val level = MemorizationTest.level(accuracy)
        val color = color(levelColor(level))
        resultCircle.setIndicatorColor(color)
        resultCircle.max = 100
        resultCircle.setProgressCompat(accuracy, true)
        tvResultPercent.text = percent(accuracy)
        tvResultLevel.setText(levelText(level))
        tvResultLevel.setTextColor(color)

        resultStats.removeAllViews()
        statRow(R.string.test_result_ayahs, getArabicNumber(grades.size))
        statRow(R.string.test_result_perfect, getArabicNumber(grades.count { it.isPerfect }))
        statRow(R.string.test_result_words, "${getArabicNumber(correct)} / ${getArabicNumber(expected)}")
        statRow(R.string.test_result_points, "+" + getArabicNumber(grades.sumOf { it.points }))
        statRow(R.string.test_result_total, getArabicNumber(quranRepository.score))
        testScroll.scrollTo(0, 0)
    }

    private fun statRow(label: Int, value: String) {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(6), 0, dp(6))
        }
        val l = TextView(requireContext()).apply {
            setText(label)
            textSize = 14f
            setTextColor(color(R.color.search_muted))
            textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            UiFont.apply(this)
        }
        val v = TextView(requireContext()).apply {
            text = value
            textSize = 15f
            setTextColor(color(R.color.search_ayah_text))
            UiFont.applyBold(this)
        }
        row.addView(l, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(v)
        binding.resultStats.addView(row)
    }
    // endregion

    private enum class State { SETUP, QUESTION, RESULT, EMPTY }

    private fun show(state: State) = with(binding) {
        setupLayout.visibility = if (state == State.SETUP) View.VISIBLE else View.GONE
        lnTestLayout.visibility = if (state == State.QUESTION) View.VISIBLE else View.GONE
        resultLayout.visibility = if (state == State.RESULT) View.VISIBLE else View.GONE
        testEmpty.visibility = if (state == State.EMPTY) View.VISIBLE else View.GONE
        if (state == State.SETUP) updateRangeSummary()
    }

    private fun updateTotalPoints() {
        binding.testTotalPoints.text = ScoreLevels.pointsLabel(quranRepository.score)
    }

    private fun suraName(s: SuraItem) = Data.SURA_NAMES.getOrElse(s.index - 1) { s.name }
    private fun suraNameOf(a: AyahItem) = Data.SURA_NAMES.getOrElse(a.surahIndex - 1) { "" }

    private fun ayahsCount(n: Int): String = when {
        n == 1 -> "آية واحدة"
        n == 2 -> "آيتان"
        n % 100 in 3..10 -> "${getArabicNumber(n)} آيات"
        else -> "${getArabicNumber(n)} آية"
    }

    private fun percent(p: Int) = getArabicNumber(p) + "٪"

    @ColorRes
    private fun levelColor(level: Level) = when (level) {
        Level.EXCELLENT, Level.VERY_GOOD -> R.color.test_correct
        Level.GOOD -> R.color.test_extra
        Level.NEEDS_REVIEW -> R.color.test_wrong
    }

    private fun levelText(level: Level) = when (level) {
        Level.EXCELLENT -> R.string.test_level_excellent
        Level.VERY_GOOD -> R.string.test_level_very_good
        Level.GOOD -> R.string.test_level_good
        Level.NEEDS_REVIEW -> R.string.test_level_review
    }

    private fun color(@ColorRes id: Int) = ContextCompat.getColor(requireContext(), id)

    private fun pill(color: Int) = GradientDrawable().apply {
        cornerRadius = dp(12).toFloat()
        setColor(ColorStateList.valueOf(color))
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onResume() {
        super.onResume()
        // back from the points screen
        if (view != null) updateTotalPoints()
    }

    override fun onDestroyView() {
        view?.let { KeyboardHelper.hideKeyboard(it) }
        activity?.let { EdgeToEdge.setStatusBarIconsDark(it, EdgeToEdge.isSystemNight(it)) }
        super.onDestroyView()
    }

    companion object {
        private const val MAX_FULL_AYAHS = 50
    }
}
