package education.mahmoud.quranyapp.data_layer.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecordDAO {
    @Insert
    fun addRecordItem(item: RecordItem)

    @Update
    fun updateRecordItem(item: RecordItem)

    @get:Query("select * from records")
    val allRecordItem: List<RecordItem>

    @get:Query("select count(*) from records")
    val recordCount: Int
}