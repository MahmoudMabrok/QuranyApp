package education.mahmoud.quranyapp.feature.show_ayahs_from_pdf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;

public class ShowAyahsPDF extends AppCompatActivity {

    @BindView(R.id.tvMMMM)
    TextView tvMMMM;
    @BindView(R.id.topLinear)
    LinearLayout topLinear;

    @BindView(R.id.tvMMKK)
    TextView tvMMKK;
    @BindView(R.id.BottomLinear)
    LinearLayout BottomLinear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_ayahs_pdf);
        ButterKnife.bind(this);

   /*     pdfView.fromAsset("final.pdf")
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(true)
                .enableDoubletap(true)
                .defaultPage(0)
                 .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                // spacing between pages in dp. To define spacing color, set view background
                .spacing(5)
                .invalidPageColor(Color.WHITE) // color of page that is invalid and cannot be loaded
                .load();
*/


    }
}
