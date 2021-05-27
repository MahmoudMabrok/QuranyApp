package education.mahmoud.quranyapp.feature.show_sura_ayas;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;

public class AyahsAdapter extends RecyclerView.Adapter<AyahsAdapter.Holder> {

    private List<AyahItem> list;
    private Typeface typeface;

    public AyahsAdapter(Typeface typeface) {
        list = new ArrayList<>();
        this.typeface = typeface;
    }


    public void setAyahItemList(List<AyahItem> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public List<AyahItem> getList() {
        return list;
    }

    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ayah_single_item, viewGroup, false);
        return new Holder(view, typeface);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int i) {
        holder.bind(list.get(i));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public int getItemIndex(AyahItem ayahItem) {
        return list.indexOf(ayahItem);
    }


    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvAyah)
        TextView tvAyah;

        public Holder(@NonNull View itemView, Typeface typeface) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            tvAyah.setTypeface(typeface);

        }

        public void bind(AyahItem item) {
            tvAyah.setText(item.getText() + "(" + (item.getAyahInSurahIndex()) + ")");
        }
    }

}