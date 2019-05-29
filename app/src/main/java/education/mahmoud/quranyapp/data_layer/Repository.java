package education.mahmoud.quranyapp.data_layer;

import android.app.Application;

import java.util.List;

import education.mahmoud.quranyapp.data_layer.local.LocalShared;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.room.BookmarkItem;
import education.mahmoud.quranyapp.data_layer.local.room.QuranDB;
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem;
import education.mahmoud.quranyapp.data_layer.remote.Remote;
import education.mahmoud.quranyapp.data_layer.remote.model.full_quran.FullQuran;
import education.mahmoud.quranyapp.data_layer.remote.model.tafseer_model.Tafseer;
import retrofit2.Call;

public class Repository {

    private static LocalShared localShared;
    private static Repository instance;
    private static QuranDB quranDB;
    private static Remote remote;

    private Repository() {
    }

    public static Repository getInstance(Application context) {
        if (instance == null) {
            instance = new Repository();
            localShared = new LocalShared(context);
            quranDB = QuranDB.getInstance(context);
            remote = new Remote();
        }
        return instance;
    }


    // shared
    public void addLatestread(int index) {
        localShared.addLatestread(index);
    }

    public void addLatestreadWithScroll(int index) {
        localShared.addLatestreadWithScroll(index);
    }

    public int getLatestRead() {
        return localShared.getLatestRead();
    }

    public int getLatestReadWithScroll() {
        return localShared.getLatestReadWithScroll();
    }

    public boolean getPermissionState() {
        return localShared.getPermissionState();
    }

    public void setPermissionState(boolean state) {
        localShared.setPermissionState(state);
    }

    public boolean getNightModeState() {
        return localShared.getNightModeState();
    }

    public void setNightModeState(boolean state) {
        localShared.setNightModeState(state);
    }

    public int getBackColorState() {
        return localShared.getBackColorState();
    }

    public void setBackColorState(int color) {
        localShared.setBackColorState(color);
    }

    public long getScore() {
        return localShared.getScore();
    }

    public void setScore(long score) {
        localShared.setScore(score);
    }


    // suarh db operation
    public void addSurah(SuraItem suraItem) {
        quranDB.surahDAO().addSurah(suraItem);
    }

    public List<String> getSurasNames() {
        return quranDB.surahDAO().getAllSurahNames();
    }

    public SuraItem getSuraByName(String name) {
        return quranDB.surahDAO().getSurahByName(name);
    }


    // ayah db operation
    public void addAyah(AyahItem item) {
        quranDB.ayahDAO().addAyah(item);
    }

    public int getTotlaAyahs() {
        return quranDB.ayahDAO().getAyahCount();
    }

    public List<AyahItem> getAyahsOfSura(int index) {
        return quranDB.ayahDAO().getAllAyahOfSurahIndex(index);
    }

    public List<AyahItem> getAyahSInRange(int start, int end) {
        return quranDB.ayahDAO().getAyahSInRange(start, end);
    }

    public List<AyahItem> getAyahByAyahText(String text) {
        return quranDB.ayahDAO().getAyahByAyahText(text);
    }

    public List<Integer> getAyahNumberNotAudioDownloaded() {
        return quranDB.ayahDAO().getAyahNumberNotAudioDownloaded();

    }

    public AyahItem getAyahByInSurahIndex(int index, int ayahIndex) {
        return quranDB.ayahDAO().getAyahByInSurahIndex(index, ayahIndex);
    }

    public AyahItem getAyahByIndex(int index) {
        return quranDB.ayahDAO().getAyahByIndex(index);
    }

    public void updateAyahItem(AyahItem item) {
        quranDB.ayahDAO().updateAyah(item);
    }

    public int getLastDownloadedChapter() {
        return quranDB.ayahDAO().getLastChapter();
    }

    public int getLastDownloadedAyahAudio() {
        return quranDB.ayahDAO().getLastDownloadedAyahAudio();
    }


    // bookmark
    public List<BookmarkItem> getBookmarks() {
        return quranDB.bookmarkDao().getBookmarks();
    }

    public void addBookmark(BookmarkItem item) {
        quranDB.bookmarkDao().addBookmark(item);
    }

    public void deleteBookmark(BookmarkItem item) {
        quranDB.bookmarkDao().delteBookmark(item);
    }


    // tafseer
    public Call<Tafseer> getChapterTafser(int id) {
        return remote.getChapterTafser(id);
    }

    // quran remote

    public Call<FullQuran> getQuran() {
        return remote.getQuran();
    }


    // remote data


    public String getCurrentUserUUID() {
        return localShared.getUserUUID();
    }


    public String getUserName() {
        return localShared.getUserName();
    }

    public void setUserName(String userName) {
        localShared.setUserName(userName);
    }

    public void setUserUUID(String uuid) {
        localShared.setUserUUID(uuid);
    }


    public List<AyahItem> getAyahsOfSura(String suraName) {
        return quranDB.ayahDAO().getAllAyahOfSurahByName(suraName);
    }

    public List<AyahItem> getAyahsByPage(int i) {
        return quranDB.ayahDAO().getAyahsByPage(i);
    }

    public List<SuraItem> getSuras() {
        return quranDB.surahDAO().getAllSurah();
    }

    public int getSuraStartpage(int index) {
        return quranDB.ayahDAO().getSuraStartpage(index);
    }

    public List<AyahItem> getAllAyahOfSurahIndex(long l) {
        return quranDB.ayahDAO().getAllAyahOfSurahIndex((int) l);
    }

    public int getTotalTafseerDownloaded() {
        return quranDB.ayahDAO().getTotalTafseerDownloaded();
    }

    public int getTotalAudioDownloaded() {
        return quranDB.ayahDAO().getTotalAudioDownloaded();
    }

    public SuraItem getSuraByIndex(long l) {
        return quranDB.surahDAO().getSurahByIndex((int) l);
    }

    public int getPageFromJuz(int pos) {
        return quranDB.ayahDAO().getPageFromJuz(pos);
    }

    public int getPageFromSurahAndAyah(int surah, int ayah) {
        return quranDB.ayahDAO().getPageFromSurahAndAyah(surah, ayah);
    }

    public List<AyahItem> getAllAyahOfSurahIndexForTafseer(long l) {
        return quranDB.ayahDAO().getAllAyahOfSurahIndexForTafseer(l);
    }


   /* public Call<String> getUsers() {
        return remote.getUsers();
    }
    public Call<String> signUp(User user) {
       return remote.signUp(user);
    }
    public Call<String> signUp(String name , String mail ,long score, int n_ayahs) {
       return remote.signUp(name, mail, score, n_ayahs);
    }

    public Call<Void> sendFeedback(String pros, String cons, String suggs) {
        return remote.sendFeedback(pros, cons, suggs);
    }

*/


}
