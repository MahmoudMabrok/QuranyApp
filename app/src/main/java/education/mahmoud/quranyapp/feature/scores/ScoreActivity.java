package education.mahmoud.quranyapp.feature.scores;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.utils.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.feature.register.RegisterActivity;

public class ScoreActivity extends AppCompatActivity {

    private static final String TAG = "ScoreActivity";

    @BindView(R.id.tvTotalScore)
    TextView tvTotalScore;
    @BindView(R.id.tvWelcomeUser)
    TextView tvWelcomeUser;
    @BindView(R.id.loggedLayout)
    LinearLayout loggedLayout;
    @BindView(R.id.btnUploadScore)
    Button btnUploadScore;
    @BindView(R.id.btnOpenLogin)
    Button btnOpenLogin;
    @BindView(R.id.rvScoreboard)
    RecyclerView rvScoreboard;


    ScoreboardAdapter scoreboardAdapter;
    String name, uuid;
    long score;
    Repository repository;

    List<Scoreboard> scoreboards = new ArrayList<>();
    @BindView(R.id.tvNoScores)
    TextView tvNoScores;
    @BindView(R.id.btnShowScoreBoard)
    Button btnShowScoreBoard;
    @BindView(R.id.tvScoreuserName)
    TextView tvScoreuserName;
    @BindView(R.id.tvScore)
    TextView tvScore;
    @BindView(R.id.lnScoreboard)
    LinearLayout lnScoreboard;
    @BindView(R.id.spScoreboard)
    SpinKitView spScoreboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        ButterKnife.bind(this);

        repository = Repository.getInstance(getApplication());

        score = repository.getScore();
        setScore();
        uuid = repository.getCurrentUserUUID();
        name = repository.getUserName();

    }

    private void loadData() {
        Log.d(TAG, "loadData: ");
        StartLoadDataState();

    }


    private void StartLoadDataState() {
        spScoreboard.setVisibility(View.VISIBLE);
        lnScoreboard.setVisibility(View.GONE);
        btnShowScoreBoard.setVisibility(View.GONE);
    }

    private void FinsihLoadDataState() {
        spScoreboard.setVisibility(View.GONE);
        lnScoreboard.setVisibility(View.VISIBLE);
        btnShowScoreBoard.setVisibility(View.VISIBLE);
    }


    private void emptyDataState() {
        lnScoreboard.setVisibility(View.GONE);
        tvNoScores.setVisibility(View.VISIBLE);
    }

    private void availavleDataState() {
        lnScoreboard.setVisibility(View.VISIBLE);
        tvNoScores.setVisibility(View.GONE);
    }

    private void initRv() {
        scoreboardAdapter = new ScoreboardAdapter();
        rvScoreboard.setLayoutManager(new LinearLayoutManager(this));
        rvScoreboard.setAdapter(scoreboardAdapter);
        rvScoreboard.setHasFixedSize(true);
    }

    private void UserState() {
        tvWelcomeUser.setVisibility(View.VISIBLE);
        tvWelcomeUser.setText(getString(R.string.welcome, name));
        btnUploadScore.setVisibility(View.VISIBLE);
        btnOpenLogin.setVisibility(View.GONE);
    }

    private void NotUserState() {
        tvWelcomeUser.setVisibility(View.GONE);
        btnUploadScore.setVisibility(View.GONE);
    }

    private void setScore() {
        if (score == 0) {
            tvTotalScore.setText(R.string.na);
        } else {
            tvTotalScore.setText(getString(R.string.score_str, score, Util.getArabicStrOfNum(score)));
        }
    }


    @OnClick(R.id.btnUploadScore)
    public void onBtnUploadScoreClicked() {
    }

    @OnClick(R.id.btnOpenLogin)
    public void onBtnOpenLoginClicked() {
        Intent intent = new Intent(ScoreActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.btnShowScoreBoard)
    public void onViewClicked() {
        loadData();
    }
}
