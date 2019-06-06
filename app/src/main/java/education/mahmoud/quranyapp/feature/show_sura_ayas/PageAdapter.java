package education.mahmoud.quranyapp.feature.show_sura_ayas;

import android.graphics.Typeface;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;

public class PageAdapter extends RecyclerView.Adapter<PageAdapter.Holder> {

    private static final String TAG = "PageAdapter";

    int ayahsColor, scrollColor;
    int vis = View.INVISIBLE;
    private List<Page> list = new ArrayList<>();
    private Typeface typeface;
    private IOnClick iOnClick;
    private PageShown pageShown;
    private IBookmark iBookmark;

    public PageAdapter(Typeface typeface, int ayahsColor, int scrollColor) {
        this.typeface = typeface;
        this.ayahsColor = ayahsColor;
        this.scrollColor = scrollColor;
    }

    public void setPageShown(PageShown pageShown) {
        this.pageShown = pageShown;
    }

    public void setiBookmark(IBookmark iBookmark) {
        this.iBookmark = iBookmark;
    }

    public void setiOnClick(IOnClick iOnClick) {
        this.iOnClick = iOnClick;
    }

    public void setPageList(List<Page> newList) {
        list = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public List<Page> getList() {
        return list;
    }

    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.page_item, viewGroup, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int index) {
        Page item = list.get(index);

        holder.topLinear.setVisibility(View.VISIBLE);
        holder.BottomLinear.setVisibility(View.VISIBLE);

        // set Colors
        holder.tvAyahs.setTextColor(ayahsColor);
        holder.scAyahsText.setBackgroundColor(scrollColor);

        //<editor-fold desc="Create Text">
        StringBuilder builder = new StringBuilder();
        String aya;
        String suraName = "";
        suraName = getSuraNameFromIndex(item.getAyahItems().get(0).getSurahIndex());

        String tempSuraName;
        boolean isFirst = true;
        for (AyahItem ayahItem : item.getAyahItems()) {
            aya = ayahItem.getText();
            // add sura name
            if (ayahItem.getAyahInSurahIndex() == 1) {
                tempSuraName = getSuraNameFromIndex(ayahItem.getSurahIndex());
                if (isFirst) {
                    // handle first name in page that not need a previous new line
                    builder.append(tempSuraName + "\n");
                } else {
                    builder.append("\n" + tempSuraName + "\n");
                }

                // AlFatiha(index = 1 ) has a Basmallah in first ayah.
                if (ayahItem.getSurahIndex() != 1) {
                    int pos = aya.indexOf("ٱلرَّحِيم");
                    Log.d(TAG, "onBindViewHolder: pos " + pos);
                    pos += (new String("ٱلرَّحِيم").length());
                    Log.d(TAG, "onBindViewHolder: last text after bsmallah " + aya.substring(pos));
                    // insert  البسملة
                    builder.append(aya.substring(0, pos + 1)); // +1 as substring upper bound is excluded
                    builder.append("\n");
                    // cute ayah
                    aya = aya.substring(pos+1); // +1 to start with new character after البسملة
                }
            }
            isFirst = false;
            builder.append(MessageFormat.format("{0} ﴿ {1} ﴾ ", aya, ayahItem.getAyahInSurahIndex()));

        }
        //</editor-fold>

        holder.tvAyahs.setText(Util.getSpannable(builder.toString()), TextView.BufferType.SPANNABLE);
        holder.tvAyahs.setTypeface(typeface);

        // text justifivation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.tvAyahs.setJustificationMode(Layout.JUSTIFICATION_MODE_NONE);
        }

        // top - bottom details
        holder.tvPageNumShowAyahs.setText(String.valueOf(item.getPageNum()));
        holder.tvSurahName.setText(suraName);
        holder.tvJuz.setText(String.valueOf(item.getJuz()));

        //<editor-fold desc="listeners">
        holder.imBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iBookmark.onBookmarkClicked(item);
            }
        });

        holder.ayahsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: frameLayout");
                flipState(holder);
            }
        });

        holder.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    iOnClick.onClick(holder.getAdapterPosition());
            }
        });

        holder.btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    iOnClick.onClick(holder.getAdapterPosition() - 2); // there will be update by one
            }
        });
        //</editor-fold>

        //<editor-fold desc="configure next/prev buttons">
        if (index == 0 ){
            holder.btnNext.setVisibility(View.VISIBLE);
            holder.btnPrev.setVisibility(View.INVISIBLE);
        }else if (index == 603 ){
            holder.btnNext.setVisibility(View.INVISIBLE);
            holder.btnPrev.setVisibility(View.VISIBLE);
        }else{
            holder.btnNext.setVisibility(View.VISIBLE);
            holder.btnPrev.setVisibility(View.VISIBLE);
        }
        //</editor-fold>


    }

    private void flipState(Holder holder) {
        vis = holder.topLinear.getVisibility();
        vis = vis == View.VISIBLE ? View.INVISIBLE : View.VISIBLE;
        holder.BottomLinear.setVisibility(vis);
        holder.topLinear.setVisibility(vis);
    }

    /**
     * @param surahIndex in quran
     * @return
     */
    private String getSuraNameFromIndex(int surahIndex) {
        return Data.SURA_NAMES[surahIndex - 1];
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull Holder holder) {
        super.onViewAttachedToWindow(holder);


        /* //<editor-fold desc="timer to hide">
        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                vis = View.VISIBLE;
                holder.BottomLinear.setVisibility(vis);
                holder.topLinear.setVisibility(vis);
            }
        }.start();
        //</editor-fold>*/

        pageShown.onDiplayed(holder.getAdapterPosition(), holder);
    }

    public Page getPage(int pos) {
        return list.get(pos);
    }

    interface PageShown {
        void onDiplayed(int pos, Holder holder);
    }

    interface IBookmark {
        void onBookmarkClicked(Page item);
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvAyahs)
        TextView tvAyahs;
        @BindView(R.id.sc_ayahs_text)
        ScrollView scAyahsText;
        @BindView(R.id.tvSurahName)
        TextView tvSurahName;
        @BindView(R.id.tvJuz)
        TextView tvJuz;
        @BindView(R.id.imBookmark)
        ImageView imBookmark;
        @BindView(R.id.topLinear)
        LinearLayout topLinear;
        @BindView(R.id.btnNext)
        ImageButton btnNext;
        @BindView(R.id.tvPageNumShowAyahs)
        TextView tvPageNumShowAyahs;
        @BindView(R.id.btnPrev)
        ImageButton btnPrev;
        @BindView(R.id.BottomLinear)
        LinearLayout BottomLinear;
        @BindView(R.id.ayahsLayout)
        FrameLayout ayahsLayout;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}