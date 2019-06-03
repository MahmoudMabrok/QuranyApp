package education.mahmoud.quranyapp.feature.download;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.github.ybq.android.spinkit.SpinKitView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem;
import education.mahmoud.quranyapp.data_layer.model.full_quran.Ayah;
import education.mahmoud.quranyapp.data_layer.model.full_quran.FullQuran;
import education.mahmoud.quranyapp.data_layer.model.full_quran.Surah;
import education.mahmoud.quranyapp.data_layer.model.tafseer.CompleteTafseer;
import education.mahmoud.quranyapp.data_layer.model.tafseer_model.Tafseer;
import education.mahmoud.quranyapp.model.Quran;
import education.mahmoud.quranyapp.model.Sura;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Download Quran, Tafseer, Sound
 */
public class DownloadActivity extends AppCompatActivity implements OnDownloadListener {

    private static final String TAG = "DownloadActivity";
    private static final int RC_STORAGE = 1001;

    @BindView(R.id.btnDownloadTafseer)
    Button btnDownloadTafseer;
    @BindView(R.id.spinListening)
    SpinKitView spinListening;
    @BindView(R.id.tvDownStatePercentage)
    TextView tvDownStatePercentage;
    @BindView(R.id.lnDown)
    LinearLayout lnDown;
    @BindView(R.id.btnDownloadSound)
    Button btnDownloadSound;

    Repository repository;
    @BindView(R.id.btnDownloadQuran)
    Button btnDownloadQuran;
    @BindView(R.id.tvTotalQuranAyahs)
    TextView tvTotalQuranAyahs;
    @BindView(R.id.tvTotalQuranTafseer)
    TextView tvTotalQuranTafseer;
    @BindView(R.id.tvTotalQuranAudio)
    TextView tvTotalQuranAudio;


