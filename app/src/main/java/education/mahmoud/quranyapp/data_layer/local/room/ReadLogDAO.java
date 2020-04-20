package education.mahmoud.quranyapp.data_layer.local.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReadLogDAO {

    @Insert
    public void addReadLog(ReadLog item);

    @Update
    public void updateReadLog(ReadLog item);

    @Query("select * from readlog order by date desc")
    public List<ReadLog> getAllReadLog();

    @Query("select * from readlog where date = :date")
    ReadLog getReadLogBydate(long date);

    @Query("select * from readlog where strDate = :currentDate")
    ReadLog getReadLogBydate(String currentDate);

    @Delete
    void deleteReadLog(ReadLog readLog);
}
