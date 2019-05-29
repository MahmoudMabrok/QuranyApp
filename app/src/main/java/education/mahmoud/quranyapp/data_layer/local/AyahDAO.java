package education.mahmoud.quranyapp.data_layer.local;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface AyahDAO {

    @Insert
    public void addAyah(AyahItem item);

    @Update
    public void updateAyah(AyahItem item);

    @Query("select  * from ayahs where `surahIndex` = :id order by ayahInSurahIndex asc")
    public List<AyahItem> getAllAyahOfSurahIndex(int id);

    @Query("select  * from ayahs where `ayahIndex` between :start and :end order by ayahIndex asc ")
    public List<AyahItem> getAyahSInRange(int start, int end);

    @Query("select  * from ayahs where `ayahIndex` = :id")
    public AyahItem getAyahByIndex(int id);


    @Query("select  * from ayahs where textClean like  '%' || :txt || '%' ")
    public List<AyahItem> getAyahByAyahText(String txt);


    @Query("select count(*) from ayahs")
    public int getAyahCount();


}
