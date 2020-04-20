package education.mahmoud.quranyapp.utils.testsomefeature;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.remote.model.MLResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

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

        //    onViewClicked();

    }

    @OnClick(R.id.btnCalculate)
    public void onViewClicked() {
        startDialoge();
        try {
            String years = tvYears.getText().toString();
            try {
                testing1(years);
            } catch (Exception e) {
                e.printStackTrace();
            }

          /*  try {
                testing2(years);
            } catch (Exception e) {
                e.printStackTrace();
            }
*/

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void testing2(String years) {

        repository.calculteSaraly2(years).enqueue(new Callback<MLResponse>() {
            @Override
            public void onResponse(Call<MLResponse> call, Response<MLResponse> response) {
                try {
                    Log.d(TAG, " @@$$ onResponse: body " + response.body().getResult());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                closeDialoge();
            }

            @Override
            public void onFailure(Call<MLResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: @@ " + t.getMessage());
                closeDialoge();
            }
        });
    }

    private void testing1(String years) {
        repository.calculteSaraly(years).enqueue(new Callback<MLResponse>() {
            @Override
            public void onResponse(Call<MLResponse> call, Response<MLResponse> response) {
                closeDialoge();
                try {
                    StringBuilder builder = new StringBuilder();
                    builder.append("n years  = " + years);
                    builder.append('\n');
                    builder.append("predicted salary :- ");
                    builder.append(response.body().getResult());
                    builder.append('\n');
                    builder.append("source :- https://qurany.herokuapp.com/test");
                    tvResult.setText(builder.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<MLResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
                closeDialoge();
            }
        });
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
