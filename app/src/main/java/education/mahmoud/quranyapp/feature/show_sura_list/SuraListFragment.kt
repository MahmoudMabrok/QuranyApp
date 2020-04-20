package education.mahmoud.quranyapp.feature.show_sura_list

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.github.ybq.android.spinkit.SpinKitView
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.Util.Constants
import education.mahmoud.quranyapp.data_layer.Repository
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem
import education.mahmoud.quranyapp.feature.download.DownloadActivity
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowAyahsActivity
import kotlinx.android.synthetic.main.fragment_sura_list.*
import java.util.*

/**
 * Refactored to Kotlin
 * using KTX instead of  findViewById and ButterKnif (later use ViewBinding)
 */
class SuraListFragment : Fragment() {

    var handler: Handler? = null
    var suraItems: List<SuraItem> = ArrayList()
    var suraAdapter: SuraAdapter? = null
    var unbinder: Unbinder? = null
    var repository: Repository? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sura_list, container, false)
        unbinder = ButterKnife.bind(this, view)
        repository = Repository.getInstance(activity!!.application)
        initRV()
        Log.d(TAG, "onCreateView: ")
        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                Log.d(TAG, "handleMessage: ")
                super.handleMessage(msg)
                if (spShowAyahs != null) {
                    spShowAyahs!!.visibility = View.GONE
                    if (suraItems.isNotEmpty()) {
                        suraAdapter!!.setStringList(suraItems)
                        rvSura!!.visibility = View.VISIBLE
                        tvNoQuranData!!.visibility = View.GONE
                    } else {
                        rvSura!!.visibility = View.GONE
                        tvNoQuranData!!.visibility = View.VISIBLE
                    }
                }
            }
        }
        loadSuraList()
        return view
    }

    private fun initRV() {
        suraAdapter = SuraAdapter()
        rvSura!!.adapter = suraAdapter
        rvSura!!.setHasFixedSize(true)
        suraAdapter!!.setSuraListner { pos ->
            gotoSuraa(pos)
            Log.d(TAG, "onSura: $pos")
        }
    }

    private fun loadSuraList() {
        Log.d(TAG, "loadSuraList: ")
        suraItems = repository!!.suras
        handler!!.sendEmptyMessage(0)
        Log.d(TAG, "loadSuraList: ## " + suraItems.size)
    }

    private fun gotoSuraa(index: Int) {
        val openAcivity = Intent(context, ShowAyahsActivity::class.java)
        openAcivity.putExtra(Constants.SURAH_INDEX, index)
        startActivity(openAcivity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder!!.unbind()
    }

    @OnClick(R.id.tv_no_quran_data)
    fun onViewClicked() {
        val openAcivity = Intent(context, DownloadActivity::class.java)
        context!!.startActivity(openAcivity)
    }

    companion object {
        private const val TAG = "SuraListFragment"
    }
}