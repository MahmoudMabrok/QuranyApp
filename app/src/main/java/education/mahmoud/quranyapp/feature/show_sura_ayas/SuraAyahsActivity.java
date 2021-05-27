package education.mahmoud.quranyapp.feature.show_sura_ayas;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;

public class SuraAyahsActivity extends AppCompatActivity {

    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sura_ayahs);

        int pos = getIntent().getIntExtra(Constants.SURAH_INDEX, 1);
        String suraName = Data.SURA_NAMES[pos - 1];
        TextView tvSuraName = findViewById(R.id.tvSuraName);
        tvSuraName.setText(suraName);
        repository = Repository.getInstance(getApplication());
        List<AyahItem> list = repository.getAllAyahOfSurahByIndex(pos);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "me_quran.ttf");
        AyahsAdapter ayahsAdapter = new AyahsAdapter(typeface);
        RecyclerView recyclerView = findViewById(R.id.rvAyahs);
        recyclerView.setAdapter(ayahsAdapter);
        ayahsAdapter.setAyahItemList(list);

    }
}