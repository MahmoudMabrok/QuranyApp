package education.mahmoud.quranyapp.feature.show_tafseer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem;
import education.mahmoud.quranyapp.feature.download.DownloadActivity;

public class TafseerDetails extends Fragment {

    private static final String TAG = "TafseerDetails";

    @BindView(R.id.spSuraTafser)
    Spinner spSuraTafser;
    @BindView(R.id.spAyahTafser)
    Spinner spAyahTafser;
    @BindView(R.id.rvTafeer)
    RecyclerView rvTafeer;
    @BindView(R.id.tvNoDataInTafseer)
    TextView tvNoDataInTafseer;

    Unbinder unbinder;
    Repository repository;
    private SuraItem sura;
    private List<AyahItem> suraAyahsTafseer;
    private TafseerAdapter adapter;

    public static TafseerDetails newInstance(String title) {
        TafseerDetails frag = new TafseerDetails();
        return frag;
    }

    /**
     * used onCreateView when providing a custom UI for fragment
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tafseer_details, container, false);
        unbinder = ButterKnife.bind(this, view);
        repository = Repository.getInstance(getActivity().getApplication());
        fillSpinners();
        initRv();
        return view;
    }

    private void initRv() {
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        rvTafeer.setLayoutManager(manager);
        adapter = new TafseerAdapter();
        rvTafeer.setHasFixedSize(true);
        rvTafeer.setAdapter(adapter);
        Log.d(TAG, "initRv: ");
    }

    private void fillSpinners() {
        List<String> suraNames = Arrays.asList(Data.SURA_NAMES);
        ArrayAdapter<String> startAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, suraNames);
        spSuraTafser.setAdapter(startAdapter);
        spSuraTafser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String suraName = (String) spSuraTafser.getSelectedItem();
                suraAyahsTafseer = repository.getAllAyahOfSurahIndexForTafseer(l + 1);
                Log.d(TAG, "onItemSelected: " + suraAyahsTafseer.size() + " & " + l);
                // update adapter
                if (suraAyahsTafseer.size() > 0) {
                    foundState();
                    adapter.setTafseerList(suraAyahsTafseer);
                    Log.d(TAG, "onItemSelected: " + suraName);
                } else {
                    notFoundState();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void foundState() {
        rvTafeer.setVisibility(View.VISIBLE);
        tvNoDataInTafseer.setVisibility(View.GONE);
    }

    private void notFoundState() {
        rvTafeer.setVisibility(View.GONE);
        tvNoDataInTafseer.setVisibility(View.VISIBLE);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.tvNoDataInTafseer)
    public void onViewClicked() {
        Intent openAcivity = new Intent(getContext(), DownloadActivity.class);
        getContext().startActivity(openAcivity);

    }
}
