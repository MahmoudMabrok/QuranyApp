package education.mahmoud.quranyapp.feature.bookmark_fragment;


import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.BookmarkItem;
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowAyahsActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookmarkFragment extends Fragment {

    private static final String TAG = "BookmarkFragment";
    Repository repository;
    BookmarkAdapter bookmarkAdapter;
    @BindView(R.id.rvBookmark)
    RecyclerView rvBookmark;
    Unbinder unbinder;
    @BindView(R.id.tvNoBookMark)
    TextView tvNoBookMark;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        unbinder = ButterKnife.bind(this, view);
        repository = Repository.getInstance(getActivity().getApplication());
        initRv();
        retriveBookmarks();

        return view;
    }

    private void initRv() {

        bookmarkAdapter = new BookmarkAdapter();
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        rvBookmark.setAdapter(bookmarkAdapter);
        rvBookmark.setLayoutManager(manager);

        bookmarkAdapter.setIoBookmark(new BookmarkAdapter.IOBookmark() {
            @Override
            public void onBookmarkClick(BookmarkItem item) {
                Intent openAcivity = new Intent(getContext(), ShowAyahsActivity.class);
                ;
                openAcivity.putExtra(Constants.PAGE_INDEX, item.getPageNum());
                startActivity(openAcivity);
            }
        });

        bookmarkAdapter.setIoBookmarkDelete(new BookmarkAdapter.IOBookmarkDelete() {
            @Override
            public void onBookmarkClick(BookmarkItem item) {
                repository.deleteBookmark(item);
                bookmarkAdapter.deleteItem(item);
                Toast.makeText(getContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && rvBookmark != null) {
            if (bookmarkAdapter != null && repository != null) {
                bookmarkAdapter.setBookmarkItemList(repository.getBookmarks());
                Log.d(TAG, "setUserVisibleHint: " + bookmarkAdapter.getItemCount());
                if (bookmarkAdapter.getItemCount() > 0) {
                    availbaleData();
                } else {
                    Log.d(TAG, "setUserVisibleHint:  no data");
                    noData();
                }
            }
        }
    }

    private void noData() {
        rvBookmark.setVisibility(View.GONE);
        tvNoBookMark.setVisibility(View.VISIBLE);
    }

    private void availbaleData() {
        rvBookmark.setVisibility(View.VISIBLE);
        tvNoBookMark.setVisibility(View.GONE);
    }

    private int getIndexOfString(String suraName) {
        List<String> list = new ArrayList<>(Arrays.asList(Data.SURA_NAMES));
        return list.indexOf(suraName);
    }

    private void retriveBookmarks() {
        bookmarkAdapter.setBookmarkItemList(repository.getBookmarks());
        if (bookmarkAdapter.getItemCount() > 0) {
            availbaleData();
        } else {
            noData();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
