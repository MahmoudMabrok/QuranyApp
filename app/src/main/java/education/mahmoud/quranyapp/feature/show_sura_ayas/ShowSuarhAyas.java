package education.mahmoud.quranyapp.feature.show_sura_ayas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.model.Aya;
import education.mahmoud.quranyapp.model.Sura;

public class ShowSuarhAyas extends AppCompatActivity {

    @BindView(R.id.tvAyahs)
    TextView tvAyahs;
    @BindView(R.id.tvSuraNameShowAyas)
    TextView tvSuraNameShowAyas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_suarh_ayas);
        ButterKnife.bind(this);

        int index = getIntent().getIntExtra(Constants.SURAH_INDEX, 0);
        tvSuraNameShowAyas.setText(Data.SURA_NAMES[index]);
        parseSura(index);

    }

    private void parseSura(int index) {
        Sura sura = Util.getSurah(this, index);
        Aya[] ayas = sura.getAyahs();
        StringBuilder builder = new StringBuilder();

       /* for (int i = ayas.length -1 ; i >= 0  ; i--) {
            builder.append(ayas[i].getText() + "(" + i +")");
            System.out.println("ayas = " + ayas[i].getText());
        }
        */
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
}
