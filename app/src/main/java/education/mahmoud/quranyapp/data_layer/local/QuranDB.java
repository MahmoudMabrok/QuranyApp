package education.mahmoud.quranyapp.data_layer.local;

import android.app.Application;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {AyahItem.class, SuraItem.class}, version = 2, exportSchema = false)
public abstract class QuranDB extends RoomDatabase {

    // Singleton Pattern only one istance exists and availbale for all classes from this class
    private static QuranDB instance;

    public static synchronized QuranDB getInstance(Application application) {
        if (instance == null) { // first time to create instance
            instance = Room.databaseBuilder(application, QuranDB.class, "quran")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract AyahDAO ayahDAO();

    public abstract SurahDAO surahDAO();
}
