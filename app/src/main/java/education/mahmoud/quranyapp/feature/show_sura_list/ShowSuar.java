package education.mahmoud.quranyapp.feature.show_sura_list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.SuraItem;
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowSuarhAyas;
import education.mahmoud.quranyapp.model.Aya;
import education.mahmoud.quranyapp.model.Quran;
import education.mahmoud.quranyapp.model.Sura;

public class ShowSuar extends AppCompatActivity {

    @BindView(R.id.rvSura)
    RecyclerView rvSura;

    private static final String TAG = "ShowSuar";
    Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_suar);
        ButterKnife.bind(this);

        repository = Repository.getInstance(getApplication());
        initRv();
        if (repository.getTotlaAyahs() == 0)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    checkDb();
                }
            }).start();

    }

    private void checkDb() {

        Quran quran = Util.getQuran(this);
        int ayahIndex = 1, surahIndex = 1;
        for (Sura sura : quran != null ? quran.getSurahs() : new Sura[0]) {
            // here ayahIndex represent first ayahIndex in a sura
            repository.addSurah(new SuraItem(surahIndex, ayahIndex, sura.getAyahs().length, sura.getName()));
            for (Aya aya : sura.getAyahs()) {
                repository.addAyah(new AyahItem(ayahIndex, surahIndex, Integer.parseInt(aya.getNum()), aya.getText()));
                ++ayahIndex;// update suraIndex
            }
            surahIndex++; // update suraIndex
        }


        Log.d(TAG, "checkDb: " + repository.getTotlaAyahs());
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
                gotoSuraa(pos, 0);
            }
        });


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
        int index = Repository.getInstance(getApplication()).getLastSura();
        int scroll = Repository.getInstance(getApplication()).getLastSuraWithScroll();
        if (index == -1) {
            Toast.makeText(ShowSuar.this, "You Have no saved recitation", Toast.LENGTH_SHORT).show();
            return;
        }
        gotoSuraa(index, scroll);
    }

    private void gotoSuraa(int index, int scroll) {
        Intent openAcivity = new Intent(ShowSuar.this, ShowSuarhAyas.class);
        openAcivity.putExtra(Constants.SURAH_INDEX, index);
        openAcivity.putExtra(Constants.LAST_INDEX_Scroll, scroll);
        startActivity(openAcivity);
    }

    private void openGoToSura() {
        FragmentTransaction a = getSupportFragmentManager().beginTransaction();
        GoToSurah goToSurah = new GoToSurah();
        goToSurah.show(a, null);
    }
}
