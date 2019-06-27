package education.mahmoud.quranyapp.feature.splash;

import android.content.Intent;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;

import education.mahmoud.quranyapp.feature.home_Activity.HomeActivity;


public class Splash extends AppIntro2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent openAcivity = new Intent(Splash.this, HomeActivity.class);
        openAcivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(openAcivity);
        finish();
    }

}
