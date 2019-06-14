package education.mahmoud.quranyapp.feature.bookmark_fragment;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.local.room.BookmarkItem;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.Holder> {

    private IOBookmark ioBookmark;
    private IOBookmarkDelete ioBookmarkDelete;

    private List<BookmarkItem> list;

    public BookmarkAdapter() {
        list = new ArrayList<>();
    }

    public void addBookmark(BookmarkItem item) {
        list.add(item);
        notifyItemInserted(list.size() - 1);
        notifyItemRangeChanged(list.size() - 1, list.size());
    }

    public void setIoBookmarkDelete(IOBookmarkDelete ioBookmarkDelete) {
        this.ioBookmarkDelete = ioBookmarkDelete;
    }

    public void setIoBookmark(IOBookmark ioBookmark) {
        this.ioBookmark = ioBookmark
        ;
    }

    public void setBookmarkItemList(List<BookmarkItem> newList) {
        list = new ArrayList<>(newList);
        notifyDataSetChanged();
    }


    public List<BookmarkItem> getList() {
        return list;
    }

    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bookmark_item, viewGroup, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        final BookmarkItem item = list.get(i);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ioBookmark.onBookmarkClick(item);
            }
        });

        holder.tvIndexBookmark.setText(String.valueOf(item.getPageNum()));
        holder.tvSuraNameBookmark.setText(item.getSuraName());

        holder.imDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ioBookmarkDelete.onBookmarkClick(item);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void deleteItem(BookmarkItem item) {
        //// TODO: 4/16/2019  animation for delete
        /*int index = list.indexOf(item);*/
        list.remove(item);
        notifyDataSetChanged();

    }

    interface IOBookmark {
        void onBookmarkClick(BookmarkItem item);
    }


    interface IOBookmarkDelete {
        void onBookmarkClick(BookmarkItem item);
    }

    class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvIndexBookmark)
        TextView tvIndexBookmark;
        @BindView(R.id.tvSuraNameBookmark)
        TextView tvSuraNameBookmark;

        @BindView(R.id.imDeleteBookmark)
        ImageView imDelete;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}