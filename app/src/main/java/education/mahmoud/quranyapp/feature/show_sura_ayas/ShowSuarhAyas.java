package education.mahmoud.quranyapp.feature.show_sura_ayas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.model.Aya;
import education.mahmoud.quranyapp.model.Sura;

public class ShowSuarhAyas extends AppCompatActivity {

    @BindView(R.id.tvAyahs)
    TextView tvAyahs;
    @BindView(R.id.tvSuraNameShowAyas)
    TextView tvSuraNameShowAyas;
    @BindView(R.id.scrollView)
    ScrollView scrollView;


    int index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_suarh_ayas);
        ButterKnife.bind(this);

        index = getIntent().getIntExtra(Constants.SURAH_INDEX, 0);
        tvSuraNameShowAyas.setText(Data.SURA_NAMES[index]);
        parseSura(index);

        Repository.getInstance(ShowSuarhAyas.this).addLastSura(index);
        Toast.makeText(ShowSuarhAyas.this, R.string.saved, Toast.LENGTH_SHORT).show();


        scrollView.post(new Runnable() {
            public void run() {
                scrollView.smoothScrollTo(0, 700);
            }
        });



    }

    private void parseSura(int index) {
        Sura sura = Util.getSurah(this, index);
        Aya[] ayas = sura.getAyahs();
        StringBuilder builder = new StringBuilder();

        String aya;
        for (int i = 0; i < ayas.length; i++) {
            aya = ayas[i].getText();
            //  aya  = Util.getSpannable(aya).toString();
            builder.append(aya + "(" + ayas[i].getNum() + ")");
            //      System.out.println("ayas = " + ayas[i].getText());
        }
        String res = builder.toString();
        tvAyahs.setText(Util.getSpannable(res), TextView.BufferType.SPANNABLE);
    }


    @Override
    protected void onStop() {
        super.onStop();
        Repository.getInstance(this).addLastSura(index);
    }
}
