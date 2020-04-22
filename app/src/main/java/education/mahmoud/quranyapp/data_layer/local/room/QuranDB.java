package education.mahmoud.quranyapp.data_layer.local.room;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {AyahItem.class, SuraItem.class,
        BookmarkItem.class, ReadLog.class, RecordItem.class},
        version = 7, exportSchema = false)
@TypeConverters({PagesConverter.class})
public abstract class QuranDB extends RoomDatabase {

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL("CREATE table readlog (" +
                    "date INTEGER PRIMARY KEY NOT NULL , " +
                    " strDate TEXT default null , " +
                    "page_num TEXT   )");

        }
    };

    // Singleton Pattern only one instance exists and available for all classes from this class
    private static QuranDB instance;

    public static synchronized QuranDB getInstance(Application application) {
        if (instance == null) { // first time to create instance
            instance = Room.databaseBuilder(application, QuranDB.class, "quran")
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract AyahDAO ayahDAO();
    public abstract SurahDAO surahDAO();
    public abstract BookmarkDAO bookmarkDao();
    public abstract ReadLogDAO readLogDAO();

    public abstract RecordDAO recordDAO();
}
