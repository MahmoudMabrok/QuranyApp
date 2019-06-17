package education.mahmoud.quranyapp.feature.home_Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tjeannin.apprate.AppRate;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.App;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem;
import education.mahmoud.quranyapp.data_layer.model.full_quran.Ayah;
import education.mahmoud.quranyapp.data_layer.model.full_quran.Surah;
import education.mahmoud.quranyapp.data_layer.model.tafseer.CompleteTafseer;
import education.mahmoud.quranyapp.feature.ayahs_search.ShowSearchResults;
import education.mahmoud.quranyapp.feature.bookmark_fragment.BookmarkFragment;
import education.mahmoud.quranyapp.feature.download.DownloadActivity;
import education.mahmoud.quranyapp.feature.feedback_activity.FeedbackActivity;
import education.mahmoud.quranyapp.feature.listening_activity.ListenFragment;
import education.mahmoud.quranyapp.feature.read_log.ReadLogFragment;
import education.mahmoud.quranyapp.feature.scores.ScoreActivity;
import education.mahmoud.quranyapp.feature.setting.SettingActivity;
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowAyahsActivity;
import education.mahmoud.quranyapp.feature.show_sura_list.GoToSurah;
import education.mahmoud.quranyapp.feature.show_sura_list.SuraListFragment;
import education.mahmoud.quranyapp.feature.show_tafseer.TafseerDetails;
import education.mahmoud.quranyapp.feature.test_quran.TestFragment;
import education.mahmoud.quranyapp.feature.test_quran.TestQuranSound;
import education.mahmoud.quranyapp.model.Quran;
import education.mahmoud.quranyapp.model.Sura;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class HomeActivity extends AppCompatActivity {

    private static final int RC_STORAGE = 1001;
    private static final String TAG = "HomeActivity";


    @BindView(R.id.homeContainer)
    FrameLayout homeContainer;
    @BindView(R.id.navigation)
    BottomNavigationView navigation;

    Repository repository;


    Handler handler;
    Dialog loadingDialog;


    int ahays;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_read:
                    openRead();
                    return true;

                case R.id.navigation_tafseer:
                    openTafseer();
                    return true;

                case R.id.navigationListen:
                    openListen();
                    return true;

                case R.id.navigation_test:
                    openTest();
                    return true;

                case R.id.navigation_bookmarks:
                    openBookmark();
                    return true;



            }
            return false;
        }
    };
    private boolean isPermissionAllowed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        ButterKnife.bind(this);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        repository = ((App) getApplication()).getRepository();

        new AppRate(this).setMinLaunchesUntilPrompt(5)
                .init();

       Stetho.initializeWithDefaults(getApplication());

        ahays = repository.getTotlaAyahs();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                closeDialoge();

            }
        };

        openRead();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // used to update UI
        int id = navigation.getSelectedItemId();
        navigation.setSelectedItemId(id);
    }

    private void closeDialoge() {
        loadingDialog.dismiss();
    }

    private void startProgress() {
        loadingDialog = Util.getLoadingDialog(this, "");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }

    /**
     * load quran and tafseer from json into database
     */
    public void loadData() {
        if (ahays < 6200) {
            startProgress();
            loadQuran();
            handler.sendEmptyMessage(0);
        }
    }

    /**
     * load quran from json into database
     */
    public void loadQuran() {
        Log.d(TAG, "loadQuran: ");
        List<Surah> surahs = Util.getFullQuranSurahs(this);
        StoreInDb(surahs);
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }


    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateAyahsWithTafseer() {
        AyahItem ayahItem = null ; 
        CompleteTafseer completeTafseer = Util.getCompleteTafseer(this);
        if (completeTafseer!= null){
            List<education.mahmoud.quranyapp.data_layer.model.tafseer.Surah> surahs = completeTafseer.getData().getSurahs();
            for(education.mahmoud.quranyapp.data_layer.model.tafseer.Surah surah1 : surahs){
                for (education.mahmoud.quranyapp.data_layer.model.tafseer.Ayah ayah:surah1.getAyahs()){
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

    private void openRead() {
        SuraListFragment fragment = new SuraListFragment();
        FragmentTransaction a = getSupportFragmentManager().beginTransaction();
        a.replace(homeContainer.getId(), fragment).commit();
    }

    private void openTafseer() {
        TafseerDetails fragment = new TafseerDetails();
        FragmentTransaction a = getSupportFragmentManager().beginTransaction();
        a.replace(homeContainer.getId(), fragment).commit();
    }

    private void openListen() {
        ListenFragment fragment = new ListenFragment();
        FragmentTransaction a = getSupportFragmentManager().beginTransaction();
        a.replace(homeContainer.getId(), fragment).commit();
    }

    private void openTest() {
      //  TestFragment fragment = new TestFragment();
        TestQuranSound fragment = new TestQuranSound();
        FragmentTransaction a = getSupportFragmentManager().beginTransaction();
        a.replace(homeContainer.getId(), fragment).commit();
    }

    private void openBookmark() {
        BookmarkFragment fragment = new BookmarkFragment();
        FragmentTransaction a = getSupportFragmentManager().beginTransaction();
        a.replace(homeContainer.getId(), fragment).commit();
    }


    public void acquirePermission() {
        String[] perms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        EasyPermissions.requestPermissions(new PermissionRequest.Builder(this, RC_STORAGE, perms).build());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_STORAGE && grantResults[0] == PERMISSION_GRANTED) {
            isPermissionAllowed = true;
            repository.setPermissionState(true);
        } else if (requestCode == RC_STORAGE ) {
            showMessage(getString(R.string.down_permission));
            repository.setPermissionState(false);
            openListen();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionJump:
                openGoToSura();
                break;
            case R.id.actionSearch:
                openSearch();
                break;
            case R.id.actionSetting:
                openSetting();
                break;
            case R.id.actionGoToLastRead:
                gotoLastRead();
                break;

            case R.id.actionReadLog:
                goToReadLog();
                break;

            case R.id.actionScore:
                gotoScore();
                break;

            case R.id.actionDownload:
                gotoDownload();
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    private void goToReadLog() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        ReadLogFragment logFragment = new ReadLogFragment();
        transaction.replace(homeContainer.getId(),logFragment).commit();
    }

    private void openSearch() {
        Intent openAcivity = new Intent(this, ShowSearchResults.class);
        startActivity(openAcivity);
    }

    private void gotoLastRead() {
        int index = repository.getLatestRead();
        if (index == -1) {
            Toast.makeText(this, "You Have no saved recitation", Toast.LENGTH_SHORT).show();
            return;
        }
        gotoSuraa(index);
    }

    private void gotoSuraa(int index) {
        Intent openAcivity = new Intent(this, ShowAyahsActivity.class);
        openAcivity.putExtra(Constants.LAST_INDEX, index);
        startActivity(openAcivity);
    }

    private void openGoToSura() {
        FragmentTransaction a = getSupportFragmentManager().beginTransaction();
        GoToSurah goToSurah = new GoToSurah();
        goToSurah.show(a, null);
    }

    private void openSetting() {
        Intent openAcivity = new Intent(this, SettingActivity.class);
        startActivity(openAcivity);
    }

    private void gotoFeedback() {
        Intent openAcivity = new Intent(this, FeedbackActivity.class);
        startActivity(openAcivity);
    }

    private void gotoScore() {
        Intent openAcivity = new Intent(this, ScoreActivity.class);
        startActivity(openAcivity);
    }

    private void gotoDownload() {
        Intent openAcivity = new Intent(this, DownloadActivity.class);
        startActivity(openAcivity);
    }


}
