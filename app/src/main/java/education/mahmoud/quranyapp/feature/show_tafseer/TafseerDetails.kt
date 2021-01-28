package education.mahmoud.quranyapp.feature.show_tafseer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.BaseFragment
import education.mahmoud.quranyapp.datalayer.Repository
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import kotlinx.android.synthetic.main.fragment_tafseer_details.*
import org.koin.android.ext.android.inject
import java.util.*

class TafseerDetails : BaseFragment(), BaseFragment.InitListener {
    private val repository: Repository by inject()
    private val adapter = TafseerAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setOnInitListeners(this)
        return inflater.inflate(R.layout.fragment_tafseer_details, container, false)
    }

    override fun initViews(view: View) {
        initSuraTafseerSpinner()
        initRv()
    }

    override fun setClickListeners() {
        tvNoDataInTafseer.setOnClickListener {
            //NOTHING TO DO
        }
    }

    private fun initRv() {
        rvTafeer.setHasFixedSize(true)
        rvTafeer.adapter = adapter
    }

    private fun initSuraTafseerSpinner() {
        val suraNames = repository.surasNames
        val startAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suraNames)
        spSuraTafser.adapter = startAdapter
        spSuraTafser.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, rowId: Long) {
                val ayahs = repository.getAllAyahOfSurahIndexForTafseer(rowId + 1)
                Log.d(TAG, "onItemSelected: " + ayahs.size + " & " + rowId)
                // update adapter
                if (ayahs.isNotEmpty()) {
                    foundState()
                    adapter.setTafseerList(ayahs)
                    setAyahsSpinner(ayahs)
                } else {
                    notFoundState()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun setAyahsSpinner(ayahs: List<AyahItem>) {
        val ayahsNums = creatAyahsNumList(ayahs.size) // size() ->  n of ayahs
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ayahsNums)
        spAyahTafser.adapter = adapter
        spAyahTafser.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                rvTafeer.scrollToPosition(l.toInt())
                Log.d(TAG, "onItemSelected: TAFSEEER POS $l")
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun creatAyahsNumList(i: Int): List<String> {
        val ayahsNums: MutableList<String> = ArrayList()
        for (j in 1..i) {
            ayahsNums.add(j.toString())
        }
        return ayahsNums
    }

    private fun foundState() {
        rvTafeer.visibility = View.VISIBLE
        spSuraTafser.visibility = View.VISIBLE
        spAyahTafser.visibility = View.VISIBLE
        tvNoDataInTafseer.visibility = View.GONE
    }

    private fun notFoundState() {
        rvTafeer.visibility = View.GONE
        spSuraTafser.visibility = View.INVISIBLE
        spAyahTafser.visibility = View.INVISIBLE
        tvNoDataInTafseer.visibility = View.VISIBLE
    }
}