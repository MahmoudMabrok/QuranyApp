package education.mahmoud.quranyapp.data_layer;

import android.app.Application;

import java.util.List;

import education.mahmoud.quranyapp.data_layer.local.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.LocalShared;
import education.mahmoud.quranyapp.data_layer.local.QuranDB;
import education.mahmoud.quranyapp.data_layer.local.SuraItem;

public class Repository {

    private static LocalShared localShared;
    private static Repository instance;
    private static QuranDB quranDB;

    private Repository() {
    }

    public static Repository getInstance(Application context) {
        if (instance == null) {
            instance = new Repository();
            localShared = new LocalShared(context);
            quranDB = QuranDB.getInstance(context);
        }
        return instance;
    }


    public void addLastSura(int index) {
        localShared.addLastSura(index);
    }
    public void addLastSuraWithScroll(int index) {
        localShared.addLastSuraWithScroll(index);
    }
    public int getLastSura() {
        return localShared.getLastSura();
    }
    public int getLastSuraWithScroll() {
        return localShared.getLastSuraWithScroll();
    }
    public boolean getPermissionState(){
        return localShared.getPermissionState();
    }
    public void setPermissionState(boolean state){
        localShared.setPermissionState(state);
    }

    // suarh db operation
    public void addSurah(SuraItem suraItem) {
        quranDB.surahDAO().addSurah(suraItem);
    }

    public List<String> getSurasNames(){
        return quranDB.surahDAO().getAllSurahNames();
    }

    public SuraItem getSuraByName(String name){
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

    public List<AyahItem> getAyahSInRange(int start , int end) {
        return quranDB.ayahDAO().getAyahSInRange(start , end);
    }

    public List<AyahItem> getAyahByAyahText(String text) {
        return quranDB.ayahDAO().getAyahByAyahText(text);
    }

    public AyahItem getAyahByIndex(int index){
        return quranDB.ayahDAO().getAyahByIndex(index);
    }

    public void updateAyahItem(AyahItem item){
        quranDB.ayahDAO().updateAyah(item);
    }



}
