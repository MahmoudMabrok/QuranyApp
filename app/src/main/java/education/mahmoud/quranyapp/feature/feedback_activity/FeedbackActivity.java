package education.mahmoud.quranyapp.feature.feedback_activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.utils.Util;
import education.mahmoud.quranyapp.data_layer.Repository;


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

    Repository repository;
    String pros, cons, suggs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);

        repository = Repository.getInstance(getApplication());

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
