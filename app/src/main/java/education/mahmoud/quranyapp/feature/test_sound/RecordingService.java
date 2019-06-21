package education.mahmoud.quranyapp.feature.test_sound;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.RecordItem;

public class RecordingService extends Service {
    private static final String TAG = "RecordingService";

    RecordItem recordItem;
    Repository repository;
    private MediaRecorder mRecorder;
    private long startedMillis;
    private long mElapsedSecond;

    public RecordingService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        repository = Repository.getInstance(getApplication());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        recordItem = intent.getBundleExtra("0").getParcelable(Constants.RECORD_ITEM);
        startRecording();
        // START_STICKY to run service independent and it finish when its work
        return START_STICKY;
    }

    private void startRecording() {
        Log.d(TAG, "startRecording: ");
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
        mRecorder.setAudioChannels(2);
        mRecorder.setOutputFile(recordItem.getFilePath()); // path

        try {
            mRecorder.prepare();
            mRecorder.start();
            startedMillis = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (mRecorder != null) {
            stopRecording();
        }

        super.onDestroy();
    }

    private void stopRecording() {
        Log.d(TAG, "stopRecording: ");
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        mElapsedSecond = (System.currentTimeMillis() - startedMillis) / 1000;
        saveToDB();
    }

    private void saveToDB() {
        recordItem.setDuration((int) mElapsedSecond);
        repository.addRecord(recordItem);
        Log.d(TAG, "saveToDB: ");
    }
}
