package education.mahmoud.quranyapp.feature.show_sura_list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.feature.showSuraAyas.ShowAyahsActivity
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_sura_list.*
import org.koin.android.ext.android.inject

/**
 * Refactored to Kotlin
 * using KTX instead of  findViewById and ButterKnife (later use ViewBinding)
 */
class SuraListFragment : Fragment(R.layout.fragment_sura_list) {

    var suraAdapter: SuraAdapter = SuraAdapter()
    val model: SuraListViewModel by inject()
    val bg = CompositeDisposable()
    private lateinit var screen: RecyclerViewSkeletonScreen

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRV()
        loadSuraList()
        startObserving()
    }

    private fun startObserving() {
        model.replay.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterNext {
                "accept".log()
                hideLoading()
            }
            .subscribe {
                "add ${it.size}".log()
                suraAdapter.setStringList(it)
            }
            .addTo(bg)
    }

    private fun hideLoading() {
        screen.hide()
        suraHeader.visibility = View.VISIBLE
    }

    private fun initRV() {
        suraAdapter = SuraAdapter()
        rvSura?.adapter = suraAdapter
        rvSura?.setHasFixedSize(true)
        suraAdapter.setSuraListner { pos ->
            gotoSuraa(pos)
            Log.d(TAG, "onSura: $pos")
        }

        screen = Skeleton.bind(rvSura)
            .adapter(suraAdapter)
            .count(12)
            .load(R.layout.sura_item_skelton)
            .show()
    }

    private fun loadSuraList() {
        Log.d(TAG, "loadSuraList: ")
        model.loadSura()
    }

    private fun gotoSuraa(index: Int) {
        val openAcivity = Intent(context, ShowAyahsActivity::class.java)
        openAcivity.putExtra(Constants.SURAH_INDEX, index)
        startActivity(openAcivity)
    }

    companion object {
        private const val TAG = "SuraListFragment"
    }
}
