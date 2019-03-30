package education.mahmoud.quranyapp.feature.show_sura_ayas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import education.mahmoud.quranyapp.feature.show_sura_list.ShowSuar;
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
    int scroll ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_suarh_ayas);
        ButterKnife.bind(this);

        // get index from intent
        index = getIntent().getIntExtra(Constants.SURAH_INDEX, 0);
        tvSuraNameShowAyas.setText(Data.SURA_NAMES[index]);
        parseSura(index);
        // add last sura
        Repository.getInstance(ShowSuarhAyas.this).addLastSura(index);

        // check if get from last read option
        scroll = getIntent().getIntExtra(Constants.LAST_INDEX_Scroll, 0);
        showMessage(""+scroll);

        scrollView.post(new Runnable() {
            public void run() {
                scrollView.smoothScrollTo(0, scroll);
            }
        });

        int scroll1 = Repository.getInstance(this).getLastSuraWithScroll();
        showMessage("** "+ scroll1);

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
        int lastScroll = scrollView.getScrollY() ;
        Repository.getInstance(this).addLastSuraWithScroll(lastScroll);
    }

    private static final String TAG = "ShowSuarhAyas";

     private void showMessage(String message) {
//             Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
         Log.d(TAG, message);
       }
}
