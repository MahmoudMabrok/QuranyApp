package education.mahmoud.quranyapp.feature.show_sura_list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowSuarhAyas;
import safety.com.br.android_shake_detector.core.ShakeCallback;
import safety.com.br.android_shake_detector.core.ShakeDetector;
import safety.com.br.android_shake_detector.core.ShakeOptions;

public class ShowSuar extends AppCompatActivity {

    @BindView(R.id.rvSura)
    RecyclerView rvSura;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_suar);
        ButterKnife.bind(this);

        initRv();


        ShakeOptions options = new ShakeOptions()
                .interval(1000)
                .shakeCount(2)
                .sensibility(2.0f);

        ShakeDetector shakeDetector = new ShakeDetector(options).start(this, new ShakeCallback() {
            @Override
            public void onShake() {
                int index = Repository.getInstance(ShowSuar.this).getLastSura();
                if (index == -1) {
                    Toast.makeText(ShowSuar.this, "You Have no saved recititaion", Toast.LENGTH_SHORT).show();
                    return;
                }

                gotoSuraa(index);
            }
        });

    }

    private void initRv() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        SuraAdapter suraAdapter = new SuraAdapter();
        rvSura.setLayoutManager(linearLayoutManager);
        rvSura.setAdapter(suraAdapter);
        suraAdapter.setStringList(Arrays.asList(Data.SURA_NAMES));

        suraAdapter.setSuraListner(new SuraAdapter.SuraListner() {
            @Override
            public void onSura(int pos) {
                gotoSuraa(pos);
            }
        });


    }

    public void gotoSuraa(int pos) {
        Intent openAcivity = new Intent(ShowSuar.this, ShowSuarhAyas.class);
        openAcivity.putExtra(Constants.SURAH_INDEX, pos);
        startActivity(openAcivity);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sura_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                openGoToSura();
                break;
            case R.id.actionGoToLastRead:
                gotoLastRead();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void gotoLastRead() {
        int index = Repository.getInstance(ShowSuar.this).getLastSura();
        if (index == -1) {
            Toast.makeText(ShowSuar.this, "You Have no saved recitation", Toast.LENGTH_SHORT).show();
            return;
        }
        gotoSuraa(index);
    }

    private void openGoToSura() {
        FragmentTransaction a = getSupportFragmentManager().beginTransaction();
        GoToSurah goToSurah = new GoToSurah();
        goToSurah.show(a, null);
    }
}
