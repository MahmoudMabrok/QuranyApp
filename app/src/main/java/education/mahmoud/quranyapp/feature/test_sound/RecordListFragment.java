package education.mahmoud.quranyapp.feature.test_sound;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.Repository;


public class RecordListFragment extends Fragment {

    private static final String TAG = "RecordListFragment";
    
    @BindView(R.id.rvRecordList)
    RecyclerView rvRecordList;
    private RecordAdapter recorditemAdapter;
    private Repository repository;
    private MediaPlayer player;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recorditem_list, container, false);
        ButterKnife.bind(this, view);
        repository = Repository.getInstance(getActivity().getApplication());
        initRV();

        loadData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        Log.d(TAG, "onResume: ");
    }

    private void initRV() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvRecordList.setLayoutManager(layoutManager);
        recorditemAdapter = new RecordAdapter();
        rvRecordList.setAdapter(recorditemAdapter);


        recorditemAdapter.setOnPlayRecordClick(new RecordAdapter.onPlayRecordClick() {
            @Override
            public void onPlayRecord(String path) {
                playAudio(path);
            }
        });
    }

    private void playAudio(String path) {
        Log.d(TAG, "playAudio: path " + path);
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        player = new MediaPlayer();
        try {
            player.setDataSource(path);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            loadData();
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void loadData() {
        recorditemAdapter.setRecordList(repository.getRecords());
        Log.d(TAG, "loadData: " + recorditemAdapter.getItemCount());
    }
}
