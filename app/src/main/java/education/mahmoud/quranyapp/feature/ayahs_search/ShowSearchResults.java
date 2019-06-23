package education.mahmoud.quranyapp.feature.ayahs_search;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowAyahsActivity;

public class ShowSearchResults extends AppCompatActivity {

    private static final String TAG = "ShowSearchResults";
    private static final int RC_STORAGE = 10002;
    @BindView(R.id.edSearch)
    TextInputEditText edSearch;
    @BindView(R.id.rvSearch)
    RecyclerView rvSearch;
    @BindView(R.id.tvNotFound)
    TextView tvNotFound;
    @BindView(R.id.tvSearchCount)
    TextView tvSearchCount;

    SearchResultsAdapter adapter;
    @BindView(R.id.bottomSearch)
    BottomSheetLayout bottomSearch;
    private Repository repository;
    private List<AyahItem> ayahItems;
    private String ayah;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_search_results);
        ButterKnife.bind(this);
        repository = Repository.getInstance(getApplication());
        initRv();

        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ayah = editable.toString();
                ayah = ayah.replaceAll(" +", " ");
                if (!TextUtils.isEmpty(ayah) && ayah.length() > 0) {
                    ayahItems = repository.getAyahByAyahText(ayah);
                    Log.d(TAG, "afterTextChanged: " + ayahItems.size());

                    int count = ayahItems.size(); // n of results
                    tvSearchCount.setText(getString(R.string.times, count));
                    if (count > 0) {
                        adapter.setAyahItemList(ayahItems, ayah);
                        foundState();
                    } else {
                        notFoundState();
                    }
                } else {
                    defaultState();
                }

            }
        });

    }

    /**
     * state with no search performed
     */
    private void defaultState() {
        tvNotFound.setVisibility(View.GONE);
    }

    private void initRv() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rvSearch.setLayoutManager(manager);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "me_quran.ttf");
        adapter = new SearchResultsAdapter(typeface);
        rvSearch.setAdapter(adapter);
        rvSearch.setHasFixedSize(true);

        adapter.setiOnPlay(new SearchResultsAdapter.IOnPlay() {
            @Override
            public void onPlayClick(AyahItem item) {
                playAudio(item);
            }
        });

        adapter.setiOpenAyahInPage(new SearchResultsAdapter.IOpenAyahInPage() {
            @Override
            public void openPage(int index) {
                Intent openAcivity = new Intent(ShowSearchResults.this, ShowAyahsActivity.class);
                openAcivity.putExtra(Constants.PAGE_INDEX, index);
                startActivity(openAcivity);
                //    finish();
            }
        });

    }

    private void foundState() {
        rvSearch.setVisibility(View.VISIBLE);
        tvNotFound.setVisibility(View.GONE);
    }

    private void notFoundState() {
        rvSearch.setVisibility(View.GONE);
        tvNotFound.setVisibility(View.VISIBLE);
    }

    private void playAudio(AyahItem item) {
        if (item.getAudioPath() != null) {
            Log.d(TAG, "playAudio: ");
            try {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                mediaPlayer = new MediaPlayer();
                WeakReference<MediaPlayer> mediaPlayerWeakReference = new WeakReference<>(mediaPlayer);
                mediaPlayer.setDataSource(item.getAudioPath());
                mediaPlayer.prepare();
                mediaPlayer.start();


                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer1) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("file " + item.getAyahIndex() + " is correupt ");
                showMessage("Problem with file , contact us ,  " + e.getMessage());
            }
        } else {
            showMessage(getString(R.string.not_downlod_audio));
        }

    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }


    }
}
