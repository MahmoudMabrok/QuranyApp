package education.mahmoud.quranyapp.feature.show_sura_ayas;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.room.BookmarkItem;

public class ShowSuarhAyas extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private static final int DEFINITION = 1;
    private static final String TAG = "ShowSuarhAyas";
    @BindView(R.id.tvAyahs)
    TextView tvAyahs;
    @BindView(R.id.tvSuraNameShowAyas)
    TextView tvSuraNameShowAyas;
    @BindView(R.id.lnShowAyahs)
    LinearLayout lnShowAyahs;
    @BindView(R.id.imBookmark)
    ImageView imBookmark;
    @BindView(R.id.sc_ayahs_text)
    ScrollView scAyahsText;
    @BindView(R.id.tvPageNum)
    TextView tvPageNum;
    @BindView(R.id.sbRate)
    SeekBar sbRate;
    int index;
    int scroll;
    int y;
    String selectedText;
    private Repository repository;
    private int rate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_suarh_ayas);
        ButterKnife.bind(this);

        repository = Repository.getInstance(getApplication());
        // get index from intent
        index = getIntent().getIntExtra(Constants.SURAH_INDEX, 0);
        tvSuraNameShowAyas.setText(Data.SURA_NAMES[index]);

        loadSurahFromDb2UI(index);
        // add last sura
        Repository.getInstance(getApplication()).addLatestread(index);
        // check if get from last read option
        scroll = getIntent().getIntExtra(Constants.LAST_INDEX_Scroll, 0);
        Log.d(TAG, "onCreate: " + scroll);
        scAyahsText.post(new Runnable() {
            public void run() {
                scAyahsText.smoothScrollTo(0, scroll);
            }
        });

        Typeface typeface = Typeface.createFromAsset(getAssets(), "me_quran.ttf");
        tvAyahs.setTypeface(typeface);

        sbRate.setOnSeekBarChangeListener(this);

        new CountDownTimer(3200, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {

                dynamicScroll();
            }
        }.start();

        tvAyahs.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Remove the "cut" option
                menu.removeItem(android.R.id.cut);
                // Remove the "copy all" option
                return true;
            }


            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Called when action mode is first created. The menu supplied
                // will be used to generate action buttons for the action mode
                // Here is an example MenuItem
                menu.add(0, DEFINITION, 0, R.string.tafseer).setIcon(R.drawable.ic_book);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Called when an action mode is about to be exited and
                // destroyed
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case DEFINITION:
                        int min = 0;
                        int max = tvAyahs.getText().length();
                        if (tvAyahs.isFocused()) {
                            final int selStart = tvAyahs.getSelectionStart();
                            final int selEnd = tvAyahs.getSelectionEnd();

                            min = Math.max(0, Math.min(selStart, selEnd));
                            max = Math.max(0, Math.max(selStart, selEnd));
                        }
                        // Perform your definition lookup with the selected text
                        selectedText = tvAyahs.getText().subSequence(min, max).toString();
                        // remove parentheses surround ayah to facilitate  user selection
                        selectedText = selectedText.replace('(', ' ');
                        selectedText = selectedText.replace(')', ' ');
                        // remove spaces to not cause errors with parsing to integer
                        selectedText = selectedText.replaceAll(" ", "");

                        try {
                            int ayahIndex = Integer.parseInt(String.valueOf(selectedText));
                            // use ++index as index in Data is start from 0 and in database start from 1
                            AyahItem ayahItem = repository.getAyahByInSurahIndex(++index, ayahIndex);
                            String message = ayahItem.getTafseer();
                            String title = getString(R.string.tafserr_info, ayahItem.getAyahInSurahIndex(), ayahItem.getPageNum(), ayahItem.getJuz());
                                    /*
                            String message = getString
                                    (R.string.message_dialoge,ayahItem.getPageNum(),ayahItem.getTafseer());*/

                            if (ayahItem.getTafseer() != null) {
                                Util.getDialog(ShowSuarhAyas.this, message, title).show();
                            } else {
                                showMessage(getString(R.string.tafseer_not_down));
                            }

                        } catch (NumberFormatException e) {
                            showMessage(getString(R.string.select_ayah));
                        }
                        // Finish and close the ActionMode
                        mode.finish();
                        return true;
                    default:
                        break;
                }
                return false;
            }

        });

        openTafseer();
    }

    private void openTafseer() {
        Log.d(TAG, "openTafseer: ");
        //    TafseerDetails tafseerDetails = TafseerDetails.newInstance("");
        //   tafseerDetails.show(getSupportFragmentManager(), "");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // // TODO: 4/22/2019 get value of rate from sharedPreference  
        /*
         *
         * ResourcesCompat.getColor(getResources(), R.color.your_color, null); //without themes
         */

        // check Night Mode
        if (repository.getNightModeState()) {
//            tvSuraNameShowAyas.setTextColor(getResources().getColor(R.color.ayas_color_night_mode));
            tvAyahs.setTextColor(getResources().getColor(R.color.ayas_color_night_mode));
            scAyahsText.setBackgroundColor(getResources().getColor(R.color.bg_ays_night_mode));
        } else {
//            tvSuraNameShowAyas.setTextColor(getResources().getColor(R.color.ayas_color));
            tvAyahs.setTextColor(getResources().getColor(R.color.ayas_color));
            // check usesr color for background
            int col = repository.getBackColorState();
            switch (col) {
                case Constants.GREEN:
                    scAyahsText.setBackgroundColor(getResources().getColor(R.color.bg_green));
                    break;
                case Constants.WHITE:
                    scAyahsText.setBackgroundColor(getResources().getColor(R.color.bg_white));
                    break;

                case Constants.YELLOW:
                    scAyahsText.setBackgroundColor(getResources().getColor(R.color.bg_yellow));
                    break;
            }


        }
    }

    private void loadSurahFromDb2UI(int index) {
        index++; // index in db start from 1
        StringBuilder builder = new StringBuilder();
        List<AyahItem> ayahs = repository.getAyahsOfSura(index);
        String aya;
        for (AyahItem ayahItem : ayahs) {
            aya = ayahItem.getText();
            builder.append(aya + " ﴿" + ayahItem.getAyahInSurahIndex() + "﴾ ");
            // // TODO: 4/22/2019  check Hizb and add A symbol
        }
        String res = builder.toString();
        tvAyahs.setText(Util.getSpannable(res), TextView.BufferType.SPANNABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tvAyahs.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Repository.getInstance(getApplication()).addLatestread(index);
        int lastScroll = scAyahsText.getScrollY();
        Repository.getInstance(getApplication()).addLatestreadWithScroll(lastScroll);
        Log.d(TAG, "onStop: last " + lastScroll);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void dynamicScroll() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                scAyahsText.post(new Runnable() {
                    @Override
                    public void run() {
                        y = 5 * rate;
                        scAyahsText.smoothScrollBy(0, y);
                        Log.d(TAG, "run: " + y);
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 500);

    }

    @OnClick(R.id.imBookmark)
    public void onViewClicked() {
        BookmarkItem bookmarkItem = new BookmarkItem();

        int scroll = scAyahsText.getScrollY();
        String name = Data.SURA_NAMES[index];
        long miilis = new Date().getTime();

        bookmarkItem.setScrollIndex(scroll);
        bookmarkItem.setSuraName(name);
        bookmarkItem.setTimemills(miilis);

        repository.addBookmark(bookmarkItem);
        Log.d(TAG, "ibBookmark: " + scroll);

        showMessage(getString(R.string.saved));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        rate = i;
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


}
