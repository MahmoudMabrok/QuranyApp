package education.mahmoud.quranyapp.feature.show_tafseer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import butterknife.OnClick
import butterknife.Unbinder
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.BaseFragment
import education.mahmoud.quranyapp.data_layer.Repository
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem
import education.mahmoud.quranyapp.feature.download.DownloadActivity
import education.mahmoud.quranyapp.utils.Data
import kotlinx.android.synthetic.main.fragment_tafseer_details.*
import org.koin.java.KoinJavaComponent
import java.util.*

class TafseerDetails : BaseFragment() {
    var unbinder: Unbinder? = null
    private val repository = KoinJavaComponent.get(Repository::class.java)
    private val sura: SuraItem? = null
    private var suraAyahsTafseer: List<AyahItem>? = null
    private var adapter: TafseerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tafseer_details, container, false)
    }

    override fun initViews(view: View) {
        super.initViews(view)
        fillSpinners()
        initRv()
    }

    private fun initRv() {
        adapter = TafseerAdapter()
        rvTafeer.setHasFixedSize(true)
        rvTafeer.setAdapter(adapter)

    }

    private fun fillSpinners() {
        val suraNames = Arrays.asList(*Data.SURA_NAMES)
        val startAdapter = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, suraNames)
        spSuraTafser.setAdapter(startAdapter)
        spSuraTafser.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                val suraName = spSuraTafser.getSelectedItem() as String
                suraAyahsTafseer = repository.getAllAyahOfSurahIndexForTafseer(l + 1)
                Log.d(TAG, "onItemSelected: " + suraAyahsTafseer!!.size + " & " + l)
                // update adapter
                if (suraAyahsTafseer!!.size > 0) {
                    foundState()
                    adapter!!.setTafseerList(suraAyahsTafseer)
                    // Log.d(TAG, "onItemSelected: " + suraName);
                    val ayahsNums = creatAyahsNumList(suraAyahsTafseer!!.size) // size() ->  n of ayahs
                    val adapter = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, ayahsNums)
                    spAyahTafser.setAdapter(adapter)
                    spAyahTafser.setOnItemSelectedListener(object : OnItemSelectedListener {
                        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                            rvTafeer.scrollToPosition(l.toInt())
                            Log.d(TAG, "onItemSelected: TAFSEEER POS $l")
                        }

                        override fun onNothingSelected(adapterView: AdapterView<*>?) {}
                    })
                } else {
                    notFoundState()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        })
    }

    private fun creatAyahsNumList(i: Int): List<String> {
        val ayahsNums: MutableList<String> = ArrayList()
        for (j in 1..i) {
            ayahsNums.add(j.toString())
        }
        return ayahsNums
    }

    private fun foundState() {
        rvTafeer.setVisibility(View.VISIBLE)
        spSuraTafser.setVisibility(View.VISIBLE)
        spAyahTafser.setVisibility(View.VISIBLE)
        tvNoDataInTafseer.setVisibility(View.GONE)
    }

    private fun notFoundState() {
        rvTafeer.setVisibility(View.GONE)
        spSuraTafser.setVisibility(View.INVISIBLE)
        spAyahTafser.setVisibility(View.INVISIBLE)
        tvNoDataInTafseer.setVisibility(View.VISIBLE)
    }


    @OnClick(R.id.tvNoDataInTafseer)
    fun onViewClicked() {
        val openAcivity = Intent(context, DownloadActivity::class.java)
        context!!.startActivity(openAcivity)
    }

    companion object {
        private const val TAG = "TafseerDetails"
        fun newInstance(title: String?): TafseerDetails {
            return TafseerDetails()
        }
    }
}