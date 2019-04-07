package education.mahmoud.quranyapp.feature.ayahs_search;

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
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.data_layer.local.AyahItem;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.Holder> {

    private AyahListner ayahListner;
    private List<AyahItem> list;

    public SearchResultsAdapter() {
        list = new ArrayList<>();
    }

    public void setSuraListner(AyahListner ayahListner) {
        this.ayahListner = ayahListner;
    }

    public void setAyahItemList(List<AyahItem> newList) {
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

    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_result_item, viewGroup, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        AyahItem item = list.get(i);
        holder.tvSearchResult.setText(item.getText());
        holder.tvSearchAyahNum.setText("" + item.getAyahInSurahIndex());
        holder.tvSearchSuraName.setText("" + Data.SURA_NAMES[item.getSurahIndex() - 1]);


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    interface AyahListner {
        void onSura(int pos);
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvSearchSuraName)
        TextView tvSearchSuraName;
        @BindView(R.id.tvSearchAyahNum)
        TextView tvSearchAyahNum;
        @BindView(R.id.tvSearchResult)
        TextView tvSearchResult;


        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
           /* itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    ayahListner.onSura(pos);
                }
            });*/

        }
    }

}