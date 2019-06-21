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


public class RecordListFragment extends Fragment {

    @BindView(R.id.rvRecordList)
    RecyclerView rvRecordList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recorditem_list, container, false);
        ButterKnife.bind(this, view);

        initRV();
        return view;
    }

    private void initRV() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvRecordList.setLayoutManager(layoutManager);

        RecordAdapter recorditemAdapter = new RecordAdapter();
        rvRecordList.setAdapter(recorditemAdapter);
    }

}
