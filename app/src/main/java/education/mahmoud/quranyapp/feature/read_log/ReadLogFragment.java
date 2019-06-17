package education.mahmoud.quranyapp.feature.read_log;


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

/**
 * A simple {@link Fragment} subclass.
 */
public class ReadLogFragment extends Fragment {


    @BindView(R.id.rvReadLog)
    RecyclerView rvReadLog;

    Repository repository;
    private ReadLogAdapter logAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_read_log, container, false);
        ButterKnife.bind(this,view);
        repository = Repository.getInstance(getActivity().getApplication());
        initRV();
        loadData();
        return view;
    }

    private void initRV() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvReadLog.setLayoutManager(linearLayoutManager);
        logAdapter = new ReadLogAdapter();
        rvReadLog.setAdapter(logAdapter);
    }

    private void loadData() {
        logAdapter.setReadLogList(repository.getReadLog());
    }

}