    String url = "http://cdn.alquran.cloud/media/audio/ayah/ar.alafasy/";
    Handler handler;
    String message = "";
    private int tafseerToDownload = 1;
    private boolean isPermissionAllowed;
    private String downURL;
    private String path;
    private String filename;
    private int audioToDownload = 0;
    private int max_Tafseer = 114;
    private int max_Audio = 6236;
    private List<Integer> audioIndexesToDownload;
    private Dialog loadingDialog;
    private int ahays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);
        repository = Repository.getInstance(getApplication());
        isPermissionAllowed = repository.getPermissionState();
        ahays = repository.getTotlaAyahs();


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                closeDialoge();
                Toast.makeText(DownloadActivity.this, "Download success", Toast.LENGTH_SHORT).show();
                createStatistics();
            }
        };

        createStatistics();

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
            onDownloadAudioClicked();
        } else {
            showMessage(getString(R.string.down_permission));
        }
    }

    /**
     * create Statistics about dowloaded data
     * if one is complete it hide its button to be not clicked.
     */
    private void createStatistics() {
        // retrieve data
        int totalAyahsDown = repository.getTotlaAyahs();
        int totalTafseerAyahsDown = repository.getTotalTafseerDownloaded();
        int totalAudioAyahsDown = repository.getTotalAudioDownloaded();

        // set ui
        tvTotalQuranAyahs.setText(getString(R.string.totalQuranAyahs, totalAyahsDown, max_Audio));
        tvTotalQuranTafseer.setText(getString(R.string.totalQuranTafseer, totalTafseerAyahsDown, max_Audio));
        tvTotalQuranAudio.setText(getString(R.string.totalQuranAudio, totalAudioAyahsDown, max_Audio));

        // if it full hide button
        if (totalAudioAyahsDown == max_Audio) {
            btnDownloadSound.setVisibility(View.GONE);
        }
        if (totalAyahsDown == max_Audio) {
            btnDownloadQuran.setVisibility(View.GONE);
        }

        if (totalTafseerAyahsDown == max_Audio) {
            btnDownloadTafseer.setVisibility(View.GONE);
        }
    }


    @OnClick(R.id.btnDownloadTafseer)
    public void onViewClicked() {
      /*  tafseerToDownload = repository.getLastDownloadedChapter();
        Log.d(TAG, "onCreate: " + tafseerToDownload);
        if (tafseerToDownload == 0) {
            ++tafseerToDownload;
        } else if (tafseerToDownload > 1) {
            // to make sure if download non-complete previous download
            tafseerToDownload -= 1;
        }

        downState();
        loadTafseer();

        */

        loadTafseerFromJson();
    }

    private void loadTafseerFromJson() {
        Log.d(TAG, "loadTafseer: ");
        startProgress();
        new Thread(() -> {
            if (ahays > 0) {
                updateAyahsWithTafseer();
            } else {
                showMessage("First Load Ayahs");
            }
            handler.sendEmptyMessage(0);

        }).start();

    }

    private void updateAyahsWithTafseer() {
        AyahItem ayahItem = null;
        CompleteTafseer completeTafseer = Util.getCompleteTafseer(this);
        if (completeTafseer != null) {
            List<education.mahmoud.quranyapp.data_layer.model.tafseer.Surah> surahs = completeTafseer.getData().getSurahs();
            for (education.mahmoud.quranyapp.data_layer.model.tafseer.Surah surah1 : surahs) {
                for (education.mahmoud.quranyapp.data_layer.model.tafseer.Ayah ayah : surah1.getAyahs()) {
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

    //<editor-fold desc="Tafseer net">

    /**
     * load tafseer from internet
     */
    private void loadTafseer() {
        setUI(tafseerToDownload, max_Tafseer);
        showMessage(String.valueOf(tafseerToDownload));
        if (tafseerToDownload <= 114)
            loadChapter();
        defaultState();
    }

    private void downState() {
        lnDown.setVisibility(View.VISIBLE);
        btnDownloadTafseer.setVisibility(View.GONE);
        btnDownloadSound.setVisibility(View.GONE);
        btnDownloadQuran.setVisibility(View.GONE);

        tvTotalQuranTafseer.setVisibility(View.GONE);
        tvTotalQuranAyahs.setVisibility(View.GONE);
        tvTotalQuranAudio.setVisibility(View.GONE);
    }

    private void setUI(int current, int max) {
        tvDownStatePercentage.setText(getString(R.string.downState, current, max));
    }

    private void loadChapter() {
        repository.getChapterTafser(tafseerToDownload).enqueue(new Callback<Tafseer>() {
            @Override
            public void onResponse(Call<Tafseer> call, Response<Tafseer> response) {
                try {
                    Tafseer tafseer = response.body();
                    List<education.mahmoud.quranyapp.data_layer.model.tafseer_model.Ayah> ayahs = tafseer.getData().getAyahs();
                    for (education.mahmoud.quranyapp.data_layer.model.tafseer_model.Ayah ayah : ayahs) {
                        // get ayah from db to update it by supply tafseer
                        AyahItem ayahItem = repository.getAyahByIndex(ayah.getNumber());
                        // set  it with  new data
                        ayahItem.setJuz(ayah.getJuz());
                        ayahItem.setPageNum(ayah.getPage());
                        ayahItem.setTafseer(ayah.getText());
                        // update in db
                        repository.updateAyahItem(ayahItem);
                    }
                    tafseerToDownload++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                loadTafseer();
            }

            @Override
            public void onFailure(Call<Tafseer> call, Throwable t) {
                showMessage("Error , try again");
                Log.d(TAG, "onFailure: " + t.getMessage());
                defaultState();
            }
        });
    }

    private void defaultState() {
        lnDown.setVisibility(View.GONE);
        btnDownloadTafseer.setVisibility(View.VISIBLE);
        btnDownloadSound.setVisibility(View.VISIBLE);
        btnDownloadQuran.setVisibility(View.VISIBLE);

        tvTotalQuranTafseer.setVisibility(View.VISIBLE);
        tvTotalQuranAyahs.setVisibility(View.VISIBLE);
        tvTotalQuranAudio.setVisibility(View.VISIBLE);

    }
    //</editor-fold>

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void startProgress() {
        loadingDialog = Util.getLoadingDialog(this, "");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        Log.d(TAG, "startProgress: ");
    }

    private void closeDialoge() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    //<editor-fold desc="Download Audio">
    @OnClick(R.id.btnDownloadSound)
    public void onDownloadAudioClicked() {
        if (!isPermissionAllowed) {
            acquirePermission();
        } else {
            audioToDownload = 0;
            audioIndexesToDownload = repository.getAyahNumberNotAudioDownloaded();
            downState();
            downlaodAudioData();

        }
    }

    private void downlaodAudioData() {
        setUI((audioToDownload + 1), audioIndexesToDownload.size());
        if (audioToDownload < audioIndexesToDownload.size()) {
            downloadAudio();
        }
    }

    private void downloadAudio() {
        // form  URL
        downURL = url + audioIndexesToDownload.get(audioToDownload);
        // form path
        path = Util.getDirectoryPath(); // get folder path
        // form file name
        filename = audioIndexesToDownload.get(audioToDownload) + ".mp3";
        Log.d(TAG, "downloadAudio:  file name " + filename);
        //start downloading
        PRDownloader.download(downURL, path, filename).build().start(this);
    }

    @Override
    public void onDownloadComplete() {
        Log.d(TAG, "onDownloadComplete: " + audioToDownload);
        try {
            // store storage path in db to use in media player
            AyahItem ayahItem = repository.getAyahByIndex(audioIndexesToDownload.get(audioToDownload)); // first get ayah to edit it with storage path
            String storagePath = path + "/" + filename;
            ayahItem.setAudioPath(storagePath); // set path
            repository.updateAyahItem(ayahItem);
            audioToDownload++;
            downlaodAudioData();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("" + e.getMessage());
        }

    }

    @Override
    public void onError(Error error) {
        Log.d(TAG, "onError: " + error.isConnectionError());
        if (error.isConnectionError()) {
            showMessage(getString(R.string.error_net));
        } else if (error.isServerError()) {
            showMessage("Error with server , plz try later ");
        } else {
            showMessage("" + error.toString());
        }
        defaultState();
    }
    //</editor-fold>

    //<editor-fold desc="download quran">
    @OnClick(R.id.btnDownloadQuran)
    public void onDownloadQuran() {
        // downQuranState();
        Log.d(TAG, "onDownloadQuran: ");
        startProgress();
        new Thread(() -> {
            LoadQuranFromJson();
        }).start();

    }

    public void LoadQuranFromJson() {
        ahays = repository.getTotlaAyahs();
        if (ahays == 0) {
            List<Surah> surahs = Util.getFullQuranSurahs(this);
            Store(surahs);
        } else {
            closeDialoge();
        }
    }

    private void downQuranState() {
        tvDownStatePercentage.setVisibility(View.GONE);
        downState();
    }

    private void downloadQuran() {
        repository.getQuran().enqueue(new Callback<FullQuran>() {
            @Override
            public void onResponse(Call<FullQuran> call, @NotNull Response<FullQuran> response) {
                tvDownStatePercentage.setVisibility(View.VISIBLE);
                List<Surah> surahs = response.body().getData().getSurahs();
                StoreInDb(surahs);
            }

            @Override
            public void onFailure(Call<FullQuran> call, Throwable t) {
                runOnUiThread(() -> {
                    defaultState();
                    showMessage(t.getMessage());
                    Log.d(TAG, "onFailure: " + t.getMessage());
                });

            }
        });
    }

    private void StoreInDb(List<Surah> surahs) {
        new Thread(() -> {
            Store(surahs);
        }).start();
    }

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
                    setUI(ayahItem.getAyahIndex(), max_Audio);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }

        handler.sendEmptyMessage(0);

    }
    //</editor-fold>


}
