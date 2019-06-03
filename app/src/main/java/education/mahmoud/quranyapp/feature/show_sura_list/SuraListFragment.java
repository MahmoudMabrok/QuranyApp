package education.mahmoud.quranyapp.feature.show_sura_list;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem;
import education.mahmoud.quranyapp.feature.download.DownloadActivity;
import education.mahmoud.quranyapp.feature.home_Activity.HomeActivity;
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowAyahsActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class SuraListFragment extends Fragment {

    private static final String TAG = "SuraListFragment";

    @BindView(R.id.rvSura)
    RecyclerView rvSura;
    @BindView(R.id.spShowAyahs)
    SpinKitView spShowAyahs;
    @BindView(R.id.tv_no_quran_data)
    TextView tvNoQuranData;

    Handler handler;
    List<SuraItem> suraItems = new ArrayList<>();
    SuraAdapter suraAdapter;
    Unbinder unbinder;
    Repository repository;
    private Dialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sura_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        repository = Repository.getInstance(getActivity().getApplication());
        initRV();

        Log.d(TAG, "onCreateView: ");

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "handleMessage: ");
                super.handleMessage(msg);
                if (spShowAyahs != null) {
                    spShowAyahs.setVisibility(View.GONE);
                    if (suraItems.size() > 0) {
                        suraAdapter.setStringList(suraItems);
                        rvSura.setVisibility(View.VISIBLE);
                        tvNoQuranData.setVisibility(View.GONE);
                    } else {
                        rvSura.setVisibility(View.GONE);
                        tvNoQuranData.setVisibility(View.VISIBLE);
                    }
                }

            }
        };
        loadSuraList();
        return view;
    }

    private void initRV() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "me_quran.ttf");
        suraAdapter = new SuraAdapter(typeface);
        rvSura.setLayoutManager(linearLayoutManager);
        rvSura.setAdapter(suraAdapter);
        rvSura.setHasFixedSize(true);

        suraAdapter.setSuraListner(new SuraAdapter.SuraListner() {
            @Override
            public void onSura(int pos) {
                gotoSuraa(pos);
                Log.d(TAG, "onSura: " + pos);
            }
        });

    }


    private void loadSuraList() {
        Log.d(TAG, "loadSuraList: ");
        suraItems = repository.getSuras();
        handler.sendEmptyMessage(0);
        Log.d(TAG, "loadSuraList: ## " +suraItems.size());

    }

    private void gotoSuraa(int index) {
        Intent openAcivity = new Intent(getContext(), ShowAyahsActivity.class);
        openAcivity.putExtra(Constants.SURAH_INDEX, index);
        startActivity(openAcivity);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @OnClick(R.id.tv_no_quran_data)
    public void onViewClicked() {
        Intent openAcivity = new Intent(getContext(), DownloadActivity.class);
        getContext().startActivity(openAcivity);
    }

}
