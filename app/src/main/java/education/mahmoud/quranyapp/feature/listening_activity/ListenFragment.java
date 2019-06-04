package education.mahmoud.quranyapp.feature.listening_activity;


import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.github.ybq.android.spinkit.SpinKitView;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem;
import education.mahmoud.quranyapp.feature.home_Activity.HomeActivity;

import static android.view.View.GONE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListenFragment extends Fragment implements OnDownloadListener {

    private static final String TAG = "ListenFragment";


    @BindView(R.id.btnPlayPause)
    Button btnPlayPause;
    @BindView(R.id.sbPosition)
    SeekBar sbPosition;
    @BindView(R.id.tvProgressAudio)
    TextView tvProgressAudio;

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
    @BindView(R.id.edRepeatAyah)
    TextInputEditText edRepeatAyah;
    @BindView(R.id.edRepeatSet)
    TextInputEditText edRepeatSet;
    @BindView(R.id.tvDownStatePercentage)
    TextView tvDownStatePercentage;
    @BindView(R.id.tvDownCurrentFile)
    TextView tvDownCurrentFile;
    @BindView(R.id.lnDownState)
    LinearLayout lnDownState;


    MediaPlayer mediaPlayer;
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
    String fileSource;
    List<AyahItem> ayahsToListen;
    int actualStart, actualEnd;
    int currentIteration = 0, endIteration;
    private Unbinder unbinder;
    private Repository repository;
    private List<AyahItem> ayahsToDownLoad;
    private int ayahsRepeatCount;
    private int ayahsSetCount;

    Handler handler ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listen, container, false);
        unbinder = ButterKnife.bind(this, view);

        repository = Repository.getInstance(getActivity().getApplication());

        typeface = Typeface.createFromAsset(getActivity().getAssets(), "me_quran.ttf");

        isPermissionAllowed = repository.getPermissionState();

        initSpinners();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (mediaPlayer != null && isVisible()) {
                    tvProgressAudio.setText(getString(R.string.time_progress, mediaPlayer.getCurrentPosition() / 1000
                            , mediaPlayer.getDuration() / 1000));
                    sbPosition.setProgress(mediaPlayer.getCurrentPosition());
                }

            }
        };

        return view;
    }

    private void initSpinners() {
        List<String> suraNames = Arrays.asList(Data.SURA_NAMES);

        ArrayAdapter<String> startAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, suraNames);
        spStartSura.setAdapter(startAdapter);

        ArrayAdapter<String> endAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, suraNames);
        spEndSura.setAdapter(endAdapter);

        spStartSura.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long index) {
                try {
                    startSura = repository.getSuraByIndex(index + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spEndSura.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long index) {
                try {
                    endSura = repository.getSuraByIndex(index + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    //<editor-fold desc="downolad">
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

        // set text on screen downloaded / todownled
        // second is show name of current file to download
        tvDownCurrentFile.setText(getString(R.string.now_down, filename));
        tvDownStatePercentage.setText(getString(R.string.downState, currentIteration, endIteration));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint: ");
        if (isVisibleToUser && lnPlayView != null) {
            initSpinners();
            backToSelectionState();
        }
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

    @Override
    public void onError(Error error) {
        if (error.isConnectionError()) {
            showMessage(getString(R.string.error_net));
        } else if (error.isServerError()) {
            showMessage("Server error");
        } else {
            showMessage("Error " + error.toString());
        }

        lnDownState.setVisibility(GONE);
        backToSelectionState();
    }

    private void finishDownloadState() {
        showMessage(getString(R.string.finish));
        btnStartListening.setVisibility(View.VISIBLE);
        lnDownState.setVisibility(GONE);
    }
    //</editor-fold>

    private void displayAyasState() {
        Log.d(TAG, "displayAyasState: ");
        currentAyaAtAyasToListen = 0;
        // first reload ayahs from db
        ayahsToListen = repository.getAyahSInRange(actualStart, actualEnd);

        // repeatation formation
        ayahsToListen = getAyahsEachOneRepreated(ayahsRepeatCount);
        ayahsToListen = getAllAyahsRepeated(ayahsSetCount);

        // control visibility
        lnSelectorAyahs.setVisibility(GONE);
        lnPlayView.setVisibility(View.VISIBLE);
        btnPlayPause.setBackgroundResource(R.drawable.ic_pause);

        displayAyahs();

    }

    private List<AyahItem> getAllAyahsRepeated(int ayahsSetCount) {
        List<AyahItem> ayahItems = new ArrayList<>();
        for (int i = 0; i < ayahsSetCount; i++) {
            ayahItems.addAll(ayahsToListen);
        }

        Log.d(TAG, "getAllAyahsRepeated: " + ayahItems.size());
        return ayahItems;
    }

    private List<AyahItem> getAyahsEachOneRepreated(int ayahsRepeatCount) {
        List<AyahItem> ayahItems = new ArrayList<>();
        for (AyahItem ayahItem : ayahsToListen) {
            for (int j = 0; j < ayahsRepeatCount; j++) {
                ayahItems.add(ayahItem);
            }
        }
        return ayahItems;
    }

    private void logAyahs() {
        for (AyahItem ayahItem : ayahsToListen) {
            Log.d(TAG, "logAyahs: " + ayahItem.getAyahIndex());
        }
    }

    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void displayAyahs() {
        Log.d(TAG, "displayAyahs: " + currentAyaAtAyasToListen);
        AyahItem ayahItem = ayahsToListen.get(currentAyaAtAyasToListen);
        tvAyahToListen.setTypeface(typeface);
        tvAyahToListen.setText(MessageFormat.format("{0} ﴿ {1} ﴾ " , ayahItem.getText() , ayahItem.getAyahInSurahIndex()));
        showMessage("size " + ayahsToListen.size());
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

    @OnClick(R.id.btnStartListening)
    public void onViewClicked() {
        ayahsToDownLoad = new ArrayList<>();
        ayahsToListen = new ArrayList<>();

        //region check inputs
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
                actualStart = repository.getAyahByInSurahIndex(startSura.getIndex(), start).getAyahIndex()-1 ;
                // compute actual end
                actualEnd = repository.getAyahByInSurahIndex(endSura.getIndex(), end).getAyahIndex()-1;

                // check actualstart & actualEnd
                if (actualEnd < actualStart) {
                    makeRangeError();
                    return;
                }
                Log.d(TAG, "onViewClicked: actual " + actualStart + " " + actualEnd);

                try {
                    ayahsSetCount = Integer.parseInt(edRepeatSet.getText().toString());
                } catch (NumberFormatException e) {
                    ayahsSetCount = 1;
                }
                try {
                    ayahsRepeatCount = Integer.parseInt(edRepeatAyah.getText().toString());
                } catch (NumberFormatException e) {
                    ayahsRepeatCount = 1;
                }

                // get ayahs from db,
                // actual end is updated with one as query return result excluded one item
                ayahsToListen = repository.getAyahSInRange(actualStart, actualEnd);
                Log.d(TAG, "onViewClicked: start log after first select "+ ayahsToListen.size());
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
            }catch (Exception e){
                e.printStackTrace();
            }

        } else {
            showMessage(getString(R.string.sura_select_error));
        }
        //endregion
    }

    private void playAudio() {
        Log.d(TAG, "playAudio:  current " + currentAyaAtAyasToListen );
        btnPlayPause.setEnabled(false);
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            mediaPlayer = new MediaPlayer();
            fileSource = ayahsToListen.get(currentAyaAtAyasToListen).getAudioPath();
            mediaPlayer.setDataSource(fileSource);
            Log.d(TAG, "playAudio: file source " + fileSource);
            try {
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();

            sbPosition.setMax(mediaPlayer.getDuration());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mediaPlayer != null) {
                        try {
                            handler.sendEmptyMessage(0);
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

                    if (currentAyaAtAyasToListen < ayahsToListen.size()) {
                        Log.d(TAG, "@@  onCompletion: " + currentAyaAtAyasToListen);
                        displayAyahs();
                    } else {
                        actualStart = -1 ;
                        actualEnd = -1 ;
                        backToSelectionState();
                    }

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            showMessage(getString(R.string.error));
            backToSelectionState();
        }

    }

    private void closeKeyboard() {
        edEndSuraAyah.clearFocus();
        edStartSuraAyah.clearFocus();
        lnPlayView.requestFocus();
    //    Util.hideInputKeyboard(getContext());
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

        if (!isPermissionAllowed) {
            ((HomeActivity) getActivity()).acquirePermission();
        }

        downloadState();
        downloadAudio();

    }

    private void downloadState() {
        showMessage(getString(R.string.downloading));
        btnStartListening.setVisibility(GONE);
        lnDownState.setVisibility(View.VISIBLE);
    }


    private void backToSelectionState() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // control visibility
        lnPlayView.setVisibility(GONE);
        lnSelectorAyahs.setVisibility(View.VISIBLE);
        btnStartListening.setVisibility(View.VISIBLE);
        lnDownState.setVisibility(GONE);

        // clear inputs
        edEndSuraAyah.setText(null);
        edStartSuraAyah.setText(null);
        edEndSuraAyah.setError(null);
        edStartSuraAyah.setError(null);
        edRepeatAyah.setText(null);
        edRepeatSet.setText(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
