package education.mahmoud.quranyapp.feature.download;

import android.Manifest;
import android.os.Bundle;
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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class Download extends AppCompatActivity implements OnDownloadListener {

    private static final String TAG = "Download";
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
    String url = "http://cdn.alquran.cloud/media/audio/ayah/ar.alafasy/";
    private int tafseerToDownload = 1;
    private boolean isPermissionAllowed;
    private String downURL;
    private String path;
    private String filename;
    private int audioToDownload = 0;
    private int max_Tafseer = 114;
    private int max_Audio = 6236;
    private List<Integer> audioIndexesToDownload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);
        repository = Repository.getInstance(getApplication());
        isPermissionAllowed = repository.getPermissionState();

        if (!isPermissionAllowed) {
            acquirePermission();
        }
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


    @OnClick(R.id.btnDownloadTafseer)
    public void onViewClicked() {
        tafseerToDownload = repository.getLastDownloadedChapter();
        Log.d(TAG, "onCreate: " + tafseerToDownload);
        if (tafseerToDownload == 0) {
            ++tafseerToDownload;
        } else if (tafseerToDownload > 1) {
            // to make sure if download non-complete previous download
            tafseerToDownload -= 1;
        }
        downState();
        loadTafseer();
    }

    private void downState() {
        lnDown.setVisibility(View.VISIBLE);
        btnDownloadTafseer.setVisibility(View.GONE);
        btnDownloadSound.setVisibility(View.GONE);
    }


    private void loadTafseer() {
        setUI(tafseerToDownload, max_Tafseer);
        if (tafseerToDownload <= 114)
            loadChapter();
    }

    private void setUI(int current, int max) {
        tvDownStatePercentage.setText(getString(R.string.downState, current, max));
    }

    private void loadChapter() {

   /*     repository.getChapterTafser(tafseerToDownload).enqueue(new Callback<Tafseer>() {
            @Override
            public void onResponse(Call<Tafseer> call, Response<Tafseer> response) {
                try {
                    Tafseer tafseer = response.body();
                    List<Ayah> ayahs = tafseer.getData().getAyahs();
                    for(Ayah ayah : ayahs){
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

*/



        /*  repository.getChapterTafser(tafseerToDownload).enqueue(new Callback<Tafseer>() {
            @Override
            public void onResponse(Call<Tafseer> call, Response<Tafseer> response) {
                try {
                    Tafseer tafseer = response.body();
                    Data data = tafseer.getData();
                    // get ayah from db to update it by supply tafseer
                    AyahItem ayahItem = repository.getAyahByIndex(data.getNumber());
                    // set  it with  new data
                    ayahItem.setJuz(data.getJuz());
                    ayahItem.setPageNum(data.getPage());
                    ayahItem.setTafseer(data.getText());
                    // update in db
                    repository.updateAyahItem(ayahItem);
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
        });*/


    }

    private void defaultState() {
        lnDown.setVisibility(View.GONE);
        btnDownloadTafseer.setVisibility(View.VISIBLE);
        btnDownloadSound.setVisibility(View.VISIBLE);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//         Log.d(TAG, "showMessage: " + message);
    }

    @OnClick(R.id.btnDownloadSound)
    public void onDownloadAudioClicked() {
        audioToDownload = 0;
        audioIndexesToDownload = repository.getAyahNumberNotAudioDownloaded();
        downState();
        downlaodAudioData();
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
        if (error.isConnectionError()) {
            showMessage(getString(R.string.error_net));
        } else if (error.isServerError()) {
            showMessage("Error with server , plz try later ");
        } else {
            showMessage("" + error.toString());
        }
        defaultState();
    }

}
