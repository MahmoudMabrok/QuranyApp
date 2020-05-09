package education.mahmoud.quranyapp.datalayer.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReadLogDAO {
    @Insert
    fun addReadLog(item: ReadLog)

    @Update
    fun updateReadLog(item: ReadLog)

    @get:Query("select * from readlog order by date desc")
    val allReadLog: List<ReadLog>

    @Query("select * from readlog where date = :date")
    fun getReadLogBydate(date: Long): ReadLog?

    @Query("select * from readlog where strDate = :currentDate")
    fun getReadLogBydate(currentDate: String?): ReadLog?

    @Delete
    fun deleteReadLog(readLog: ReadLog)
}