package education.mahmoud.quranyapp.data_layer.local.room;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

@Database(entities = {AyahItem.class, SuraItem.class, BookmarkItem.class}, version = 5, exportSchema = false)
public abstract class QuranDB extends RoomDatabase {

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE ayahs "
                    + " ADD COLUMN pageNum INTEGER not null default 1 ");

            database.execSQL("ALTER TABLE ayahs "
                    + " ADD COLUMN juz INTEGER not null default 1 ");

            database.execSQL("ALTER TABLE ayahs "
                    + " ADD COLUMN tafseer TEXT");

            database.execSQL("CREATE table bookmark (" +
                    "id INTEGER PRIMARY KEY NOT NULL , scrollIndex INTEGER NOT NULL default 0 ,timemills  " +
                    "INTEGER NOT NULL default 0 , suraName TEXT default null , pageNum INTEGER NOT NULL  default 0 )");

        }
    };
    static final Migration MIGRATION_1_3 = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE ayahs "
                    + " ADD COLUMN pageNum INTEGER not null default 1 ");

            database.execSQL("ALTER TABLE ayahs "
                    + " ADD COLUMN juz INTEGER not null default 1 ");

            database.execSQL("ALTER TABLE ayahs "
                    + " ADD COLUMN tafseer TEXT");

            database.execSQL("CREATE table bookmark (" +
                    "id INTEGER PRIMARY KEY NOT NULL , scrollIndex INTEGER NOT NULL default 0 ,timemills  " +
                    "INTEGER NOT NULL default 0 , suraName TEXT default null , pageNum INTEGER NOT NULL  default 0 )");

        }
    };
    // Singleton Pattern only one instance exists and available for all classes from this class
    private static QuranDB instance;

    public static synchronized QuranDB getInstance(Application application) {
        if (instance == null) { // first time to create instance
            instance = Room.databaseBuilder(application, QuranDB.class, "quran")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract AyahDAO ayahDAO();

    public abstract SurahDAO surahDAO();

    public abstract BookmarkDAO bookmarkDao();
}
