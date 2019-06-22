package education.mahmoud.quranyapp.feature.test_sound;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.Repository;


public class RecordListFragment extends Fragment {

    @BindView(R.id.rvRecordList)
    RecyclerView rvRecordList;
    private RecordAdapter recorditemAdapter;
    private Repository repository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recorditem_list, container, false);
        ButterKnife.bind(this, view);
        repository = Repository.getInstance(getActivity().getApplication());
        initRV();
        return view;
    }

    private void initRV() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvRecordList.setLayoutManager(layoutManager);
        recorditemAdapter = new RecordAdapter();
        rvRecordList.setAdapter(recorditemAdapter);
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
    }
}
