package education.mahmoud.quranyapp.data_layer.local.room;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.HashSet;
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

    @Query("select ayahIndex from ayahs where audioPath is  null ")
    public List<Integer> getAyahNumberNotAudioDownloaded();

    @Query("select count(*) from ayahs")
    public int getAyahCount();

    @Query("select max(surahIndex) from ayahs where tafseer is not null ")
    public int getLastChapter();

    @Query("select  * from ayahs where `surahIndex` = :index  and ayahInSurahIndex = :ayahIndex")
    AyahItem getAyahByInSurahIndex(int index, int ayahIndex);

    @Query("select max(ayahIndex) from ayahs where audioPath is not null ")
    int getLastDownloadedAyahAudio();

    @Query("select tafseer , textClean ,ayahIndex, surahIndex, pageNum, juz, hizbQuarter, sajda, manzil, ayahInSurahIndex, text, ay.audioPath from ayahs ay  , surah su where ay.surahIndex = su.`index` and su.name = :suraName  ")
    List<AyahItem> getAllAyahOfSurahByName(String suraName);


    @Query("select * from ayahs where pageNum = :i order by ayahIndex")
    List<AyahItem> getAyahsByPage(int i);

    @Query("select pageNum from ayahs where surahIndex = :pos order by ayahInSurahIndex limit 1 ")
    int getSuraStartpage(int pos);

    @Query("select count(*) from ayahs where tafseer is not null ")
    int getTotalTafseerDownloaded();

    @Query("select count(*) from ayahs where audioPath is not null ")
    int getTotalAudioDownloaded();

    @Query("select pageNum from ayahs where juz = :pos order by pageNum asc limit 1 ")
    int getPageFromJuz(int pos);

    @Query("select pageNum from ayahs where surahIndex = :surah and ayahInSurahIndex = :ayah order by pageNum asc limit 1 ")
    int getPageFromSurahAndAyah(int surah, int ayah);

    @Query("select  * from ayahs where `surahIndex` = :l and tafseer is not null")
    List<AyahItem> getAllAyahOfSurahIndexForTafseer(long l);

    @Query("select pageNum from ayahs group by hizbQuarter order by pageNum asc")
    List<Integer> getHizbQuaterStart();
}
