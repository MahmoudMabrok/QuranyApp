package education.mahmoud.quranyapp.feature.show_sura_ayas;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.AyahItem;

public class ShowSuarhAyas extends AppCompatActivity {

    @BindView(R.id.tvAyahs)
    TextView tvAyahs;
    @BindView(R.id.tvSuraNameShowAyas)
    TextView tvSuraNameShowAyas;
    @BindView(R.id.scrollView)
    ScrollView scrollView;

    private Repository repository;
    int index;
    private static final String TAG = "ShowSuarhAyas";
    int scroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_suarh_ayas);
        ButterKnife.bind(this);

        repository = Repository.getInstance(getApplication());
        // get index from intent
        index = getIntent().getIntExtra(Constants.SURAH_INDEX, 0);
        tvSuraNameShowAyas.setText(Data.SURA_NAMES[index]);

        loadSurahFromDb2UI(index);
        // add last sura
        Repository.getInstance(getApplication()).addLastSura(index);
        // check if get from last read option
        scroll = getIntent().getIntExtra(Constants.LAST_INDEX_Scroll, 0);


        Typeface typeface = Typeface.createFromAsset(getAssets(), "kfgqpc_naskh.ttf");
        tvAyahs.setTypeface(typeface);

    }

    @Override
    protected void onResume() {
        super.onResume();
        showMessage(" intented scroll " + scroll);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.smoothScrollTo(0, scroll);
            }
        });

        int scroll1 = Repository.getInstance(getApplication()).getLastSuraWithScroll();
    }

    private void loadSurahFromDb2UI(int index) {
        index++; // index in db start from 1
        StringBuilder builder = new StringBuilder();
        List<AyahItem> ayahs = repository.getAyahsOfSura(index);
        String aya;
        for (AyahItem ayahItem : ayahs) {
            aya = ayahItem.getText();
            builder.append(aya + " (" + ayahItem.getAyahInSurahIndex() + ") ");
        }
        String res = builder.toString();
        tvAyahs.setText(Util.getSpannable(res), TextView.BufferType.SPANNABLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Repository.getInstance(getApplication()).addLastSura(index);
        int lastScroll = scrollView.getScrollY();
        Repository.getInstance(getApplication()).addLastSuraWithScroll(lastScroll);
        showMessage("last stop scroll " + lastScroll);
    }

    private void showMessage(String message) {
        //  Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, message);
    }
}
