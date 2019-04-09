package education.mahmoud.quranyapp.feature.ayahs_search;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
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
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.local.AyahItem;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.Holder> {

    private AyahListner ayahListner;
    private List<AyahItem> list;

    private String wordSearched;
    private Typeface typeface;

    public SearchResultsAdapter(Typeface typeface) {
        list = new ArrayList<>();
        this.typeface = typeface;
    }

    public void setSuraListner(AyahListner ayahListner) {
        this.ayahListner = ayahListner;
    }

    public void setAyahItemList(List<AyahItem> newList, String word) {
        wordSearched = word;
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
        Spannable ayahSpanned = Util.getSpanOfText(item.getTextClean(), wordSearched);

        holder.tvSearchResult.setText(ayahSpanned, TextView.BufferType.SPANNABLE);
        holder.tvSearchAyahNum.setText("" + item.getAyahInSurahIndex());
        holder.tvSearchSuraName.setText("" + Data.SURA_NAMES[item.getSurahIndex() - 1]);

        holder.tvSearchResult.setTypeface(typeface);
        holder.tvSearchSuraName.setTypeface(typeface);
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