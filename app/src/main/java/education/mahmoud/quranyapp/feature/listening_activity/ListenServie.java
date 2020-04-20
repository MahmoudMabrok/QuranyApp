package education.mahmoud.quranyapp.feature.listening_activity;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.utils.Constants;
import education.mahmoud.quranyapp.utils.Util;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ListenServie extends Service {


    private static final String TAG = "ListenServie";
    MediaPlayer player;
    NotificationManager notificationManager;

    public static Intent createService(Context context, AyahsListen items) {
        Intent intent = new Intent(context, ListenServie.class);
        intent.putExtra(Constants.AUDIO_ITEMS, items);
        context.startService(intent);
        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return null;
    }

    List<AyahItem> ayahsToListen ;
    int currentAudio = 0 ;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AyahsListen ayahsListen = intent.getParcelableExtra(Constants.AUDIO_ITEMS);
        ayahsToListen = new ArrayList<>(ayahsListen.getAyahItemList());
        if (ayahsToListen.size() > 0 ){
            playAyah(ayahsToListen.get(currentAudio));
        }
        return START_STICKY;
    }

    private void playAyah(AyahItem ayahItem) {
        Log.d(TAG, "playSound: !! " + ayahItem.getAyahInSurahIndex());
        createNotification(Util.getName(ayahItem) , ayahItem.getText());
        String path = ayahItem.getAudioPath();
        if (path != null) {
            try {
                player = new MediaPlayer();
                player.setDataSource(path);
                player.prepareAsync();

                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                        Log.d(TAG, "onPrepared: ");
                    }
                });

                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        player.release();
                        player = null ;
                        currentAudio++;
                        try {
                            playAyah(ayahsToListen.get(currentAudio));
                        } catch (Exception e) {
                            // if exception occuured so we finish all audios
                            stopSelf();
                            notificationManager.cancelAll();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void createNotification(String name, String text) {
        // // TODO: 6/29/2019 add template
        notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // create channel
            String CHANNEL_ID = "101";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Listen",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Listen state ");
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat
                    .Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_sound)
                    .setContentTitle(name)
                    .setContentText(text);

            notificationManager.notify(10, builder.build());


        } else {
            NotificationCompat.Builder builder = new NotificationCompat.
                    Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_sound)
                    .setContentTitle(name)
                    .setContentText(text)
                    .setAutoCancel(true);

            notificationManager.notify(10, builder.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: service");
        if (player != null) {
            //   player.stop();
            player.release();
            player = null;
        }

    }


}
