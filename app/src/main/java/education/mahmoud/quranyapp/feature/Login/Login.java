package education.mahmoud.quranyapp.feature.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.textfield.TextInputLayout;

import org.koin.java.KoinJavaComponent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.feature.register.RegisterActivity;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login";

    @BindView(R.id.edEmailLogin)
    EditText edEmailLogin;
    @BindView(R.id.textInputEmail)
    TextInputLayout textInputEmail;
    @BindView(R.id.edPassLogin)
    EditText edPassLogin;
    @BindView(R.id.textInputPassword)
    TextInputLayout textInputPassword;
    @BindView(R.id.btnLogin)
    Button btnLogin;
    @BindView(R.id.tvGoToRegister)
    TextView tvGoToLogin;
    @BindView(R.id.spLogin)
    SpinKitView spLogin;
    private Repository repository = KoinJavaComponent.get(Repository.class);
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);


    }

    @OnClick(R.id.btnLogin)
    public void onViewClicked() {
        email = edEmailLogin.getText().toString();
        password = edPassLogin.getText().toString();

        if (checkInput(email)) {
            if (checkInput(password)) {
                StartOperationState();

            } else {
                edPassLogin.setError(getString(R.string.wrong_pass));
            }

        } else {
            edEmailLogin.setError(getString(R.string.wrong_email));
        }

    }

    private void StartOperationState() {
        btnLogin.setVisibility(View.GONE);
        spLogin.setVisibility(View.VISIBLE);
    }

    private void FinishOperationState() {
        btnLogin.setVisibility(View.VISIBLE);
        spLogin.setVisibility(View.GONE);
    }


    private boolean checkInput(String input) {
        return !TextUtils.isEmpty(input) && input.replaceAll(" ", "").length() != 0;
    }

    @OnClick(R.id.tvGoToRegister)
    public void onTvOpenRegister() {
        Intent openAcivity = new Intent(Login.this, RegisterActivity.class);
        openAcivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(openAcivity);
        finish();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
