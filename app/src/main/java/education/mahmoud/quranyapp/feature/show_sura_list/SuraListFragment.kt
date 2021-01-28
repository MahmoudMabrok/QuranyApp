package education.mahmoud.quranyapp.feature.show_sura_list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.BaseFragment
import education.mahmoud.quranyapp.feature.showSuraAyas.ShowAyahsActivity
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_sura_list.*
import org.koin.android.ext.android.inject

/**
 * Refactored to Kotlin
 * using KTX instead of  findViewById and ButterKnif (later use ViewBinding)
 */
class SuraListFragment : BaseFragment(), BaseFragment.InitListener {

    var suraAdapter: SuraAdapter = SuraAdapter()
    val model: SuraListViewModel by inject()

    override fun initViews(view: View) {
        initRV()
        loadSuraList()
        startObserving()
    }

    override fun setClickListeners() {
        suraAdapter.setSuraListner { pos ->
            gotoSuraa(pos)
            Log.d(TAG, "onSura: $pos")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setOnInitListeners(this)
        return inflater.inflate(R.layout.fragment_sura_list, container, false)
    }

    private fun startObserving() {
        model.replay.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterNext {
                    "accept".log()
                    hideLoad()
                }
                .subscribe {
                    "add ${it.size}".log()
                    suraAdapter.setStringList(it)
                }
    }

    private fun initRV() {
        suraAdapter = SuraAdapter()
        rvSura?.adapter = suraAdapter
        rvSura?.setHasFixedSize(true)
    }

    private fun loadSuraList() {
        showSkeleton(rvSura, R.layout.sura_item_skelton, suraAdapter)
        Log.d(TAG, "loadSuraList: ")
        model.loadSura()
    }

    private fun gotoSuraa(index: Int) {
        val openAcivity = Intent(context, ShowAyahsActivity::class.java)
        openAcivity.putExtra(Constants.SURAH_INDEX, index)
        startActivity(openAcivity)
    }
}