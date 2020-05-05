package education.mahmoud.quranyapp.datalayer.local.room

import androidx.room.*
import io.reactivex.Flowable

@Dao
interface BookmarkDAO {
    @Insert
    fun addBookmark(item: BookmarkItem)

    @Update
    fun updateBookmark(item: BookmarkItem)

    @Delete
    fun deleteBookmark(item: BookmarkItem)

    @Query("select * from bookmark")
    fun loadBookmarks(): Flowable<List<BookmarkItem>>
}