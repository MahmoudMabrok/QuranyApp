package education.mahmoud.quranyapp.feature.show_tafseer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.koin.java.KoinJavaComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem;
import education.mahmoud.quranyapp.feature.download.DownloadActivity;
import education.mahmoud.quranyapp.utils.Data;

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
    private Repository repository = KoinJavaComponent.get(Repository.class);
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
                   // Log.d(TAG, "onItemSelected: " + suraName);

                    List<String> ayahsNums = creatAyahsNumList(suraAyahsTafseer.size()); // size() ->  n of ayahs
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext() ,android.R.layout.simple_dropdown_item_1line,ayahsNums );
                    spAyahTafser.setAdapter(adapter);
                    spAyahTafser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            rvTafeer.scrollToPosition((int) l);
                            Log.d(TAG, "onItemSelected: TAFSEEER POS " + l);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                } else {
                    notFoundState();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private List<String> creatAyahsNumList(int i) {
        List<String> ayahsNums = new ArrayList<>();
        for (int j = 1; j <= i  ; j++) {
            ayahsNums.add(String.valueOf(j));
        }
        return ayahsNums;
    }

    private void foundState() {
        rvTafeer.setVisibility(View.VISIBLE);
        spSuraTafser.setVisibility(View.VISIBLE);
        spAyahTafser.setVisibility(View.VISIBLE);
        tvNoDataInTafseer.setVisibility(View.GONE);
    }

    private void notFoundState() {
        rvTafeer.setVisibility(View.GONE);
        spSuraTafser.setVisibility(View.INVISIBLE);
        spAyahTafser.setVisibility(View.INVISIBLE);
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
