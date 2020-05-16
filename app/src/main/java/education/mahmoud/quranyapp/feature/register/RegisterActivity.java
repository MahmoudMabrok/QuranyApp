package education.mahmoud.quranyapp.feature.register;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.ybq.android.spinkit.SpinKitView;

import org.koin.java.KoinJavaComponent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.datalayer.Repository;
import education.mahmoud.quranyapp.utils.Util;

public class RegisterActivity extends AppCompatActivity {


    private static final String TAG = "RegisterActivity";
    @BindView(R.id.edResgisterName)
    EditText edResgisterName;
    @BindView(R.id.edResgisterEmail)
    EditText edResgisterEmail;
    @BindView(R.id.edResgisterPassword)
    EditText edResgisterPassword;
    @BindView(R.id.btnResgister)
    Button btnResgister;
    @BindView(R.id.tvOpenLogin)
    TextView tvOpenLogin;
    private Repository repository = KoinJavaComponent.get(Repository.class);
    String name, email, password;
    @BindView(R.id.spRegister)
    SpinKitView spRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);


    }

    @OnClick(R.id.btnResgister)
    public void onBtnResgisterClicked() {
        name = edResgisterName.getText().toString();
        email = edResgisterEmail.getText().toString();
        password = edResgisterPassword.getText().toString();

        if (Util.checkInput(name)) {
            if (Util.checkInput(email)) {
                if (Util.checkInput(password)) {
                    StartOperationState();
                    // reg here
                } else {
                    edResgisterPassword.setError(getString(R.string.wrong_pass));
                }

            } else {
                edResgisterEmail.setError(getString(R.string.wrong_email));
            }
        } else {
            edResgisterName.setError(getString(R.string.wrong_name));
        }
    }

    private void StartOperationState() {
        btnResgister.setVisibility(View.GONE);
        spRegister.setVisibility(View.VISIBLE);
    }

    private void FinishOperationState() {
        btnResgister.setVisibility(View.VISIBLE);
        spRegister.setVisibility(View.GONE);
    }

    /*@OnClick(R.id.tvOpenLogin)
    public void onTvOpenLoginClicked() {
        Intent openAcivity = new Intent(RegisterActivity.this, Login.class);
        //  openAcivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(openAcivity);
        finish();
    }
*/

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
