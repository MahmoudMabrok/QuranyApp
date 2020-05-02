package education.mahmoud.quranyapp.feature.show_sura_ayas

import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.PublishRelay
import education.mahmoud.quranyapp.data_layer.Repository
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem
import education.mahmoud.quranyapp.data_layer.local.room.BookmarkItem
import education.mahmoud.quranyapp.data_layer.local.room.ReadLog
import io.reactivex.disposables.CompositeDisposable

class AyahsViewModel(private val repo: Repository) : ViewModel() {

    val backColorState = repo.backColorState
    val nightModeState = repo.nightModeState
    val hizbQuaterStart: List<Int> = repo.hizbQuaterStart

    val replay = PublishRelay.create<List<Page>>()

    val bg = CompositeDisposable()



    fun addLatestread(lastpageShown: Int) {
        repo.addLatestread(lastpageShown)
    }

    fun addReadLog(readLog: ReadLog) {
        repo.addReadLog(readLog)
    }

    fun updateReadLog(readLog: ReadLog) {
        repo.updateReadLog(readLog)
    }

    fun getLReadLogByDate(currentDateStr: String): ReadLog? {
        return repo.getLReadLogByDate(currentDateStr)
    }

    fun getAyahsByPage(i: Int): List<AyahItem> {
        return repo.getAyahsByPage(i)
    }

    fun getLatestRead(): Int {
        return repo.latestRead
    }

    fun getPageFromSurahAndAyah(surah: Int, ayah: Int): Int {
        return repo.getPageFromSurahAndAyah(surah, ayah)
    }

    fun getPageFromJuz(pos: Int): Int {
        return repo.getPageFromJuz(pos)
    }

    fun getSuraStartpage(pos: Int): Int {
        return repo.getSuraStartpage(pos)
    }

    fun addBookmark(bookmarkItem: BookmarkItem) {
        repo.addBookmark(bookmarkItem)
    }

    override fun onCleared() {
        super.onCleared()
        bg.clear()
    }

}