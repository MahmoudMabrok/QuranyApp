package education.mahmoud.quranyapp.feature.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.koin.java.KoinJavaComponent;

import java.util.List;

import education.mahmoud.quranyapp.datalayer.QuranRepository;
import education.mahmoud.quranyapp.datalayer.model.full_quran.Surah;
import education.mahmoud.quranyapp.utils.Util;

public class LoadDataQuranTafseer extends Service {
    private static final String TAG = "LoadDataQuranTafseer";
    private QuranRepository quranRepository = KoinJavaComponent.get(QuranRepository.class);

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
        stopSelf();
    }

}
