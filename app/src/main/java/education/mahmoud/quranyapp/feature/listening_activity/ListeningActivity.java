package education.mahmoud.quranyapp.feature.listening_activity;

import android.Manifest;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.Status;
import com.facebook.stetho.Stetho;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.AyahItem;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ListeningActivity extends AppCompatActivity implements OnDownloadListener {

    private static final int RC_STORAGE = 1001;

    @BindView(R.id.button)
    Button button;
    @BindView(R.id.btnPlayPause)
    Button btnPlayPause;
    @BindView(R.id.sbPosition)
    SeekBar sbPosition;
    @BindView(R.id.tvProgressAudio)
    TextView tvProgressAudio;

    MediaPlayer mediaPlayer;
    private Repository repository ;
    String url = "http://cdn.alquran.cloud/media/audio/ayah/ar.alafasy/";
    boolean isPermissionAllowed;
    int downloadID;
    int i = 1;

    
    //// TODO: 4/9/2019 selector and download (check if already download not download else down load )
    //// TODO: 4/9/2019  design of display ayah then swipe after complete audi  
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening);
        ButterKnife.bind(this);

        repository = Repository.getInstance(getApplication());

        // // TODO: 4/8/2019  fix dialoge
        //  makeAlertForPermission();
        isPermissionAllowed = repository.getPermissionState();
        if (!isPermissionAllowed){
            acquirePermission();
        }

        Stetho.initializeWithDefaults(this);
    }

    private void makeAlertForPermission() {

        final AlertDialog alertPermission = new AlertDialog.Builder(this)
                .setMessage("Permission needed to write audio in external storage ")
                .setTitle("Why Permission")
                .create();

        alertPermission.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acquirePermission();
                alertPermission.dismiss();
            }
        });

        alertPermission.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertPermission.setMessage("WE can not do our work ");
                //   finish();
            }
        });

    }

    private void acquirePermission() {
        String[] perms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        EasyPermissions.requestPermissions(new PermissionRequest.Builder(this, RC_STORAGE, perms).build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_STORAGE && grantResults[0] == PERMISSION_GRANTED) {
            isPermissionAllowed = true;
            repository.setPermissionState(true);
        } else {
            finish();
        }
    }

    //// TODO: 4/8/2019 handle state of permission
    @OnClick(R.id.button)
    public void onButtonClicked() {
        downloadAllQuran(i ,25 );
    }


    int startDown , endDown ;
    String downURL , path , filename ;

    private void downloadAllQuran(int start, int end) {
        startDown = start ;
        endDown = end ;

        downloadAudio();

        /*
        for (int i = start; i <= end ; i++) {
            // form  URL
            downURL = url + start ;
            // form path
            path = Util.getDirectoryPath(); // get folder path
            // form file name
            filename = start + ".mp3" ;

        }

        */

    }

    private void downloadAudio() {
        // form  URL
        downURL = url + startDown ;
        // form path
        path = Util.getDirectoryPath(); // get folder path
        // form file name
        filename = startDown + ".mp3" ;

        //start downloading
        PRDownloader.download(downURL, path, filename).build().start(this);

    }


    @Override
    public void onDownloadComplete() {
        // display status
        Status status = PRDownloader.getStatus(downloadID);
        showMessage(status.name() + " " + startDown);

        // store storage path in db to use in media player
        AyahItem ayahItem = repository.getAyahByIndex(startDown); // first get ayah to edit it with storage path
        String storagePath = path + "/" + filename ;
        showMessage("path " + storagePath);
        ayahItem.setAudioPath(storagePath); // set path
        repository.updateAyahItem(ayahItem);

        // update startdown to indicate complete of download
        startDown++;
        if (startDown <= endDown){
            // still files to download
            downloadAudio();
        }
    }


    @OnClick(R.id.btnPlayPause)
    public void onBtnPlayPauseClicked() {
        i++;

        AyahItem ayahItem = repository.getAyahByIndex(i);
        String newUrl = ayahItem.getAudioPath();
        btnPlayPause.setEnabled(false);
        try {

            //region player init
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(newUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            //endregion

            //region track progress
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mediaPlayer != null) {
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvProgressAudio.setText(getString(R.string.time_progress, mediaPlayer.getCurrentPosition()
                                            , mediaPlayer.getDuration()));
                                }
                            });
                            Thread.sleep(750);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            //endregion

            //region complete play
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer1) {
                    mediaPlayer.release();
                    mediaPlayer = null;

                    btnPlayPause.setEnabled(true);

                    if (i <= 7) {
                        onBtnPlayPauseClicked();
                    }
                }
            });
            //endregion

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void playAudio() {
        btnPlayPause.setEnabled(false);
        try {
            mediaPlayer = new MediaPlayer();
            //   File file = new File(path);
            showMessage(path);
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();


            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mediaPlayer != null) {
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvProgressAudio.setText(getString(R.string.time_progress, mediaPlayer.getCurrentPosition()
                                            , mediaPlayer.getDuration()));
                                }
                            });
                            Thread.sleep(750);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();


            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer1) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    btnPlayPause.setEnabled(true);

/*
                    if (i <= 7) {
                        onBtnPlayPauseClicked();
                    }
*/
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("error");
        }

    }


    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Error error) {

    }
}
