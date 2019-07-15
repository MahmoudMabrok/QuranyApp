package education.mahmoud.quranyapp.feature.test_sound;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.data_layer.local.room.RecordItem;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.Holder> {

    private List<RecordItem> list;
    private onPlayRecordClick onPlayRecordClick;

    public void setOnPlayRecordClick(RecordAdapter.onPlayRecordClick onPlayRecordClick) {
        this.onPlayRecordClick = onPlayRecordClick;
    }

    public RecordAdapter() {
        list = new ArrayList<>();
    }

    public void addRecord(RecordItem item) {
        list.add(item);
        notifyItemInserted(list.size() - 1);
        notifyItemRangeChanged(list.size() - 1, list.size());
    }


    public void setRecordList(List<RecordItem> newList) {
        list = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public List<RecordItem> getList() {
        return list;
    }

    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_recorditem, viewGroup, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        RecordItem item = list.get(i);
        holder.recordName.setText(item.getFileName());
        holder.tvNAyahs.setText(String.valueOf(item.getEndIndex() - item.getStartIndex()));
        int score = item.getResult();

        if (score == Constants.NA) {
            holder.tvRecordScore.setText("NA");
        } else {
            holder.tvRecordScore.setText(String.valueOf(score));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayRecordClick.onPlayRecord(item);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    interface onPlayRecordClick {
        void onPlayRecord(RecordItem recordItem);
    }


    class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.recordName)
        TextView recordName;
        @BindView(R.id.tvNAyahs)
        TextView tvNAyahs;
        @BindView(R.id.tvRecordScore)
        TextView tvRecordScore;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}