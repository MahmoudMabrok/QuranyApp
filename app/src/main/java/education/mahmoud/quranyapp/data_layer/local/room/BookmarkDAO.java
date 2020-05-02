package education.mahmoud.quranyapp.data_layer.local.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BookmarkDAO {

    @Insert
    void addBookmark(BookmarkItem item);

    @Update
    void updateBookmark(BookmarkItem item);

    @Delete
    void delteBookmark(BookmarkItem item);

    @Query("select * from bookmark")
    List<BookmarkItem> getBookmarks();


}
