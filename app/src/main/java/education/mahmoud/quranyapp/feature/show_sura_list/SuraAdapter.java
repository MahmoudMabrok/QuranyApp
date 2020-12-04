package education.mahmoud.quranyapp.feature.show_sura_list;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.datalayer.local.room.SuraItem;

public class SuraAdapter extends RecyclerView.Adapter<SuraAdapter.Holder> {


    private static final String TAG = "SuraAdapter";

    private SuraListner suraListner;
    private List<SuraItem> list;


    public SuraAdapter() {
        list = new ArrayList<>();
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

    @NotNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sura, viewGroup, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        SuraItem suraItem = list.get(i);
        holder.tvSuraName.setText(suraItem.getName());
        holder.tvSuraPageNum.setText(String.valueOf(suraItem.getStartIndex()));
        holder.tvSuraNumber.setText(String.valueOf(suraItem.getIndex()));

        String ayahSuffix = holder.itemView.getContext().getString(R.string.ayahSuffix);
        holder.tvSuraAyahsNum.setText(suraItem.getNumOfAyahs() + " " + ayahSuffix);

        if (suraItem.getRevelationType().equals("Meccan")) {
            holder.tvSuraRevolution.setText(holder.itemView.getContext().getString(R.string.sura_rev_mackkia));

        } else {
            holder.tvSuraRevolution.setText(holder.itemView.getContext().getString(R.string.sura_rev_madniaa));
        }

        if (i % 2 == 0) holder.changeToEven();
        else holder.changeToOdd();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    interface SuraListner {
        void onSura(int pos);
    }

    class Holder extends RecyclerView.ViewHolder {


        TextView tvSuraNumber;

        TextView tvSuraName;

        TextView tvSuraRevolution;

        TextView tvSuraAyahsNum;

        TextView tvSuraPageNum;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvSuraNumber = itemView.findViewById(R.id.tvSuraNumber);
            tvSuraName = itemView.findViewById(R.id.tvSuraName);
            tvSuraRevolution = itemView.findViewById(R.id.tvSuraRevolution);
            tvSuraAyahsNum = itemView.findViewById(R.id.tvSuraAyahsNum);
            tvSuraPageNum = itemView.findViewById(R.id.tvSuraPageNum);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    suraListner.onSura(list.get(pos).getIndex());
                }
            });
        }


        void changeToOdd() {
            itemView.setBackground(new ColorDrawable(Color.TRANSPARENT));
        }

        void changeToEven() {
            itemView.setBackground(new ColorDrawable(ContextCompat.getColor(itemView.getContext(), R.color.even_sura)));
        }
    }

}