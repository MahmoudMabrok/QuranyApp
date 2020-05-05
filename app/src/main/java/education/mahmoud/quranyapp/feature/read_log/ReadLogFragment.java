package education.mahmoud.quranyapp.feature.read_log;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.koin.java.KoinJavaComponent;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.datalayer.Repository;
import education.mahmoud.quranyapp.datalayer.local.room.ReadLog;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReadLogFragment extends Fragment {


    @BindView(R.id.rvReadLog)
    RecyclerView rvReadLog;

    private Repository repository = KoinJavaComponent.get(Repository.class);
    private ReadLogAdapter logAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_read_log, container, false);
        ButterKnife.bind(this, view);

        initRV();
        loadData();
        return view;
    }

    private void initRV() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvReadLog.setLayoutManager(linearLayoutManager);
        logAdapter = new ReadLogAdapter();
        rvReadLog.setAdapter(logAdapter);

        rvReadLog.setItemAnimator(new DefaultItemAnimator());

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                ReadLog readLog = logAdapter.getItem(pos);
                repository.deleteReadLog(readLog);
                logAdapter.deleteitem(pos);
                showMessage(getString(R.string.msg_deleted));
            }
        }).attachToRecyclerView(rvReadLog);
    }

     private void showMessage(String message) {
             Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
       }

    private void loadData() {
        logAdapter.setReadLogList(repository.getReadLog());
    }

}
