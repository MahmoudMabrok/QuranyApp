package education.mahmoud.quranyapp.feature.ayahs_search;

import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.AyahItem;

public class ShowSearchResults extends AppCompatActivity implements OnDownloadListener {

    private static final String TAG = "ShowSearchResults";
    @BindView(R.id.edSearch)
    TextInputEditText edSearch;
    @BindView(R.id.rvSearch)
    RecyclerView rvSearch;
    @BindView(R.id.tvNotFound)
    TextView tvNotFound;
    @BindView(R.id.tvSearchCount)
    TextView tvSearchCount;
    SearchResultsAdapter adapter;
    private Repository repository;
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
                String ayah = editable.toString();
                if (ayah.length() > 0) {
                    List<AyahItem> ayahItems = repository.getAyahByAyahText(ayah);
                    Log.d(TAG, "afterTextChanged: " + ayahItems.size());

                    int count = ayahItems.size(); // n of results
                    tvSearchCount.setText(getString(R.string.times, count));
                    if (count > 0) {
                        adapter.setAyahItemList(ayahItems, ayah);
                        foundState();
                    } else {
                        notFoundState();
                    }
                }

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

    private void initRv() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rvSearch.setLayoutManager(manager);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "kfgqpc_naskh.ttf");
        adapter = new SearchResultsAdapter(typeface);
        rvSearch.setAdapter(adapter);


        adapter.setiOnDownload(new SearchResultsAdapter.IOnDownload() {
            @Override
            public void onDownloadClick(AyahItem item) {
                showMessage("Start Downloading .....");
                downloadAudio(item);
            }
        });

        adapter.setiOnPlay(new SearchResultsAdapter.IOnPlay() {
            @Override
            public void onPlayClick(AyahItem item) {
                playAudio(item);
            }

        });

    }

    private void playAudio(AyahItem item) {
        Log.d(TAG, "playAudio: ");
        try {
            mediaPlayer = new MediaPlayer();
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
            showMessage("error");
        }

    }

    int index ;
    String url = "http://cdn.alquran.cloud/media/audio/ayah/ar.alafasy/";
    String  downURL , path , filename;

    private void downloadAudio(AyahItem item) {
        // compute index
        index = item.getAyahIndex();
        // form  URL
        downURL = url + index;
        // form path
        path = Util.getDirectoryPath(); // get folder path
        // form file name
        filename = index + ".mp3";

        Log.d(TAG, "downloadAudio:  file name " + filename);
        //start downloading
        PRDownloader.download(downURL, path, filename).build().start(this);

    }

    private void showMessage(String message) {
             Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
       }

    @Override
    public void onDownloadComplete() {
        showMessage("Finish");
        Log.d(TAG, "onDownloadComplete: ");
        // store storage path in db to use in media player
        AyahItem ayahItem = repository.getAyahByIndex(index); // first get ayah to edit it with storage path
        String storagePath = path + "/" + filename;
        ayahItem.setAudioPath(storagePath); // set path
        repository.updateAyahItem(ayahItem);

        playAudio(ayahItem);

    }

    @Override
    public void onError(Error error) {
        showMessage(getString(R.string.error_net));
    }
}
