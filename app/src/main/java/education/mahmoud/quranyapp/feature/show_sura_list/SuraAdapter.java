package education.mahmoud.quranyapp.feature.show_sura_list;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem;

public class SuraAdapter extends RecyclerView.Adapter<SuraAdapter.Holder> {


    private static final String TAG = "SuraAdapter";

    private SuraListner suraListner;
    private List<SuraItem> list;

    private Typeface typeface;

    public SuraAdapter() {
        list = new ArrayList<>();
    }

    public SuraAdapter(Typeface typeface) {
        this.typeface = typeface;
    }

    public void setSuraListner(SuraListner suraListner) {
        this.suraListner = suraListner;
    }

    public void setStringList(List<SuraItem> newList) {
        list = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public List<SuraItem> getList() {
        return list;
    }

    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sura_item, viewGroup, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        SuraItem suraItem = list.get(i);
        holder.tvSuraName.setText(suraItem.getName());
        holder.tvSuraPageNum.setText(String.valueOf(suraItem.getStartIndex()));
        holder.tvSuraName.setTypeface(typeface);
        holder.tvSuraNumber.setText(String.valueOf(suraItem.getIndex()));

        String ayahSuffix = holder.itemView.getContext().getString(R.string.ayahSuffix);
        holder.tvSuraAyahsNum.setText(suraItem.getNumOfAyahs() + ayahSuffix);

        if (suraItem.getRevelationType().equals("Meccan")) {
            holder.tvSuraRevolution.setText(holder.itemView.getContext().getString(R.string.sura_rev_mackkia));
        } else {
            holder.tvSuraRevolution.setText(holder.itemView.getContext().getString(R.string.sura_rev_madniaa));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    interface SuraListner {
        void onSura(int pos);
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvSuraNumber)
        TextView tvSuraNumber;
        @BindView(R.id.tvSuraName)
        TextView tvSuraName;
        @BindView(R.id.tvSuraRevolution)
        TextView tvSuraRevolution;
        @BindView(R.id.tvSuraAyahsNum)
        TextView tvSuraAyahsNum;
        @BindView(R.id.tvSuraPageNum)
        TextView tvSuraPageNum;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    suraListner.onSura(list.get(pos).getIndex());
                }
            });
        }
    }

}