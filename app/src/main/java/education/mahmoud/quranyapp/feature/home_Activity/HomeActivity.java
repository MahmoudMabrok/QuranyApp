package education.mahmoud.quranyapp.feature.home_Activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.tjeannin.apprate.AppRate;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.App;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.feature.ayahs_search.ShowSearchResults;
import education.mahmoud.quranyapp.feature.bookmark_fragment.BookmarkFragment;
import education.mahmoud.quranyapp.feature.download.DownloadActivity;
import education.mahmoud.quranyapp.feature.feedback_activity.FeedbackActivity;
import education.mahmoud.quranyapp.feature.listening_activity.ListenFragment;
import education.mahmoud.quranyapp.feature.scores.ScoreActivity;
import education.mahmoud.quranyapp.feature.setting.SettingActivity;
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowAyahsActivity;
import education.mahmoud.quranyapp.feature.show_sura_list.GoToSurah;
import education.mahmoud.quranyapp.feature.show_sura_list.SuraListFragment;
import education.mahmoud.quranyapp.feature.show_tafseer.TafseerDetails;
import education.mahmoud.quranyapp.feature.test_quran.TestFragment;
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
    ProgressDialog progressDialog;
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
        openRead();
        startProgress();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                closeDialoge();
            }
        };

        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {
                ahays = repository.getTotlaAyahs();
                Log.d(TAG, "onTick: " + ahays);
                if (ahays > 6000) {
                    closeDialoge();
                }
            }

            @Override
            public void onFinish() {
                closeDialoge();
            }
        }.start();

    }

    private void closeDialoge() {
        loadingDialog.dismiss();
    }

    private void startProgress() {
        loadingDialog = Util.getLoadingDialog(this, "");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
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
        } else {
            finish();
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


            case R.id.actionScore:
                gotoScore();
                break;

            case R.id.actionDownload:
                gotoDownload();
                break;


        }
        return super.onOptionsItemSelected(item);
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
