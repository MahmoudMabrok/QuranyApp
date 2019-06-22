package education.mahmoud.quranyapp.data_layer.local.room;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface RecordDAO {
    @Insert
    public void addRecordItem(RecordItem item);

    @Update
    public void updateRecordItem(RecordItem item);

    @Query("select * from records")
    public List<RecordItem> getAllRecordItem();
}
