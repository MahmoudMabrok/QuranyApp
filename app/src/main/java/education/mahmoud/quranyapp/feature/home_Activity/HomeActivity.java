package education.mahmoud.quranyapp.feature.home_Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tjeannin.apprate.AppRate;

import org.koin.java.KoinJavaComponent;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.App;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem;
import education.mahmoud.quranyapp.data_layer.model.full_quran.Ayah;
import education.mahmoud.quranyapp.data_layer.model.full_quran.Surah;
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
import education.mahmoud.quranyapp.feature.splash.Splash;
import education.mahmoud.quranyapp.feature.test_quran.TestFragment;
import education.mahmoud.quranyapp.model.Quran;
import education.mahmoud.quranyapp.model.Sura;
import education.mahmoud.quranyapp.utils.Constants;
import education.mahmoud.quranyapp.utils.Util;
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

    private Repository repository = KoinJavaComponent.get(Repository.class);
    private boolean isPermissionAllowed;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        ButterKnife.bind(this);
        Log.d(TAG, "onCreate: start app");
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        repository = ((App) getApplication()).getRepository();

        new AppRate(this).setMinLaunchesUntilPrompt(5)
                .init();

        //    Stetho.initializeWithDefaults(getApplication());
        Log.d(TAG, "onCreate: n " + ahays);
        ahays = repository.getTotlaAyahs();
        Log.d(TAG, "onCreate: nn after  " + ahays);
        openRead();
        checkLastReadAndDisplayDialoge();
        determineToOpenOrNotSplash();

    }

    private void determineToOpenOrNotSplash() {
        Log.d(TAG, "determineToOpenOrNotSplash:  n " + ahays);
        if (ahays == 0) {
            Log.d(TAG, "determineToOpenOrNotSplash: ok  " + ahays);
            goToSplash();
        }
    }

    private void checkLastReadAndDisplayDialoge() {
        int last = repository.getLatestRead();
        Log.d(TAG, "checkLastReadAndDisplayDialoge: " + last);
        if (last >= 0) {
            displayDialoge(last);
            Log.d(TAG, "checkLastReadAndDisplayDialoge: @@ ");
        }
    }

    private void displayDialoge(int last) {
        Log.d(TAG, "displayDialoge: ");
        Dialog dialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.last_read_dialoge, null);
        Button button = view.findViewById(R.id.btnOpenPage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPage(last);
                dialog.dismiss();
            }
        });

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private void openPage(int last) {
        gotoSuraa(last);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // used to update UI
        int id = navigation.getSelectedItemId();
        navigation.setSelectedItemId(id);

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
        TestFragment fragment = new TestFragment();
        //  TestQuranSound fragment = new TestQuranSound();
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

    private void goToSplash() {
        Log.d(TAG, "goToSplash:");
        Intent openAcivity = new Intent(HomeActivity.this, Splash.class);
        startActivity(openAcivity);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_STORAGE && grantResults[0] == PERMISSION_GRANTED) {
            isPermissionAllowed = true;
            repository.setPermissionState(true);
        } else if (requestCode == RC_STORAGE) {
            showMessage(getString(R.string.down_permission));
            repository.setPermissionState(false);
            openListen();
        }
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

    private void goToReadLog() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        ReadLogFragment logFragment = new ReadLogFragment();
        transaction.replace(homeContainer.getId(), logFragment).commit();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // // TODO: 6/27/2019 message of exiting - n pages - . m

    }
}
