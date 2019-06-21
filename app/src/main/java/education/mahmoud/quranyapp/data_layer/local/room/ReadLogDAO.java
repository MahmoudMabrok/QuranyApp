package education.mahmoud.quranyapp.data_layer.local.room;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ReadLogDAO {

    @Insert
    public void addReadLog(ReadLog item);

    @Update
    public void updateReadLog(ReadLog item);

    @Query("select * from readlog")
    public List<ReadLog> getAllReadLog();

    @Query("select * from readlog where date = :date")
    ReadLog getReadLogBydate(long date);

    @Delete
    void deleteReadLog(ReadLog readLog);
}
