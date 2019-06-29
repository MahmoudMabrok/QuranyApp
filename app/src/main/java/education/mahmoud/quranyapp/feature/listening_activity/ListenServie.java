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

import org.greenrobot.eventbus.EventBus;

import java.text.MessageFormat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;

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

    public static Intent createService(Context context, String path, String name) {
        Intent intent = new Intent(context, ListenServie.class);
        intent.putExtra(Constants.AUDIO_PATH, path);
        intent.putExtra(Constants.AUDIO_NAME, name);
        context.startService(intent);
        return intent;
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: start id " + startId + " flags " + flags);
        String name = intent.getStringExtra(Constants.AUDIO_NAME);
        String path = intent.getStringExtra(Constants.AUDIO_PATH);
        Log.d(TAG, "onStartCommand: data " + MessageFormat.format("name:{0} ", name));
        createNotification(name);
        playSound(path);
        return START_STICKY;
    }

    private void playSound(String path) {
        if (path != null) {
            try {
                player = new MediaPlayer();
                player.setDataSource(path);
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
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
                    .setContentTitle("u r listing to ")
                    .setContentText(name)
                    .setOngoing(true);

            notificationManager.notify(10, builder.build());


        } else {
            NotificationCompat.Builder builder = new NotificationCompat.
                    Builder(getApplicationContext())
                    .setContentTitle("u r listing to")
                    .setContentText(name)
                    .setAutoCancel(true)
                    .setOngoing(true);
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
        EventBus.getDefault().post(new StopeedMessage());

    }


}
