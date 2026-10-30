package education.mahmoud.quranyapp.datalayer.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Flowable

@Dao
interface SurahDAO {
    @Insert
    fun addSurah(suraItem: SuraItem)

    @Update
    fun updateSurah(suraItem: SuraItem)

    @get:Query("select  * from surah ")
    val allSurah: List<SuraItem>

    /** Emits the current Surah list and again whenever the table changes (e.g. first-launch import). */
    @Query("select  * from surah ")
    fun observeAllSurah(): Flowable<List<SuraItem>>

    @get:Query("select name from surah ")
    val allSurahNames: List<String>

    @Query("select  * from surah where `index` = :id")
    fun getSurahByIndex(id: Int): SuraItem

    @Query("select  * from surah where `name` = :name")
    fun getSurahByName(name: String): SuraItem?

    @Query("select startIndex from surah where `index` = :index")
    fun getSuraStartpage(index: Int): Int

    @Insert
    fun addSurahs(surrahs: List<SuraItem>)
}
