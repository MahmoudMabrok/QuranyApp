package education.mahmoud.quranyapp.data_layer.local.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RecordDAO {
    @Insert
    public void addRecordItem(RecordItem item);

    @Update
    public void updateRecordItem(RecordItem item);

    @Query("select * from records")
    public List<RecordItem> getAllRecordItem();

    @Query("select count(*) from records")
    int getRecordCount();
}
