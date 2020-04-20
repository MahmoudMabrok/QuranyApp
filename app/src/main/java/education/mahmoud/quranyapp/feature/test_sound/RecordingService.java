package education.mahmoud.quranyapp.feature.test_sound;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import education.mahmoud.quranyapp.utils.Constants;
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
        recordItem = intent.getExtras().getParcelable(Constants.RECORD_ITEM);
        startRecording();
        // START_STICKY to run service independent and it finish when its work
        return START_STICKY;
    }

    private void startRecording() {
        Log.d(TAG, "startRecording: ");
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        mRecorder.setOutputFile(recordItem.getFilePath()); // path

        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(192000);


        try {
            mRecorder.prepare();
            mRecorder.start();
            startedMillis = System.currentTimeMillis();
            Log.d(TAG, "startRecording: startedMillis " + startedMillis);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "startRecording: error not carry prepare");
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (mRecorder != null) {
            try {
                stopRecording();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

        Toast.makeText(this, "Saved to " + recordItem.getFilePath(), Toast.LENGTH_LONG).show();
    }

    private void saveToDB() {
        recordItem.setDuration((int) mElapsedSecond);
        repository.addRecord(recordItem);
        Log.d(TAG, "saveToDB: ");
    }
}
