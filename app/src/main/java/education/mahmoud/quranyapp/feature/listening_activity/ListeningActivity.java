package education.mahmoud.quranyapp.feature.listening_activity;

import android.Manifest;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.facebook.stetho.Stetho;
import com.github.ybq.android.spinkit.SpinKitView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.SuraItem;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.view.View.GONE;

public class ListeningActivity extends AppCompatActivity implements OnDownloadListener {

    private static final int RC_STORAGE = 1001;

    @BindView(R.id.btnPlayPause)
    Button btnPlayPause;
    @BindView(R.id.sbPosition)
    SeekBar sbPosition;
    @BindView(R.id.tvProgressAudio)
    TextView tvProgressAudio;

    MediaPlayer mediaPlayer;
    private static final String TAG = "ListeningActivity";
    @BindView(R.id.spStartSura)
    Spinner spStartSura;
    @BindView(R.id.edStartSuraAyah)
    TextInputEditText edStartSuraAyah;
    @BindView(R.id.spEndSura)
    Spinner spEndSura;
    @BindView(R.id.edEndSuraAyah)
    TextInputEditText edEndSuraAyah;
    @BindView(R.id.btnStartListening)
    Button btnStartListening;
    @BindView(R.id.lnSelectorAyahs)
    LinearLayout lnSelectorAyahs;
    @BindView(R.id.lnPlayView)
    LinearLayout lnPlayView;
    @BindView(R.id.tvAyahToListen)
    TextView tvAyahToListen;
    @BindView(R.id.spinListening)
    SpinKitView spinListening;
    String url = "http://cdn.alquran.cloud/media/audio/ayah/ar.alafasy/";
    boolean isPermissionAllowed;
    int downloadID;
    int i = 1;
    Typeface typeface;
    SuraItem startSura, endSura;
    int startDown, endDown;
    String downURL, path, filename;
    int index;
    int currentAyaAtAyasToListen = 0;

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

