package education.mahmoud.quranyapp.feature.test_quran

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.BaseFragment
import education.mahmoud.quranyapp.datalayer.Repository
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.datalayer.local.room.SuraItem
import education.mahmoud.quranyapp.utils.Util
import kotlinx.android.synthetic.main.fragment_test.*
import org.koin.android.ext.android.inject
import java.util.*

class TestFragment : BaseFragment(), BaseFragment.InitListener {
    var adapter = SaveTestAdapter()
    private val repository: Repository by inject()

    private var startSura: SuraItem? = null
    private var endSura: SuraItem? = null
    private var actualStart = 0
    private var actualEnd = 0
    private var ayahsToTest = mutableListOf<AyahItem>()
    private var isInputValid = false
    private var start = 0
    private var end = 0

    // Ayahs used to be compared with user input
    private lateinit var ayahItemTobeTest: AyahItem
    private var isFullTest = false
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? { // Inflate the layout for getContext() fragment
        return inflater.inflate(R.layout.fragment_test, container, false)
    }

    override fun initViews(view: View) {
        initSpinners()
        initRV()
    }

    /**
     * @param isVisibleToUser
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && lnSelectorAyahs != null) {
            selectionState()
        }
    }

    /**
     * used to initialize the recyclerview
     */
    private fun initRV() {
        val manager = LinearLayoutManager(context)
        rvTestText.layoutManager = manager
        rvTestText.adapter = adapter
        rvTestText.setHasFixedSize(true)
        adapter.setiOnTestClick { item, editText ->
            // processing of user text
            val ayah = editText.text.toString()
            val spannable = Util.getDiffSpannaled(item.textClean, ayah)
            updateTotalScore(Util.getTotalScore()) // Util.getTotalScore() -> score for yours Save test
            editText.setText(spannable, TextView.BufferType.SPANNABLE)
        }
    }

