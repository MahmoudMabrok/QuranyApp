package education.mahmoud.quranyapp.feature.show_sura_ayas;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.App;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.room.BookmarkItem;
import education.mahmoud.quranyapp.feature.download.DownloadActivity;

public class ShowAyahsActivity extends AppCompatActivity {

    private static final String TAG = "ShowAyahsActivity";

    @BindView(R.id.rvAyahsPages)
    RecyclerView rvAyahsPages;
    @BindView(R.id.tv_no_quran_data)
    TextView tvNoQuranData;
    @BindView(R.id.spShowAyahs)
    SpinKitView spShowAyahs;


    Repository repository;
    PageAdapter pageAdapter;
    Typeface typeface;
    int pos;
    Handler handler;
    List<Page> pageList;
    int ayahsColor, scrollorColor;
    private int lastpageShown = 1;
    private List<Integer> quraterSStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_ayahs);
        ButterKnife.bind(this);
        repository = Repository.getInstance(getApplication());

        typeface = Typeface.createFromAsset(getAssets(), "me_quran.ttf");

        pos = getIntent().getIntExtra(Constants.SURAH_INDEX, 1);
        Log.d(TAG, "onCreate: ** " + pos);
        pos = getStartPageFromIndex(pos);
        Log.d(TAG, "onCreate: *** " + pos);

        //region Description
        if (getIntent().hasExtra(Constants.SURAH_GO_INDEX)) {
            int surah = getIntent().getIntExtra(Constants.SURAH_GO_INDEX, 1);
            int ayah = getIntent().getIntExtra(Constants.AYAH_GO_INDEX, 1);
            Log.d(TAG, "onCreate: ayah  " + ayah);
            pos = getPosFromSurahAndAyah(surah, ayah);
            //       showMessage(String.valueOf(pos));D
        } else if (getIntent().hasExtra(Constants.LAST_INDEX)) {
            pos = repository.getLatestRead(); // as it will be decreased

        } else if (getIntent().hasExtra(Constants.PAGE_INDEX)) {  // case bookmark
            pos = getIntent().getIntExtra(Constants.PAGE_INDEX, 1);
         } else if (getIntent().hasExtra(Constants.JUZ_INDEX)) {
            pos = getIntent().getIntExtra(Constants.JUZ_INDEX, 1);
            pos = getPageFromJuz(pos);
        }
        //endregion

        Log.d(TAG, "onCreate: " + pos);

        initRV();


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //region handle
                if (pageList != null) {
                    if (pageList.size() > 0) {
                        rvAyahsPages.setVisibility(View.VISIBLE);
                        tvNoQuranData.setVisibility(View.GONE);
                        pageAdapter.setPageList(pageList);
                        rvAyahsPages.scrollToPosition(pos - 1);
                        foundState();
                    } else {
                        notFound();
                    }
                } else {
                    finish();
                }
                //endregion
            }
        };
    }

    private int getPosFromSurahAndAyah(int surah, int ayah) {
        return repository.getPageFromSurahAndAyah(surah, ayah);
    }

    private int getPageFromJuz(int pos) {
        return repository.getPageFromJuz(pos);
    }

    private int getStartPageFromIndex(int pos) {
        return repository.getSuraStartpage(pos);
    }

    private void initRV() {
        prepareColors();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);

        GridLayoutManager manager1 = new GridLayoutManager(this, 2);
        rvAyahsPages.setLayoutManager(manager);

        rvAyahsPages.setHasFixedSize(true);
        pageAdapter = new PageAdapter(typeface, ayahsColor, scrollorColor);
        rvAyahsPages.setAdapter(pageAdapter);
        rvAyahsPages.setItemAnimator(new DefaultItemAnimator());

        //// TODO: 6/4/2019  get pages that contain start of rub3
        pageAdapter.setPageShown(new PageAdapter.PageShown() {
            @Override
            public void onDiplayed(int pos, PageAdapter.Holder holder) {
                // items start from 0 increase 1 to get real page num, will be used in bookmark
                lastpageShown = pos + 1;

                holder.topLinear.setVisibility(View.INVISIBLE);
                holder.BottomLinear.setVisibility(View.INVISIBLE);

                // calculate Hizb info.
                Page page = pageAdapter.getPage(pos);
                if (quraterSStart.contains(page.getPageNum())) {
                    // get last ayah to extract info from it
                    AyahItem ayahItem = page.getAyahItems().get(page.getAyahItems().size()-1);
                    int rub3Num = ayahItem.getHizbQuarter();
                    rub3Num -- ; // as first one must be 0
                    if (rub3Num % 8  == 0 ){
                        showMessage(getString(R.string.juz_to_display , ayahItem.getJuz()));
                    }else if (rub3Num % 4 == 0 ){
                        showMessage(getString(R.string.hizb_to_display , rub3Num / 4));
                    }else{
                        int part = rub3Num % 4 ;
                        part -- ; // 1/4 is first element which is 0
                        String[] parts = getResources().getStringArray(R.array.parts);
                        showMessage(getString(R.string.part_to_display , parts[part], (rub3Num/4)+1));
                    }
                }


            }
        });

        pageAdapter.setiBookmark(new PageAdapter.IBookmark() {
            @Override
            public void onBookmarkClicked(Page item) {
                BookmarkItem bookmarkItem = new BookmarkItem();

                bookmarkItem.setTimemills(new Date().getTime());

                AyahItem ayahItem = item.getAyahItems().get(0);
                bookmarkItem.setSuraName(getSuraNameFromIndex(ayahItem.getSurahIndex()));
                bookmarkItem.setPageNum(item.getPageNum());
                Log.d(TAG, "onBookmarkClicked: " + bookmarkItem.getPageNum());


                repository.addBookmark(bookmarkItem);

                showMessage("Saved");
            }
        });

        rvAyahsPages.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

    }

    private void prepareColors() {
        // check Night Mode
        if (repository.getNightModeState()) {
//            tvSuraNameShowAyas.setTextColor(getResources().getColor(R.color.ayas_color_night_mode));
            ayahsColor = (getResources().getColor(R.color.ayas_color_night_mode));
            scrollorColor = (getResources().getColor(R.color.bg_ays_night_mode));
        } else {
//            tvSuraNameShowAyas.setTextColor(getResources().getColor(R.color.ayas_color));
            ayahsColor = (getResources().getColor(R.color.ayas_color));
            // check usesr color for background
            int col = repository.getBackColorState();
            switch (col) {
                case Constants.GREEN:
                    scrollorColor = (getResources().getColor(R.color.bg_green));
                    break;
                case Constants.WHITE:
                    scrollorColor = (getResources().getColor(R.color.bg_white));
                    break;

                case Constants.YELLOW:
                    scrollorColor = (getResources().getColor(R.color.bg_yellow));
                    break;
            }


        }
    }

    /**
     * @param surahIndex in quran
     * @return
     */
    private String getSuraNameFromIndex(int surahIndex) {
        return Data.SURA_NAMES[surahIndex - 1];
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        pageList = ((App) getApplication()).getQuranPages();
        if (pageList != null && pageList.size() >= 50) {
            handler.sendEmptyMessage(0);
            Log.d(TAG, "loadData: %%% ");
        } else {
            Log.d(TAG, "loadData: @@@@");
            new Thread(() -> {
                List<Page> pages = new ArrayList<>();
                Page page;
                List<AyahItem> ayahItems;
                for (int i = 1; i <= 604; i++) {
                    ayahItems = repository.getAyahsByPage(i);
                    if (ayahItems.size() > 0) {
                        page = new Page();
                        page.setAyahItems(ayahItems);
                        page.setPageNum(i);
                        page.setJuz(ayahItems.get(0).getJuz());
                        pages.add(page);
                    }
                }

                pageList = new ArrayList<>(pages);

                handler.sendEmptyMessage(0);

            }).start();
        }
        new Thread(this::generateListOfPagesStartWithHizbQurater).start();
    }

    /**
     * retrieve list of pages that contain start of hizb Quaters.
     */
    private void generateListOfPagesStartWithHizbQurater() {
       quraterSStart = repository.getHizbQuaterStart();
    }

    private void foundState() {
        spShowAyahs.setVisibility(View.GONE);
        tvNoQuranData.setVisibility(View.GONE);
        rvAyahsPages.setVisibility(View.VISIBLE);
    }

    private void notFound() {
        spShowAyahs.setVisibility(View.GONE);
        tvNoQuranData.setVisibility(View.VISIBLE);
        rvAyahsPages.setVisibility(View.GONE);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.tv_no_quran_data)
    public void onBOClicked() {
        Intent openAcivity = new Intent(ShowAyahsActivity.this, DownloadActivity.class);
        startActivity(openAcivity);
    }

    @Override
    protected void onStop() {
        super.onStop();
        repository.addLatestread(lastpageShown);
    }
}
