package education.mahmoud.quranyapp.feature.ayahs_search;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.Holder> {


    IOnPlay iOnPlay;
    private List<AyahItem> list;
    private String wordSearched;
    private Typeface typeface;

    public SearchResultsAdapter(Typeface typeface) {
        list = new ArrayList<>();
        this.typeface = typeface;
    }

    public void setiOnPlay(IOnPlay iOnPlay) {
        this.iOnPlay = iOnPlay;
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
    public void onBindViewHolder(@NonNull final Holder holder, int i) {
        final AyahItem item = list.get(i);
        Spannable ayahSpanned = Util.getSpanOfText(item.getTextClean(), wordSearched);

        holder.tvSearchResult.setText(ayahSpanned, TextView.BufferType.SPANNABLE);
        holder.tvSearchAyahNum.setText("" + item.getAyahInSurahIndex());
        holder.tvSearchSuraName.setText("" + Data.SURA_NAMES[item.getSurahIndex() - 1]);

        holder.tvSearchResult.setTypeface(typeface);
        holder.tvSearchSuraName.setTypeface(typeface);

        holder.btnShowTashkeel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.tvSearchResult.setText(item.getText());
            }
        });

        holder.btnShowTafseer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.getTafseer() != null) {
                    String title = holder.itemView.getContext().
                            getString(R.string.tafserr_info, item.getAyahInSurahIndex(), item.getPageNum(), item.getJuz());
                    Util.getDialog(holder.itemView.getContext(), item.getTafseer(), title).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), holder.itemView.getContext().getText(R.string.tafseer_not_down), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public int getItemIndex(AyahItem ayahItem) {
        return list.indexOf(ayahItem);
    }

    public void updateItem(AyahItem ayahItem, int itemIndex) {
        list.set(itemIndex, ayahItem);
        notifyDataSetChanged();
    }

    interface IOnPlay {
        void onPlayClick(AyahItem item);
    }


    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvSearchSuraName)
        TextView tvSearchSuraName;
        @BindView(R.id.tvSearchAyahNum)
        TextView tvSearchAyahNum;
        @BindView(R.id.tvSearchResult)
        TextView tvSearchResult;
        @BindView(R.id.btnPlayInSearch)
        ImageButton btnPlayInSearch;
        @BindView(R.id.btnShowTafseer)
        Button btnShowTafseer;
        @BindView(R.id.btnShowTashkeel)
        Button btnShowTashkeel;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            btnPlayInSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    iOnPlay.onPlayClick(list.get(getAdapterPosition()));
                }
            });

        }
    }

}