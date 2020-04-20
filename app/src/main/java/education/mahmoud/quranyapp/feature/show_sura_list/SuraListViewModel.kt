package education.mahmoud.quranyapp.feature.show_sura_list

import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.PublishRelay
import education.mahmoud.quranyapp.data_layer.Repository
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem
import education.mahmoud.quranyapp.utils.log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class SuraListViewModel(private val repo: Repository) : ViewModel() {

    val replay = PublishRelay.create<List<SuraItem>>()

    val bg = CompositeDisposable()
    fun loadSura() {
        Single.timer(5, TimeUnit.SECONDS)
                .subscribe { _ ->
                    "got data".log()
                    replay.accept(repo.suras)
                }.addTo(bg)

    }

    override fun onCleared() {
        super.onCleared()
        bg.clear()
    }

}