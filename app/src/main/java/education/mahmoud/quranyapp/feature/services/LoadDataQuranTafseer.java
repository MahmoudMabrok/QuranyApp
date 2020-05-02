package education.mahmoud.quranyapp.feature.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.koin.java.KoinJavaComponent;

import java.util.List;

import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem;
import education.mahmoud.quranyapp.data_layer.model.full_quran.Ayah;
import education.mahmoud.quranyapp.data_layer.model.full_quran.Surah;
import education.mahmoud.quranyapp.data_layer.model.tafseer.CompleteTafseer;
import education.mahmoud.quranyapp.model.Quran;
import education.mahmoud.quranyapp.model.Sura;
import education.mahmoud.quranyapp.utils.Util;

public class LoadDataQuranTafseer extends Service {
    private static final String TAG = "LoadDataQuranTafseer";
    private Repository repository = KoinJavaComponent.get(Repository.class);

    public LoadDataQuranTafseer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        new Thread(this::loadQuranTafseer).start();
        return START_STICKY;
    }

    private void loadQuranTafseer() {
        List<Surah> surahs = Util.getFullQuranSurahs(this);
        Log.d(TAG, "loadQuranTafseer: before");
        Store(surahs);
        Log.d(TAG, "loadQuranTafseer: after");
        updateAyahsWithTafseer();
        stopSelf();
    }

    //<editor-fold desc="quran">
    private void Store(List<Surah> surahs) {
        SuraItem suraItem;
        AyahItem ayahItem;
        Quran quran = Util.getQuranClean(this);
        Sura[] surahClean = quran.getSurahs();

        String clean;
        for (Surah surah : surahs) {
            suraItem = new SuraItem(surah.getNumber()
                    , surah.getAyahs().size()
                    , surah.getName(), surah.getEnglishName()
                    , surah.getEnglishNameTranslation(), surah.getRevelationType());
            // add start page
            suraItem.setIndex(surah.getNumber());
            suraItem.setStartIndex(surah.getAyahs().get(0).getPage());
            try {
                repository.addSurah(suraItem);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (Ayah ayah : surah.getAyahs()) {
                ayahItem = new AyahItem(ayah.getNumber(), surah.getNumber()
                        , ayah.getPage(), ayah.getJuz()
                        , ayah.getHizbQuarter(), false
                        , ayah.getNumberInSurah(), ayah.getText()
                        , ayah.getText());

                //    ayahItem.setTextClean(Util.removeTashkeel(ayahItem.getText()));
                if (surahClean != null) {
                    clean = surahClean[surah.getNumber() - 1].getAyahs()[ayahItem.getAyahInSurahIndex() - 1].getText();
                    ayahItem.setTextClean(clean);
                } else {
                    ayahItem.setTextClean(Util.removeTashkeel(ayahItem.getText()));
                }
                try {
                    repository.addAyah(ayahItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //</editor-fold>

    private void updateAyahsWithTafseer() {
        AyahItem ayahItem = null;
        CompleteTafseer completeTafseer = Util.getCompleteTafseer(this);
        if (completeTafseer != null) {
            List<Surah> surahs = completeTafseer.getData().getSurahs();
            for (Surah surah1 : surahs) {
                for (Ayah ayah : surah1.getAyahs()) {
                    ayahItem = repository.getAyahByIndex(ayah.getNumber());
                    ayahItem.setTafseer(ayah.getText());
                    try {
                        repository.updateAyahItem(ayahItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Log.d(TAG, "updateAyahsWithTafseer: ");
            }
        }

    }
}
