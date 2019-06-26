package education.mahmoud.quranyapp.Util.testsomefeature;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.Repository;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    List<String> namesofFile = new ArrayList<>();
    @BindView(R.id.tvYears)
    EditText tvYears;
    @BindView(R.id.btnCalculate)
    Button btnCalculate;

    Repository repository;
    @BindView(R.id.spResults)
    SpinKitView spResults;
    @BindView(R.id.tvResult)
    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        repository = Repository.getInstance(getApplication());

        onViewClicked();

    }

    @OnClick(R.id.btnCalculate)
    public void onViewClicked() {
        startDialoge();
        try {
            String years = tvYears.getText().toString();
            Log.d(TAG, "onViewClicked: years " + years);


            try {
                repository.calculteSaraly(years).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        closeDialoge();
                        try {
                            Log.d(TAG, "onResponse: call " + call.request().toString());
                            Log.d(TAG, "onResponse:  res " + response.body().toString());
                            tvResult.setText(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        closeDialoge();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                repository.calculteSaraly2(years).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d(TAG, " @@ onResponse: " + response.raw().toString());
                        try {
                            Log.d(TAG, " @@$$ onResponse: body " + response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        closeDialoge();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d(TAG, "onFailure: @@ " + t.getMessage());
                        closeDialoge();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void closeDialoge() {
        tvResult.setVisibility(View.VISIBLE);
        btnCalculate.setVisibility(View.VISIBLE);
        spResults.setVisibility(View.INVISIBLE);
    }

    private void startDialoge() {
        btnCalculate.setVisibility(View.INVISIBLE);
        spResults.setVisibility(View.VISIBLE);
        tvResult.setVisibility(View.INVISIBLE);
    }
}
