package education.mahmoud.quranyapp.feature.download;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.github.ybq.android.spinkit.SpinKitView;

import org.koin.java.KoinJavaComponent;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.datalayer.QuranRepository;
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem;
import education.mahmoud.quranyapp.utils.Util;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

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

    private QuranRepository quranRepository = KoinJavaComponent.get(QuranRepository.class);
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

        isPermissionAllowed = quranRepository.getPermissionState();
        ahays = quranRepository.getTotlaAyahs();

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
            quranRepository.setPermissionState(true);
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
        int totalAyahsDown = quranRepository.getTotlaAyahs();
        int totalTafseerAyahsDown = quranRepository.getTotalTafseerDownloaded();
        int totalAudioAyahsDown = quranRepository.getTotalAudioDownloaded();

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
            audioIndexesToDownload = quranRepository.getAyahNumberNotAudioDownloaded();
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
            AyahItem ayahItem = quranRepository.getAyahByIndex(audioIndexesToDownload.get(audioToDownload)); // first get ayah to edit it with storage path
            String storagePath = path + "/" + filename;
            ayahItem.setAudioPath(storagePath); // set path
            quranRepository.updateAyahItem(ayahItem);
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



    private void downQuranState() {
        tvDownStatePercentage.setVisibility(View.GONE);
        downState();
    }




    //</editor-fold>


}
