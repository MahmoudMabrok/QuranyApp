package education.mahmoud.quranyapp.feature.feedback_activity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.koin.java.KoinJavaComponent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.datalayer.QuranRepository;
import education.mahmoud.quranyapp.utils.Util;


public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "FeedbackActivity";

    @BindView(R.id.edPositives)
    TextInputEditText edPositives;
    @BindView(R.id.edNegatives)
    TextInputEditText edNegatives;
    @BindView(R.id.edSuggestions)
    TextInputEditText edSuggestions;
    @BindView(R.id.btnSendFeedback)
    Button btnSendFeedback;

    private QuranRepository quranRepository = KoinJavaComponent.get(QuranRepository.class);
    String pros, cons, suggs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);


    }

    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
     //   getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btnSendFeedback)
    public void onViewClicked() {

        pros = edPositives.getText().toString();
        cons = edNegatives.getText().toString();
        suggs = edSuggestions.getText().toString();

        if (Util.checkInput(pros)) {
            if (Util.checkInput(cons)) {

            } else {
                edNegatives.setError(getString(R.string.empty));
            }
        } else {
            edPositives.setError(getString(R.string.empty));
        }
    }

    private void blankFields() {
        edPositives.setText(null);
        edNegatives.setText(null);
        edSuggestions.setText(null);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
