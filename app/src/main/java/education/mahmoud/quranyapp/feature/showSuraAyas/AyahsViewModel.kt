package education.mahmoud.quranyapp.feature.showSuraAyas

import androidx.lifecycle.ViewModel
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.datalayer.local.room.BookmarkItem
import education.mahmoud.quranyapp.datalayer.local.room.ReadLog
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderStyle
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafLook
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafPageTurn
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafReaderSettings
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafSpreadMode
import io.reactivex.disposables.CompositeDisposable

class AyahsViewModel(private val repo: QuranRepository) : ViewModel() {

    val backColorState = repo.backColorState
    val nightModeState = repo.nightModeState

    /** Snapshot of the text-mushaf preferences. Unknown stored ids fall back to the previous reader. */
    val mushafReaderSettings = MushafReaderSettings(
        headerStyle = SuraHeaderStyle.fromId(repo.mushafHeaderStyle),
        pageFrame = repo.mushafPageFrame,
        look = MushafLook.fromId(repo.mushafLook),
        divisionMarks = repo.mushafDivisionMarks,
        pageTurn = MushafPageTurn.fromId(repo.mushafPageTurn),
        spread = MushafSpreadMode.fromId(repo.mushafSpread),
        tajweed = repo.mushafTajweed,
        naturalMadd = repo.mushafNaturalMadd,
    )

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

    val highlightedAyahs: Set<Int>
        get() = repo.highlightedAyahs

    fun setAyahHighlighted(ayahIndex: Int, highlighted: Boolean) {
        repo.setAyahHighlighted(ayahIndex, highlighted)
    }

    /** Fresh copy from the database (tafseer and audio path may have been downloaded since). */
    fun getAyah(ayahIndex: Int): AyahItem? = runCatching { repo.getAyahByIndex(ayahIndex) }.getOrNull()

    override fun onCleared() {
        super.onCleared()
        bg.clear()
    }
}
