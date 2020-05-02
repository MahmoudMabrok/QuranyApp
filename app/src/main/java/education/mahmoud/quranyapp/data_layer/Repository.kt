package education.mahmoud.quranyapp.data_layer

import education.mahmoud.quranyapp.data_layer.local.LocalShared
import education.mahmoud.quranyapp.data_layer.local.room.*


class Repository(private var localShared: LocalShared,
                 private var quranDB: QuranDB) {
    // shared
    fun addLatestread(index: Int) {
        localShared.addLatestread(index)
    }

    fun addLatestreadWithScroll(index: Int) {
        localShared.addLatestreadWithScroll(index)
    }

    val latestRead: Int
        get() = localShared.latestRead

    val latestReadWithScroll: Int
        get() = localShared.latestReadWithScroll

    var permissionState: Boolean
        get() = localShared.permissionState
        set(state) {
            localShared.permissionState = state
        }

    var nightModeState: Boolean
        get() = localShared.nightModeState
        set(state) {
            localShared.nightModeState = state
        }

    var backColorState: Int
        get() = localShared.backColorState
        set(color) {
            localShared.backColorState = color
        }

    var score: Long
        get() = localShared.score
        set(score) {
            localShared.score = score
        }

    // suarh db operation
    fun addSurah(suraItem: SuraItem) {
        quranDB.surahDAO().addSurah(suraItem)
    }

    val surasNames: List<String>
        get() = quranDB.surahDAO().allSurahNames

    fun getSuraByName(name: String): SuraItem? {
        return quranDB.surahDAO().getSurahByName(name)
    }

    // ayah db operation
    fun addAyah(item: AyahItem) {
        quranDB.ayahDAO().addAyah(item)
    }

    val totlaAyahs: Int
        get() = quranDB.ayahDAO().ayahCount

    fun getAyahsOfSura(index: Int): List<AyahItem> {
        return quranDB.ayahDAO().getAllAyahOfSurahIndex(index)
    }

    fun getAyahSInRange(start: Int, end: Int): List<AyahItem> {
        return quranDB.ayahDAO().getAyahSInRange(start, end)
    }

    fun getAyahByAyahText(text: String): List<AyahItem> {
        return quranDB.ayahDAO().getAyahByAyahText(text)
    }

    val ayahNumberNotAudioDownloaded: List<Int>
        get() = quranDB.ayahDAO().ayahNumberNotAudioDownloaded

    fun getAyahByInSurahIndex(index: Int, ayahIndex: Int): AyahItem? {
        return quranDB.ayahDAO().getAyahByInSurahIndex(index, ayahIndex)
    }

    fun getAyahByIndex(index: Int): AyahItem? {
        return quranDB.ayahDAO().getAyahByIndex(index)
    }

    fun updateAyahItem(item: AyahItem) {
        quranDB.ayahDAO().updateAyah(item)
    }

    val lastDownloadedChapter: Int
        get() = quranDB.ayahDAO().lastChapter

    val lastDownloadedAyahAudio: Int
        get() = quranDB.ayahDAO().lastDownloadedAyahAudio

    // bookmark
    val bookmarks: List<BookmarkItem>
        get() = quranDB.bookmarkDao().bookmarks

    fun addBookmark(item: BookmarkItem) {
        quranDB.bookmarkDao().addBookmark(item)
    }

    fun deleteBookmark(item: BookmarkItem) {
        quranDB.bookmarkDao().delteBookmark(item)
    }

    // remote data
    val currentUserUUID: String?
        get() = localShared.userUUID

    var userName: String?
        get() = localShared.userName
        set(userName) {
            localShared.userName = userName
        }

    fun setUserUUID(uuid: String) {
        localShared.userUUID = uuid
    }

    fun getAyahsOfSura(suraName: String): List<AyahItem> {
        return quranDB.ayahDAO().getAllAyahOfSurahByName(suraName)
    }

    fun getAyahsByPage(i: Int): List<AyahItem> {
        return quranDB.ayahDAO().getAyahsByPage(i)
    }

    val suras: List<SuraItem> = quranDB.surahDAO().allSurah


    fun getSuraStartpage(index: Int): Int {
        return quranDB.ayahDAO().getSuraStartpage(index)
    }

    fun getAllAyahOfSurahIndex(l: Long): List<AyahItem> {
        return quranDB.ayahDAO().getAllAyahOfSurahIndex(l.toInt())
    }

    val totalTafseerDownloaded: Int
        get() = quranDB.ayahDAO().totalTafseerDownloaded

    val totalAudioDownloaded: Int
        get() = quranDB.ayahDAO().totalAudioDownloaded

    fun getSuraByIndex(l: Long): SuraItem {
        return quranDB.surahDAO().getSurahByIndex(l.toInt())
    }

    fun getPageFromJuz(pos: Int): Int {
        return quranDB.ayahDAO().getPageFromJuz(pos)
    }

    fun getPageFromSurahAndAyah(surah: Int, ayah: Int): Int {
        return quranDB.ayahDAO().getPageFromSurahAndAyah(surah, ayah)
    }

    fun getAllAyahOfSurahIndexForTafseer(l: Long): List<AyahItem> {
        return quranDB.ayahDAO().getAllAyahOfSurahIndexForTafseer(l)
    }

    val hizbQuaterStart: List<Int>
        get() = quranDB.ayahDAO().hizbQuaterStart

    fun getLReadLogByDate(currentDate: Long): ReadLog? {
        return quranDB.readLogDAO().getReadLogBydate(currentDate)
    }

    fun getLReadLogByDate(currentDate: String): ReadLog? {
        return quranDB.readLogDAO().getReadLogBydate(currentDate)
    }

    val readLog: List<ReadLog>
        get() = quranDB.readLogDAO().allReadLog

    fun addReadLog(readLog: ReadLog) {
        quranDB.readLogDAO().addReadLog(readLog)
    }

    fun updateReadLog(readLog: ReadLog) {
        quranDB.readLogDAO().updateReadLog(readLog)
    }

    fun deleteReadLog(readLog: ReadLog) {
        quranDB.readLogDAO().deleteReadLog(readLog)
    }

    fun addRecord(recordItem: RecordItem) {
        quranDB.recordDAO().addRecordItem(recordItem)
    }

    val records: List<RecordItem>
        get() = quranDB.recordDAO().allRecordItem

    val recordCount: Int
        get() = quranDB.recordDAO().recordCount


    fun updateRecordItem(path: RecordItem) {
        quranDB.recordDAO().updateRecordItem(path)
    }

}