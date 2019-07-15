package education.mahmoud.quranyapp.feature.test_sound;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.MenuSheetView;

import java.io.File;
import java.io.IOException;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.room.RecordItem;
import education.mahmoud.quranyapp.data_layer.remote.model.MLResponse;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RecordListFragment extends Fragment {

    private static final String TAG = "RecordListFragment";

    @BindView(R.id.rvRecordList)
    RecyclerView rvRecordList;
    @BindView(R.id.recordlist_bottom_sheet)
    BottomSheetLayout recordlistBottomSheet;
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
            public void onPlayRecord(RecordItem recordItem) {
                setupBottomSheet(recordItem);
            }
        });


    }

    private void setupBottomSheet(RecordItem path) {
        // bottom sheet
        MenuSheetView menuSheetView =
                new MenuSheetView(getContext(), MenuSheetView.MenuType.LIST, "Options", new MenuSheetView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (recordlistBottomSheet.isSheetShowing()) {
                            recordlistBottomSheet.dismissSheet();
                        }
                        switch (item.getItemId()) {
                            case R.id.menuUpload:
                                uploadFile(path);
                                break;
                            case R.id.menuPlayRecord:
                                playAudio(path);
                                break;
                        }
                        return true;
                    }
                });
        menuSheetView.inflateMenu(R.menu.menu_sheet_recordlist);
        recordlistBottomSheet.showWithSheetView(menuSheetView);
    }

    private void uploadFile(RecordItem path) {
        Log.d(TAG, "uploadFile: ");
        String resFromApi = "الحمد ";
        //<editor-fold desc="api">
        // make body
        MediaType MEDIA_TYPE_AUDIO = MediaType.parse("audio/*");
        File file = new File(path.getFilePath());
        Log.d(TAG, "uploadFile: " + file.exists());

        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_AUDIO, file);
        //callUpload2(requestBody , file);


        requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", "Square Logo")
                .addFormDataPart("file", "aa.mp4",
                        RequestBody.create(MEDIA_TYPE_AUDIO, new File(path.getFilePath())))
                .build();

        // callUpload(requestBody);

        //    upload3(file);
        //</editor-fold>
        // TODO: 7/15/2019  add it inside api (above)
        handleResultFromApi(path, resFromApi);
    }

    private void handleResultFromApi(RecordItem path, String resFromApi) {
        // get ayahs
        List<AyahItem> ayahItemList = repository.getAyahSInRange(path.getStartIndex(), path.getEndIndex());
        // concatenate
        String ayhasStr = concateAyahs(ayahItemList);

        Spannable spannable = Util.getDiffSpannaled(ayhasStr, resFromApi);
        updateTotalScore(Util.getTotalScore()); // Util.getTotalScore() -> score for yours Save test

        Util.getTestResltDialoge(getContext(), spannable).show();

    }

    /**
     * called after finish test and used to update score in db and show score for current test
     *
     * @param totalScore
     */
    private void updateTotalScore(long totalScore) {
        long cuurentTotalScore = repository.getScore(); // saved score
        cuurentTotalScore += totalScore; // update with new score
        repository.setScore(cuurentTotalScore); // set in db

        Util.getDialog(getContext(), String.valueOf(cuurentTotalScore), getString(R.string.score)).show();
    }


    private String concateAyahs(List<AyahItem> ayahItemList) {
        StringBuilder builder = new StringBuilder();
        for (AyahItem ayahItem : ayahItemList) {
            builder.append(ayahItem.getTextClean());
        }
        return builder.toString();
    }

    private void callUpload2(RequestBody requestBody, File file) {
        repository.uploadFile(file).enqueue(new Callback<MLResponse>() {
            @Override
            public void onResponse(Call<MLResponse> call, Response<MLResponse> response) {
                Log.d(TAG, "## onResponse: call " + call.request().body().contentType());
                try {
                    Log.d(TAG, "## onResponse: " + response.raw());
                    showMessage(response.body().getResult());
                } catch (Exception e) {
                    showMessage("error");
                }
            }

            @Override
            public void onFailure(Call<MLResponse> call, Throwable t) {
                showMessage(t.getMessage());
            }
        });
    }


    private void callUpload(RequestBody requestBody) {
        repository.upload(requestBody).enqueue(new Callback<MLResponse>() {
            @Override
            public void onResponse(Call<MLResponse> call, Response<MLResponse> response) {
                try {
                    Log.d(TAG, "onResponse:call type  " + call.request().body().contentType());
                    Log.d(TAG, "onResponse: " + response.raw());
                    Log.d(TAG, "onResponse: res " + response.body().getResult());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<MLResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }


    public void upload3(File file) {
        MultipartBody.Part filePart = MultipartBody.Part.createFormData
                ("file", file.getName(),
                        RequestBody.create(MediaType.parse("audio/*"), file));

        repository.upload(filePart).enqueue(new Callback<MLResponse>() {
            @Override
            public void onResponse(Call<MLResponse> call, Response<MLResponse> response) {
                try {
                    Log.d(TAG, "onResponse: %% " + response.raw());
                    Log.d(TAG, "onResponse: %%call  " + call.request().body().contentType());
                    Log.d(TAG, "onResponse: %%  " + response.body().getResult());
                    showMessage(response.body().getResult());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<MLResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());

            }
        });
    }

    //region play audio
    private void playAudio(RecordItem path) {
        Log.d(TAG, "playAudio: path " + path);
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        player = new MediaPlayer();
        try {
            player.setDataSource(path.getFilePath());
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //endregion

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

    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
