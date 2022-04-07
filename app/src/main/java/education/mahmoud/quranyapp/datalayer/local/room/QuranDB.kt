package education.mahmoud.quranyapp.datalayer.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [AyahItem::class, SuraItem::class, BookmarkItem::class, ReadLog::class,
    RecordItem::class],
    version = 1,
    exportSchema = false)
@TypeConverters(PagesConverter::class)
abstract class QuranDB : RoomDatabase() {
    abstract fun ayahDAO(): AyahDAO
    abstract fun surahDAO(): SurahDAO
    abstract fun bookmarkDao(): BookmarkDAO
    abstract fun readLogDAO(): ReadLogDAO
    abstract fun recordDAO(): RecordDAO
}
