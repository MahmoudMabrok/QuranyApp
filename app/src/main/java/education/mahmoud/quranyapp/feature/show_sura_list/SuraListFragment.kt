package education.mahmoud.quranyapp.feature.show_sura_list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import butterknife.OnClick
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.data_layer.Repository
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem
import education.mahmoud.quranyapp.feature.download.DownloadActivity
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowAyahsActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_sura_list.*
import org.koin.android.ext.android.inject
import java.util.*

/**
 * Refactored to Kotlin
 * using KTX instead of  findViewById and ButterKnif (later use ViewBinding)
 */
class SuraListFragment : Fragment(R.layout.fragment_sura_list) {

    var suraItems: List<SuraItem> = ArrayList()
    var suraAdapter: SuraAdapter = SuraAdapter()

    var repository: Repository? = null

    val model: SuraListViewModel by inject()
    val bg = CompositeDisposable()

    private lateinit var screen:RecyclerViewSkeletonScreen


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
                    hideLoading()
                }
                .subscribe {
                    suraAdapter.setStringList(it)
                }
                .addTo(bg)
    }

    private fun hideLoading() {
        screen.hide()
    }

    private fun initRV() {
        suraAdapter = SuraAdapter()
        rvSura?.adapter = suraAdapter
        rvSura?.setHasFixedSize(true)
        suraAdapter.setSuraListner { pos ->
            gotoSuraa(pos)
            Log.d(TAG, "onSura: $pos")
        }

        screen  = Skeleton.bind(rvSura)
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

    @OnClick(R.id.tv_no_quran_data)
    fun onViewClicked() {
        val openAcivity = Intent(context, DownloadActivity::class.java)
        context?.startActivity(openAcivity)
    }



    companion object {
        private const val TAG = "SuraListFragment"
    }
}