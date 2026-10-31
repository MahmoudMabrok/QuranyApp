package education.mahmoud.quranyapp.feature.scores;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.koin.java.KoinJavaComponent;

import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.datalayer.QuranRepository;
import education.mahmoud.quranyapp.feature.home_Activity.HomeActivity;
import education.mahmoud.quranyapp.utils.EdgeToEdge;
import education.mahmoud.quranyapp.utils.NumberHelper;
import education.mahmoud.quranyapp.utils.UiFont;

/**
 * Points screen. Shows the total that the memorization test adds to
 * ({@link QuranRepository#getScore()}), the level it reaches and the way to the next one.
 * The app stores only that total, so there are no per-test stats here.
 */
public class ScoreActivity extends AppCompatActivity {

    private final QuranRepository quranRepository = KoinJavaComponent.get(QuranRepository.class);

    private View scoreContent;
    private View scoreEmpty;
    private View scoreShare;
    private TextView tvWelcomeUser;
    private TextView tvTotalScore;
    private TextView tvScoreWords;
    private TextView tvLevelBadge;
    private TextView tvNextLevel;
    private TextView tvRemaining;
    private ImageView ivLevelIcon;
    private LinearProgressIndicator scoreProgress;
    private LinearLayout levelsList;

    private ScoreProgress progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // teal app bar behind the status bar -> white icons
        EdgeToEdge.enable(this, true);
        setContentView(R.layout.activity_score);

        View toolbar = findViewById(R.id.scoreToolbar);
        View scroll = findViewById(R.id.scoreScroll);
        EdgeToEdge.applyPadding(toolbar, true, true, true, false);
        EdgeToEdge.applyPadding(scroll, true, false, true, true);

        scoreContent = findViewById(R.id.scoreContent);
        scoreEmpty = findViewById(R.id.scoreEmpty);
        scoreShare = findViewById(R.id.scoreShare);
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvTotalScore = findViewById(R.id.tvTotalScore);
        tvScoreWords = findViewById(R.id.tvScoreWords);
        tvLevelBadge = findViewById(R.id.tvLevelBadge);
        tvNextLevel = findViewById(R.id.tvNextLevel);
        tvRemaining = findViewById(R.id.tvRemaining);
        ivLevelIcon = findViewById(R.id.ivLevelIcon);
        scoreProgress = findViewById(R.id.scoreProgress);
        levelsList = findViewById(R.id.levelsList);

        // the theme font (me_quran) has no Arabic-Indic digits and merges words in labels
        applyUiFont(findViewById(R.id.scoreRoot));
        UiFont.applyBold(findViewById(R.id.scoreTitle), tvTotalScore, tvLevelBadge,
                findViewById(R.id.tvLevelsTitle), findViewById(R.id.tvHowTitle),
                findViewById(R.id.tvEmptyTitle));

        findViewById(R.id.scoreBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        scoreShare.setOnClickListener(v -> share());
        findViewById(R.id.btnStartTest).setOnClickListener(v -> startMemorizationTest());

        String name = quranRepository.getUserName();
        if (!TextUtils.isEmpty(name)) {
            tvWelcomeUser.setText(getString(R.string.welcome, name));
            tvWelcomeUser.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // re-read every time: a test taken after this screen was created adds points
        render(quranRepository.getScore());
    }

    private void render(long score) {
        progress = ScoreLevels.progress(score);
        boolean empty = progress.getScore() == 0;
        scoreContent.setVisibility(empty ? View.GONE : View.VISIBLE);
        scoreEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        scoreShare.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) return;

        ScoreLevel level = progress.getLevel();
        int color = ContextCompat.getColor(this, levelColor(level));
        tvTotalScore.setText(NumberHelper.Companion.getArabicNumber(progress.getScore()));
        tvTotalScore.setTextColor(color);
        // the old screen spelled the number out with ArabicTools, which is wrong past 1000
        // ("ألف ألفاً ...") and ignores counting rules ("ثلاث نقطة"); the digits are enough
        tvScoreWords.setText(ScoreLevels.pointsUnit(progress.getScore()));
        tvLevelBadge.setText(getString(R.string.score_your_level, getString(levelName(level))));
        GradientDrawable badge = (GradientDrawable) tvLevelBadge.getBackground().mutate();
        badge.setColor(color);
        ImageViewCompat.setImageTintList(ivLevelIcon, ColorStateList.valueOf(color));

        ScoreLevel next = progress.getNext();
        scoreProgress.setProgressCompat(progress.getPercent(), false);
        if (next == null) {
            tvNextLevel.setText(R.string.score_top_level);
            tvRemaining.setVisibility(View.GONE);
        } else {
            String nextName = getString(levelName(next));
            tvNextLevel.setText(getString(R.string.score_next_level, nextName));
            tvRemaining.setText(getString(R.string.score_remaining,
                    ScoreLevels.pointsLabel(progress.getRemaining()), nextName));
            tvRemaining.setVisibility(View.VISIBLE);
        }
        bindLevels(level);
    }

