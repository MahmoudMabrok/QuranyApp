package education.mahmoud.quranyapp.datalayer.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AyahDAO {
    @Insert
    fun addAyah(item: AyahItem)

    @Insert
    fun addAyahs(item: List<AyahItem>)

    @Update
    fun updateAyah(item: AyahItem)

    @Query("select  * from ayahs where `surahIndex` = :id order by ayahInSurahIndex asc")
    fun getAllAyahOfSurahIndex(id: Int): List<AyahItem>

    @Query("select  * from ayahs where `ayahIndex` between :start and :end order by ayahIndex asc ")
    fun getAyahSInRange(start: Int, end: Int): List<AyahItem>

    @Query("select  * from ayahs where `ayahIndex` = :id")
    fun getAyahByIndex(id: Int): AyahItem

    @Query("select  * from ayahs where textClean like  '%' || :txt || '%' ")
    fun getAyahByAyahText(txt: String?): List<AyahItem>

    @get:Query("select ayahIndex from ayahs where audioPath is  null ")
    val ayahNumberNotAudioDownloaded: List<Int>

    @get:Query("select count(*) from ayahs")
    val ayahCount: Int

    @get:Query("select max(surahIndex) from ayahs where tafseer is not null ")
    val lastChapter: Int

    @Query("select  * from ayahs where `surahIndex` = :index  and ayahInSurahIndex = :ayahIndex")
    fun getAyahByInSurahIndex(index: Int, ayahIndex: Int): AyahItem

    @get:Query("select max(ayahIndex) from ayahs where audioPath is not null ")
    val lastDownloadedAyahAudio: Int

    @Query("select tafseer , textClean ,ayahIndex, surahIndex, pageNum, juz, hizbQuarter, sajda, manzil, ayahInSurahIndex, text, ay.audioPath from ayahs ay  , surah su where ay.surahIndex = su.`index` and su.name = :suraName  ")
    fun getAllAyahOfSurahByName(suraName: String?): List<AyahItem>

    @Query("select * from ayahs where pageNum = :i order by ayahIndex")
    fun getAyahsByPage(i: Int): List<AyahItem>

    @Query("select pageNum from ayahs where surahIndex = :pos order by ayahInSurahIndex limit 1 ")
    fun getSuraStartpage(pos: Int): Int

    @get:Query("select count(*) from ayahs where tafseer is not null ")
    val totalTafseerDownloaded: Int

    @get:Query("select count(*) from ayahs where audioPath is not null ")
    val totalAudioDownloaded: Int

    @Query("select pageNum from ayahs where juz = :pos order by pageNum asc limit 1 ")
    fun getPageFromJuz(pos: Int): Int

    @Query("select pageNum from ayahs where surahIndex = :surah and ayahInSurahIndex = :ayah order by pageNum asc limit 1 ")
    fun getPageFromSurahAndAyah(surah: Int, ayah: Int): Int

    @Query("select  * from ayahs where `surahIndex` = :l and tafseer is not null")
    fun getAllAyahOfSurahIndexForTafseer(l: Long): List<AyahItem>

    @get:Query("select pageNum from ayahs group by hizbQuarter order by pageNum asc")
    val hizbQuaterStart: List<Int>
}