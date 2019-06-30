package education.mahmoud.quranyapp.feature.listening_activity;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;

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

    public static Intent createService(Context context, String path, ArrayList<? extends Parcelable> name) {
        Intent intent = new Intent(context, ListenServie.class);
        intent.putExtra(Constants.AUDIO_ITEMS, name);
        context.startService(intent);
        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       String ayahsString = intent.
                getStringExtra(Constants.AUDIO_ITEMS);

        List<AyahItem> ayahsToListen = Util.fromStringToAyahItems(ayahsString);
        if (ayahsToListen != null){
            playAyahs(ayahsToListen);
        }
        return START_STICKY;
    }

    private void playAyahs(List<AyahItem> ayahsToListen) {
        for (AyahItem ayahItem:ayahsToListen){
            playSound(ayahItem.getAudioPath());
            createNotification(Util.getName(ayahItem));
        }
    }

    private void playSound(String path) {
        Log.d(TAG, "playSound: !! ");
        if (path != null) {
            try {
                player = new MediaPlayer();
                player.setDataSource(path);
                Log.d(TAG, "playSound: after data source");
                player.prepareAsync();

                Log.d(TAG, "playSound: after start");
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
                        mp.stop();
                        mp.release();
                        notificationManager.cancelAll();
                        stopSelf();
                        Log.d(TAG, "onCompletion: ");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void createNotification(String name) {
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
                    .setContentTitle("")
                    .setContentText(name);

            notificationManager.notify(10, builder.build());


        } else {
            NotificationCompat.Builder builder = new NotificationCompat.
                    Builder(getApplicationContext())
                    .setContentTitle("")
                    .setSmallIcon(R.drawable.ic_sound)
                    .setContentText(name)
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
