package education.mahmoud.quranyapp.feature.show_sura_list;

import android.content.Intent;
import android.graphics.Typeface;
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
import education.mahmoud.quranyapp.feature.ayahs_search.ShowSearchResults;
import education.mahmoud.quranyapp.feature.listening_activity.ListeningActivity;
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowSuarhAyas;
import education.mahmoud.quranyapp.model.Aya;
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
                    fillDBfromJson();
                }
            }).start();


    }

    private void fillDBfromJson() {
        Sura[] suralList = Util.getQuran(this).getSurahs();
        Sura[] suralListClean = Util.getQuranClean(this).getSurahs();
        int ayahIndex = 1, surahIndex = 1;
        Sura sura;
        Aya[] cleanAyahs;
        for (int i = 0; i < suralList.length; i++) {
            sura = suralList[i];
            // here ayahIndex represent first ayahIndex in a sura
            // add sura to db (index, startayahindex , n of ayahs)
            repository.addSurah(new SuraItem(surahIndex, ayahIndex, suralList[i].getAyahs().length, sura.getName()));

            // add ayahs of this sura
            // aya (ayah index -global index- , surah index , order inside sura , text , clean text -text withot symbol- )
            cleanAyahs = suralListClean[i].getAyahs(); // hold ayahs of clean sura
            for (int j = 0; j < sura.getAyahs().length; j++) {
                repository.addAyah(new AyahItem(ayahIndex, surahIndex,
                        Integer.parseInt(sura.getAyahs()[j].getNum()),
                        sura.getAyahs()[j].getText(), cleanAyahs[j].getText()));
                ++ayahIndex;// update suraIndex
            }
            surahIndex++; // update suraIndex
        }


        Log.d(TAG, "checkDb: " + repository.getTotlaAyahs());
    }

    private void initRv() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "kfgqpc_naskh.ttf");
        SuraAdapter suraAdapter = new SuraAdapter(typeface);
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
            case R.id.actionSearch:
                openSearch();
                break;

                case R.id.actionListening:
                openListening();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openListening() {
        Intent openAcivity = new Intent(ShowSuar.this, ListeningActivity.class);
        startActivity(openAcivity);
    }

    private void openSearch() {
        Intent openAcivity = new Intent(ShowSuar.this, ShowSearchResults.class);
        startActivity(openAcivity);
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
