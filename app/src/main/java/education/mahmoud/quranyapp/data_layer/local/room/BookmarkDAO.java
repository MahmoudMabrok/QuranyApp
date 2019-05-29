package education.mahmoud.quranyapp.data_layer.local.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface BookmarkDAO {

    @Insert
    public void addBookmark(BookmarkItem item);

    @Update
    public void updateBookmark(BookmarkItem item);

    @Delete
    public void delteBookmark(BookmarkItem item);

    @Query("select * from bookmark")
    public List<BookmarkItem> getBookmarks();


}
