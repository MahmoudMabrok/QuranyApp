package education.mahmoud.quranyapp.feature.read_log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.local.room.ReadLog;

public class ReadLogAdapter extends RecyclerView.Adapter<ReadLogAdapter.Holder> {

    private List<ReadLog> list;

    public ReadLogAdapter() {
        list = new ArrayList<>();
    }

    public void addReadLog(ReadLog item) {
        list.add(item);
        notifyItemInserted(list.size() - 1);
        notifyItemRangeChanged(list.size() - 1, list.size());
    }

    public void setReadLogList(List<ReadLog> newList) {
        list = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public List<ReadLog> getList() {
        return list;
    }

    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.read_log_item, viewGroup, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        ReadLog item = list.get(i);
        holder.readLogDate.setText(item.getStrDate());
        holder.readLogNPages.setText(String.valueOf(item.getPages().size()));
        holder.readLogPagesNum.setText(getNums(item.getPages()));


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = getNums(item.getPages());
                Util.getDialog(holder.itemView.getContext(),msg,item.getStrDate()).show();
            }
        });
    }

    private String getNums(Set<Integer> pages) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer integer:pages){
            stringBuilder.append(String.valueOf(integer));
            stringBuilder.append(",");
        }
        return stringBuilder.toString();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public ReadLog getItem(int pos) {
        return  list.get(pos);
    }

    public void deleteitem(int pos) {
        list.remove((int)pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos,list.size());
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.readLogDate)
        TextView readLogDate;
        @BindView(R.id.readLogNPages)
        TextView readLogNPages;
        @BindView(R.id.readLogPagesNum)
        TextView readLogPagesNum;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}