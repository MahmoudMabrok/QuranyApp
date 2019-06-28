package education.mahmoud.quranyapp.feature.splash;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.github.paolorotolo.appintro.model.SliderPage;

import androidx.fragment.app.Fragment;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.feature.home_Activity.HomeActivity;
import education.mahmoud.quranyapp.feature.services.LoadDataQuranTafseer;


public class Splash extends AppIntro2 {

    private static final String TAG = "Splash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSlides();
        askPermissionForApp();
        setFadeAnimation();
        showSkipButton(false);
        showStatusBar(false);
        startServices();

    }

    private void startServices() {
        Log.d(TAG, "startServices: ");
        Intent intent = new Intent(this, LoadDataQuranTafseer.class);
        startService(intent);
    }

    private void askPermissionForApp() {
        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO}, 5);
    }

    private void initSlides() {
        SliderPage sliderPage = new SliderPage();
        sliderPage.setTitle("Home");
        sliderPage.setDescription("HollyQuranApp");
        sliderPage.setImageDrawable(R.mipmap.list);
        sliderPage.setBgColor(R.color.bg_green);
        addSlide(AppIntro2Fragment.newInstance(sliderPage));

        sliderPage = new SliderPage();
        sliderPage.setTitle("Quran Read");
        sliderPage.setDescription("HollyQuranApp");
        sliderPage.setImageDrawable(R.mipmap.ic_launcher);
        sliderPage.setBgColor(R.color.bg_green);
        addSlide(AppIntro2Fragment.newInstance(sliderPage));

        sliderPage = new SliderPage();
        sliderPage.setTitle("Quran tafseer");
        sliderPage.setDescription("HollyQuranApp");
        sliderPage.setImageDrawable(R.mipmap.tafseer);
        sliderPage.setBgColor(R.color.bg_green);
        addSlide(AppIntro2Fragment.newInstance(sliderPage));

        sliderPage = new SliderPage();
        sliderPage.setTitle("Quran Search");
        sliderPage.setDescription("HollyQuranApp");
        sliderPage.setImageDrawable(R.mipmap.ic_launcher);
        sliderPage.setBgColor(R.color.bg_green);
        addSlide(AppIntro2Fragment.newInstance(sliderPage));

        sliderPage = new SliderPage();
        sliderPage.setTitle("Quran Test as text");
        sliderPage.setDescription("HollyQuranApp");
        sliderPage.setImageDrawable(R.mipmap.jump);
        sliderPage.setBgColor(R.color.bg_green);
        addSlide(AppIntro2Fragment.newInstance(sliderPage));

        sliderPage = new SliderPage();
        sliderPage.setTitle("Read Log");
        sliderPage.setDescription("HollyQuranApp");
        sliderPage.setImageDrawable(R.mipmap.ic_launcher);
        sliderPage.setBgColor(R.color.bg_green);
        addSlide(AppIntro2Fragment.newInstance(sliderPage));

        sliderPage = new SliderPage();
        sliderPage.setTitle("Setting");
        sliderPage.setDescription("HollyQuranApp");
        sliderPage.setImageDrawable(R.mipmap.ic_launcher);
        sliderPage.setBgColor(R.color.bg_green);
        addSlide(AppIntro2Fragment.newInstance(sliderPage));

    }


    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        open();
    }

    public void open() {
        Intent openAcivity = new Intent(Splash.this, HomeActivity.class);
        openAcivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(openAcivity);
        finish();
    }

}
