package education.mahmoud.quranyapp.feature.read_log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.datalayer.local.room.ReadLog;
import education.mahmoud.quranyapp.utils.UiFont;
import education.mahmoud.quranyapp.utils.Util;

/**
 * Reading log rows. Views are looked up with findViewById: ButterKnife bindings are not
 * generated in this module (kapt is applied, so the annotationProcessor dependency is
 * ignored) and every row crashed with a null TextView as soon as the log had an entry.
 */
public class ReadLogAdapter extends RecyclerView.Adapter<ReadLogAdapter.Holder> {

    private List<ReadLog> list;

    public ReadLogAdapter() {
        list = new ArrayList<>();
    }

    public void addReadLog(ReadLog item) {
        list.add(item);
        notifyItemInserted(list.size() - 1);
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

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.read_log_item, viewGroup, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        ReadLog item = list.get(i);
        int count = item.getPages() == null ? 0 : item.getPages().size();
        String date = item.getDate() > 0 ? ReadLogFormat.INSTANCE.fullDate(item.getDate(), java.util.Calendar.getInstance())
                : String.valueOf(item.getStrDate());
        String relative = item.getDate() > 0
                ? ReadLogFormat.INSTANCE.relativeDay(item.getDate(), System.currentTimeMillis()) : null;
        String ranges = ReadLogFormat.INSTANCE.pageRanges(item.getPages());

        holder.readLogDate.setText(date);
        holder.readLogDay.setVisibility(relative == null ? View.GONE : View.VISIBLE);
        holder.readLogDay.setText(relative);
        holder.readLogNPages.setText(ReadLogFormat.INSTANCE.pagesCount(count));
        holder.readLogPagesNum.setText(ranges);

        holder.itemView.setOnClickListener(v ->
                Util.getDialog(holder.itemView.getContext(), ranges, date).show());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public ReadLog getItem(int pos) {
        return list.get(pos);
    }

    public void deleteitem(int pos) {
        list.remove(pos);
        notifyItemRemoved(pos);
    }

    static class Holder extends RecyclerView.ViewHolder {

        final TextView readLogDay;
        final TextView readLogDate;
        final TextView readLogNPages;
        final TextView readLogPagesLabel;
        final TextView readLogPagesNum;

        Holder(@NonNull View itemView) {
            super(itemView);
            readLogDay = itemView.findViewById(R.id.readLogDay);
            readLogDate = itemView.findViewById(R.id.readLogDate);
            readLogNPages = itemView.findViewById(R.id.readLogNPages);
            readLogPagesLabel = itemView.findViewById(R.id.readLogPagesLabel);
            readLogPagesNum = itemView.findViewById(R.id.readLogPagesNum);
            UiFont.applyBold(readLogDate, readLogDay, readLogNPages);
            UiFont.apply(readLogPagesLabel, readLogPagesNum);
        }
    }
}