    /**
     * called after finish test and used to update score in db and show score for current test
     *
     * @param totalScore
     */
    private fun updateTotalScore(totalScore: Long) {
        var cuurentTotalScore = repository.score // saved score
        cuurentTotalScore += totalScore // update with new score
        repository.score = cuurentTotalScore // set in db
        Util.getDialog(context, cuurentTotalScore.toString(), getString(R.string.score)).show()
    }
    //region spinners
    /**
     * load data to spinners from db
     */
    private fun initSpinners() {
        val suraNames = repository.surasNames
        val startAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suraNames)
        spStartSura.adapter = startAdapter
        val endAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suraNames)
        spEndSura.adapter = endAdapter
        spStartSura.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                startSura = repository.getSuraByIndex(l + 1)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        spEndSura.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                endSura = repository.getSuraByIndex(l + 1)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }
    //endregion

    override fun setClickListeners() {
        btnTestSave.setOnClickListener {
            onViewClicked()
        }

        btnTestSaveRandom.setOnClickListener {
            onbtnTestSaveRandom()
        }

        btnCheckTest.setOnClickListener {
            onCheckClicked()
        }
    }

    /**
     * called when btn is clicked it first check inputs then load ayahs from db
     */
    fun onViewClicked() {
        checkInput()
        if (isInputValid) {
            ayahsToTest = repository.getAyahSInRange(actualStart + 1, actualEnd + 1).toMutableList()
            // // TODO: 7/15/2019 make list
            adapter.setAyahItemList(ayahsToTest)
            TestState()
            isFullTest = true
        }
    }

    /**
     * check input of spinners and edit text if it valid a field is become true
     */
    private fun checkInput() { //region check inputs
        if (startSura != null && endSura != null) {
            try {
                start = edStartSuraAyah.text.toString().toInt()
                if (start > startSura!!.numOfAyahs) {
                    edStartSuraAyah.error = getString(R.string.outofrange, startSura!!.numOfAyahs)
                    return
                }
                end = edEndSuraAyah.text.toString().toInt()
                if (end > endSura!!.numOfAyahs) {
                    edEndSuraAyah.error = getString(R.string.outofrange, endSura!!.numOfAyahs)
                    return
                }
                // compute actual start , -1 because first ayah is 0 not 1 as user enter
                actualStart = repository.getAyahByInSurahIndex(startSura!!.index, start).ayahIndex - 1
                // compute actual end
                actualEnd = repository.getAyahByInSurahIndex(endSura!!.index, end).ayahIndex - 1
                // check actualstart & actualEnd
                if (actualEnd < actualStart) {
                    makeRangeError()
                    return
                }
                Log.d(TAG, "onViewClicked: actual $actualStart $actualEnd")
                // get ayas from db
                ayahsToTest = repository.getAyahSInRange(actualStart + 1, actualEnd + 1).toMutableList()
                isInputValid = true
                // place data in UI
                tvTestRange.text = getString(R.string.rangeoftest, startSura!!.name, start, endSura!!.name, end)
                // close keyboard
                closeKeyboard()
            } catch (e: NumberFormatException) {
                makeRangeError()
                isInputValid = false
            }
        } else {
            showMessage(getString(R.string.sura_select_error))
        }
        //endregion
    }

    /**
     * used to make test layout shown to screen and hide selections
     */
    private fun TestState() {
        lnSelectorAyahs.visibility = View.GONE
        lnTestLayout.visibility = View.VISIBLE
        // used  only with random test
        tvAyahToTestAfter.visibility = View.GONE
    }

    /**
     * used to make Selection layout shown to screen and hide test layouts
     */
    private fun selectionState() {
        lnSelectorAyahs.visibility = View.VISIBLE
        lnTestLayout.visibility = View.GONE
        adapter.clear()
        // clear inputs1
        edEndSuraAyah.text = null
        edStartSuraAyah.text = null
        edEndSuraAyah.error = null
        edStartSuraAyah.error = null
    }

    /**
     * if range of ayahs is incorrect it raise error messages
     */
    private fun makeRangeError() {
        edStartSuraAyah.error = getString(R.string.start_range_error)
        showMessage(getString(R.string.start_range_error))
    }

    /**
     * close keyboard after user click to make screen free
     */
    private fun closeKeyboard() {
        Util.hideInputKeyboard(context)
    }

    /**
     * show message by Toast
     *
     * @param message message to be shown
     */
    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun onbtnTestSaveRandom() {
        checkInput()
        if (isInputValid) {
            ayahsToTest = repository.getAyahSInRange(actualStart + 1, actualEnd + 1).toMutableList()
            if (ayahsToTest.size >= 3) {
                val r = Random().nextInt(ayahsToTest.size - 1)
                ayahItemTobeTest = ayahsToTest.get(r)
                tvAyahToTestAfter.text = getString(R.string.ayahToTestRanom, ayahItemTobeTest.textClean)
                ayahItemTobeTest = ayahsToTest.get(r + 1)
                ayahsToTest.clear()
                ayahsToTest.add(ayahItemTobeTest)
                adapter.setAyahItemList(ayahsToTest)
                // make list, used for Check
                ayahsToTest = ArrayList()
                ayahsToTest.add(ayahItemTobeTest)
                TestRandomState()
                isFullTest = false
            } else {
                ayahsNotSufficentError()
            }
        }
    }

    private fun TestRandomState() {
        tvAyahToTestAfter.visibility = View.VISIBLE
        lnSelectorAyahs.visibility = View.GONE
        lnTestLayout.visibility = View.VISIBLE
        isFullTest = true
    }

    private fun ayahsNotSufficentError() {
        edStartSuraAyah.error = getString(R.string.not_sufficient_ayahs)
        edEndSuraAyah.error = getString(R.string.not_sufficient_ayahs)
    }


    fun onCheckClicked() {
        val ayah: String = edUserTextForAyahs.text.toString()
        val ayahToTestStr = ayah
        val spannable = Util.getDiffSpannaled(ayahToTestStr, ayah)
        updateTotalScore(Util.getTotalScore()) // Util.getTotalScore() -> score for yours Save test
        edUserTextForAyahs.setText(spannable, TextView.BufferType.SPANNABLE)
    }

    private val ayah: String
        private get() = if (isFullTest) {
            concateAyahs()
        } else {
            ayahsToTest[0].textClean
        }

    private fun concateAyahs(): String {
        val builder = StringBuilder()
        for (ayahItem in ayahsToTest) {
            builder.append(ayahItem.textClean)
        }
        return builder.toString()
    }

    companion object {
        private const val TAG = "TestFragment"
    }
}