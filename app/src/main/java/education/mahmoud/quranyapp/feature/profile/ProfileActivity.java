package education.mahmoud.quranyapp.feature.profile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.model.User;

public class ProfileActivity extends AppCompatActivity {


    private static final String TAG = "ProfileActivity";


    @BindView(R.id.buttonSignUp)
    Button buttonSignUp;


    Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        repository = Repository.getInstance(getApplication());

    }

    @OnClick(R.id.buttonSignUp)
    public void onViewClicked() {

        User user = new User();
        user.setName("maahmoud");
        user.setScore(1);
        user.setEmail("m@gmail.com");
        user.setTotalNumAyahs(0);

       /* Call<String> sign = repository.signUp(user);
        sign.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()){
                    Log.d(TAG, "onResponse: success " + response.body() );
                    Log.d(TAG, "onResponse: " + response.message());
                }
                Log.d(TAG, "onResponse: " + response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });*/


    }
}
