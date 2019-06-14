package education.mahmoud.quranyapp.feature.show_tafseer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;

public class TafseerAdapter extends RecyclerView.Adapter<TafseerAdapter.Holder> {

    private static final String TAG = "TafseerAdapter";
    private List<AyahItem> list;

    public TafseerAdapter() {
        list = new ArrayList<>();
    }

    public void addTafseer(AyahItem item) {
        list.add(item);
        notifyItemInserted(list.size() - 1);
        notifyItemRangeChanged(list.size() - 1, list.size());
    }

    public void setTafseerList(List<AyahItem> newList) {
        Log.d(TAG, "setTafseerList: " + newList.size());
        list = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public List<AyahItem> getList() {
        return list;
    }

    @NotNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tafseer_item, viewGroup, false);
        Holder holder = new Holder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        AyahItem item = list.get(i);
        holder.tvtafseerAyah.setText(holder.itemView.getContext()
                .getString(R.string.tafseer_ayah_text_header, i + 1, item.getText()));
        String tafsser = item.getTafseer();
        if (tafsser != null) {
            holder.tvtafseer.setText(tafsser);
        } else {
            holder.tvtafseer.setText(holder.itemView.getContext().getString(R.string.tafseer_not_down));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvtafseerAyah)
        TextView tvtafseerAyah;
        @BindView(R.id.tvtafseer)
        TextView tvtafseer;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}