    String fileSource;
    List<AyahItem> ayahsToListen;
    int actualStart, actualEnd;
    int currentIteration = 0, endIteration;
    private Repository repository;
    private ArrayList<AyahItem> ayahsToDownLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening);
        ButterKnife.bind(this);

        repository = Repository.getInstance(getApplication());

        typeface = Typeface.createFromAsset(getAssets(), "kfgqpc_naskh.ttf");

        isPermissionAllowed = repository.getPermissionState();
        if (!isPermissionAllowed) {
            acquirePermission();
        }
        // Stetho for debuging app with browser
        Stetho.initializeWithDefaults(this);

        initSpinners();
    }

    private void initSpinners() {
        List<String> suraNames = repository.getSurasNames();

        ArrayAdapter<String> startAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suraNames);
        spStartSura.setAdapter(startAdapter);

        ArrayAdapter<String> endAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suraNames);
        spEndSura.setAdapter(endAdapter);

        spStartSura.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sura = (String) spStartSura.getSelectedItem();
                startSura = repository.getSuraByName(sura);
                Log.d(TAG, "onItemSelected: " + sura);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spEndSura.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sura = (String) spEndSura.getSelectedItem();
                endSura = repository.getSuraByName(sura);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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

    private void downloadAllQuran(int start, int end) {
        startDown = start;
        endDown = end;
        downloadAudio();
    }

    private void downloadAudio() {
        // compute index
        index = ayahsToDownLoad.get(currentIteration).getAyahIndex();
        // form  URL
        downURL = url + index;
        // form path
        path = Util.getDirectoryPath(); // get folder path
        // form file name
        filename = index + ".mp3";

        Log.d(TAG, "downloadAudio:  file name " + filename);
        //start downloading
        PRDownloader.download(downURL, path, filename).build().start(this);

    }

    @Override
    public void onDownloadComplete() {
        Log.d(TAG, "onDownloadComplete: ");

        // store storage path in db to use in media player
        AyahItem ayahItem = repository.getAyahByIndex(index); // first get ayah to edit it with storage path
        String storagePath = path + "/" + filename;
        ayahItem.setAudioPath(storagePath); // set path
        repository.updateAyahItem(ayahItem);

        // update currentIteration to indicate complete of download
        currentIteration++;

        Log.d(TAG, "onDownloadComplete:  end " + endIteration);
        Log.d(TAG, "onDownloadComplete:  current " + currentIteration);

        if (currentIteration < endIteration) {
            // still files to download
            downloadAudio();
        } else {
            // here i finish download all ayas
            // start to display
            finishDownloadState();
            displayAyasState();
        }
    }

    private void finishDownloadState() {
        showMessage("Finished Downloading... ");
        btnStartListening.setVisibility(View.VISIBLE);
        spinListening.setVisibility(GONE);
    }

    private void displayAyasState() {
        closeKeyboard();
        Log.d(TAG, "displayAyasState: ");
        currentAyaAtAyasToListen = 0;
        // first re-load ayahs from db
        ayahsToListen = repository.getAyahSInRange(actualStart, actualEnd);
        Log.d(TAG, "displayAyasState: " + ayahsToListen.size());
        logAyahs();
        // control visibility
        lnSelectorAyahs.setVisibility(GONE);
        lnPlayView.setVisibility(View.VISIBLE);

        btnPlayPause.setBackgroundResource(R.drawable.ic_pause);

        displayAyahs();

    }

    private void logAyahs() {
        for (AyahItem ayahItem : ayahsToListen) {
            Log.d(TAG, "logAyahs: " + ayahItem.getAyahIndex());
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void displayAyahs() {
        Log.d(TAG, "displayAyahs: " + currentAyaAtAyasToListen);
        AyahItem ayahItem = ayahsToListen.get(currentAyaAtAyasToListen);
        tvAyahToListen.setTypeface(typeface);
        tvAyahToListen.setText(ayahItem.getText() + "(" + ayahItem.getAyahInSurahIndex() + ")");
        playAudio();
    }

    @OnClick(R.id.btnPlayPause)
    public void onBtnPlayPauseClicked() {
        Log.d(TAG, "onBtnPlayPauseClicked: ");
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                Log.d(TAG, "onBtnPlayPauseClicked: ");
                //     btnPlayPause.setBackground(getDrawable(R.drawable.ic_pause));
                btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
            } else {
                //    btnPlayPause.setBackground(getDrawable(R.drawable.ic_play));
                btnPlayPause.setBackgroundResource(R.drawable.ic_play);
                mediaPlayer.pause();
            }
        }

    }

    private void playAudio() {
        Log.d(TAG, "playAudio: ");
        btnPlayPause.setEnabled(false);
        try {
            mediaPlayer = new MediaPlayer();
            fileSource = ayahsToListen.get(currentAyaAtAyasToListen).getAudioPath();
            mediaPlayer.setDataSource(fileSource);
            mediaPlayer.prepare();
            mediaPlayer.start();

            sbPosition.setMax(mediaPlayer.getDuration());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mediaPlayer != null) {
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mediaPlayer != null) {
                                        tvProgressAudio.setText(getString(R.string.time_progress, mediaPlayer.getCurrentPosition() / 1000
                                                , mediaPlayer.getDuration() / 1000));
                                        sbPosition.setProgress(mediaPlayer.getCurrentPosition());
                                    }
                                }
                            });
                            Thread.sleep(750);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

            sbPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    if (b) {
                        if (mediaPlayer != null) {
                            mediaPlayer.seekTo(progress);
                            sbPosition.setProgress(progress);

                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer1) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    btnPlayPause.setEnabled(true);
                    currentAyaAtAyasToListen++;

                    Log.d(TAG, "onCompletion: ");
                    if (currentAyaAtAyasToListen < ayahsToListen.size()) {
                        Log.d(TAG, "@@  onCompletion: " + ayahsToListen.size());
                        displayAyahs();
                    } else {
                        backToSelectionState();
                    }

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("error");
        }

    }

    @Override
    public void onError(Error error) {
        showMessage(getString(R.string.error_net));
    }

    @OnClick(R.id.btnStartListening)
    public void onViewClicked() {
        ayahsToDownLoad = new ArrayList<>();
        ayahsToListen = new ArrayList<>();

        if (startSura != null && endSura != null) {
            try {
                int start = Integer.parseInt(edStartSuraAyah.getText().toString());
                if (start > startSura.getNumOfAyahs()) {
                    edStartSuraAyah.setError(getString(R.string.outofrange, startSura.getNumOfAyahs()));
                    return;
                }
                int end = Integer.parseInt(edEndSuraAyah.getText().toString());
                if (end > endSura.getNumOfAyahs()) {
                    edEndSuraAyah.setError(getString(R.string.outofrange, endSura.getNumOfAyahs()));
                    return;
                }
                // compute actual start
                actualStart = startSura.getStartIndex() + start - 1;
                // compute actual end
                actualEnd = endSura.getStartIndex() + end - 1;

                // check actualstart & actualEnd
                if (actualEnd < actualStart) {
                    makeRangeError();
                    return;
                }
                Log.d(TAG, "onViewClicked: actual " + actualStart + " " + actualEnd);

                // get ayas from db
                ayahsToListen = repository.getAyahSInRange(actualStart, actualEnd);
                Log.d(TAG, "onViewClicked: start log after firest select ");
                logAyahs();
                // traverse ayahs to check if it downloaded or not
                for (AyahItem ayahItem : ayahsToListen) {
                    if (ayahItem.getAudioPath() == null) {
                        ayahsToDownLoad.add(ayahItem);
                    }
                }

                // close keyboard
                closeKeyboard();

                checkAyahsToDownloadIt();

            } catch (NumberFormatException e) {
                makeRangeError();
            }

        } else {
            showMessage("Select from suras");
        }
    }

    private void closeKeyboard() {
        Util.hideInputKeyboard(this);
        edEndSuraAyah.clearFocus();
        edStartSuraAyah.clearFocus();
    }

    private void makeRangeError() {
        edStartSuraAyah.setError("Start must be before end ");
        edEndSuraAyah.setError("End must be after start");
    }

    private void checkAyahsToDownloadIt() {
        Log.d(TAG, "checkAyahsToDownloadIt: " + ayahsToDownLoad.size());
        currentIteration = 0;
        if (ayahsToDownLoad != null && ayahsToDownLoad.size() > 0) {
            endIteration = ayahsToDownLoad.size();
            downloadAyahs();
        } else {
            displayAyasState();
        }
    }

    private void downloadAyahs() {
        Log.d(TAG, "downloadAyahs: ");
        if (Util.isNetworkConnected(this)) {
            downloadState();
            downloadAudio();
        } else {
            showMessage(getString(R.string.error_net));
        }
    }

    private void downloadState() {
        showMessage("Downloading .... ");
        btnStartListening.setVisibility(GONE);
        spinListening.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (ayahsToListen != null && ayahsToListen.size() > 0) {
            backToSelectionState();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    private void backToSelectionState() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        lnPlayView.setVisibility(GONE);
        lnSelectorAyahs.setVisibility(View.VISIBLE);

    }
}