    private void bindLevels(ScoreLevel current) {
        levelsList.removeAllViews();
        int pad = dp(8);
        for (ScoreLevel level : ScoreLevels.getRanked()) {
            boolean isCurrent = level == current;
            boolean reached = progress.getScore() >= level.getMinPoints();
            int color = ContextCompat.getColor(this, reached ? levelColor(level) : R.color.score_level_none);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(pad, pad, pad, pad);
            if (isCurrent) {
                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(dp(10));
                bg.setColor(ContextCompat.getColor(this, R.color.search_chip_bg));
                row.setBackground(bg);
            }

            ImageView icon = new ImageView(this);
            icon.setImageResource(R.drawable.ic_trophy);
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(color));
            row.addView(icon, new LinearLayout.LayoutParams(dp(22), dp(22)));

            TextView name = label(getString(levelName(level)), 15,
                    ContextCompat.getColor(this, reached ? R.color.search_ayah_text : R.color.search_muted));
            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            nameLp.setMarginStart(dp(10));
            row.addView(name, nameLp);

            if (isCurrent) {
                TextView tag = label(getString(R.string.score_level_current), 12, ContextCompat.getColor(this, android.R.color.white));
                GradientDrawable tagBg = new GradientDrawable();
                tagBg.setCornerRadius(dp(10));
                tagBg.setColor(color);
                tag.setBackground(tagBg);
                tag.setPadding(dp(8), dp(2), dp(8), dp(2));
                LinearLayout.LayoutParams tagLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tagLp.setMarginEnd(dp(8));
                row.addView(tag, tagLp);
                UiFont.applyBold(name, tag);
            }

            row.addView(label(getString(R.string.score_level_from,
                    ScoreLevels.pointsLabel(level.getMinPoints())), 13,
                    ContextCompat.getColor(this, R.color.search_muted)));
            levelsList.addView(row);
        }
    }

    private TextView label(String text, int sp, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setTextColor(color);
        tv.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        return tv;
    }

    private void share() {
        if (progress == null || progress.getScore() == 0) return;
        String text = getString(R.string.score_share_text,
                ScoreLevels.pointsLabel(progress.getScore()),
                getString(levelName(progress.getLevel())));
        Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(send, getString(R.string.score_share)));
    }

    /** The test lives in HomeActivity; bring it to the front (or start it) and open the test there. */
    private void startMemorizationTest() {
        Intent intent = new Intent(this, HomeActivity.class)
                .putExtra(HomeActivity.EXTRA_OPEN_MEMORIZATION_TEST, true)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @StringRes
    static int levelName(ScoreLevel level) {
        switch (level) {
            case BEGINNER: return R.string.score_level_beginner;
            case DILIGENT: return R.string.score_level_diligent;
            case ADVANCED: return R.string.score_level_advanced;
            case PROFICIENT: return R.string.score_level_proficient;
            case DISTINGUISHED: return R.string.score_level_distinguished;
            default: return R.string.na;
        }
    }

    @ColorRes
    static int levelColor(ScoreLevel level) {
        switch (level) {
            case BEGINNER: return R.color.score_level_beginner;
            case DILIGENT: return R.color.score_level_diligent;
            case ADVANCED: return R.color.score_level_advanced;
            case PROFICIENT: return R.color.score_level_proficient;
            case DISTINGUISHED: return R.color.score_level_distinguished;
            default: return R.color.score_level_none;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static void applyUiFont(View view) {
        if (view instanceof TextView) {
            UiFont.apply((TextView) view);
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) applyUiFont(group.getChildAt(i));
        }
    }
}
