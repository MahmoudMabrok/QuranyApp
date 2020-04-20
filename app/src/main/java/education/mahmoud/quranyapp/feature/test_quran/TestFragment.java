package education.mahmoud.quranyapp.feature.test_quran;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem;
import education.mahmoud.quranyapp.feature.test_sound.TestSoundActivity;
import education.mahmoud.quranyapp.utils.Data;
import education.mahmoud.quranyapp.utils.Util;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestFragment extends Fragment {

    private static final String TAG = "TestFragment";

    @BindView(R.id.spStartSura)
    Spinner spStartSura;
    @BindView(R.id.edStartSuraAyah)
    TextInputEditText edStartSuraAyah;
    @BindView(R.id.spEndSura)
    Spinner spEndSura;
    @BindView(R.id.edEndSuraAyah)
    TextInputEditText edEndSuraAyah;
    @BindView(R.id.btnTestSave)
    Button btnTestSave;
    @BindView(R.id.btnTestSaveRandom)
    Button btnTestSaveRandom;
    @BindView(R.id.lnSelectorAyahs)
    LinearLayout lnSelectorAyahs;
    @BindView(R.id.tvAyahToTestAfter)
    TextView tvAyahToTestAfter;
    @BindView(R.id.rvTestText)
    RecyclerView rvTestText;
    @BindView(R.id.lnTestLayout)
    LinearLayout lnTestLayout;
    @BindView(R.id.tvTestRange)
    TextView tvTestRange;

    Activity activity;
    Unbinder unbinder;
    SaveTestAdapter adapter = new SaveTestAdapter();
    @BindView(R.id.btnOpenTestSound)
    Button btnOpenTestSound;
    @BindView(R.id.edUserTextForAyahs)
    EditText edUserTextForAyahs;
    @BindView(R.id.btnCheckTest)
    Button btnCheckTest;
    private Repository repository;
    private SuraItem startSura;
    private SuraItem endSura;
    private int actualStart;
    private int actualEnd;
    private List<AyahItem> ayahsToTest;
    private boolean isInputValid;
    private int start;
    private int end;
    // Ayahs used to be compared with user input
    private AyahItem ayahItemTobeTest;

    private boolean isFullTest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for getContext() fragment
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        activity = getActivity();
        repository = Repository.getInstance(activity.getApplication());
        unbinder = ButterKnife.bind(this, view);

        initSpinners();
        initRV();
        return view;
    }

    /**
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && lnSelectorAyahs != null) {
            selectionState();
        }
    }

    /**
     * used to initialize the recyclerview
     */
    private void initRV() {
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        rvTestText.setLayoutManager(manager);
        rvTestText.setAdapter(adapter);
        rvTestText.setHasFixedSize(true);

        adapter.setiOnTestClick(new SaveTestAdapter.IOnTestClick() {
            @Override
            public void onClickTestCheck(AyahItem item, TextInputEditText editText) {
                // processing of user text
                String ayah = editText.getText().toString();
                Spannable spannable = Util.getDiffSpannaled(item.getTextClean(), ayah);
                updateTotalScore(Util.getTotalScore()); // Util.getTotalScore() -> score for yours Save test
                editText.setText(spannable, TextView.BufferType.SPANNABLE);
            }
        });

    }

    /**
     * called after finish test and used to update score in db and show score for current test
     *
     * @param totalScore
     */
    private void updateTotalScore(long totalScore) {
        long cuurentTotalScore = repository.getScore(); // saved score
        cuurentTotalScore += totalScore; // update with new score
        repository.setScore(cuurentTotalScore); // set in db

        Util.getDialog(getContext(), String.valueOf(cuurentTotalScore), getString(R.string.score)).show();
    }


    //region spinners
    /**
     * load data to spinners from db
     */
    private void initSpinners() {
        List<String> suraNames = Arrays.asList(Data.SURA_NAMES);
        ArrayAdapter<String> startAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, suraNames);
        spStartSura.setAdapter(startAdapter);

        ArrayAdapter<String> endAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, suraNames);
        spEndSura.setAdapter(endAdapter);

        spStartSura.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                startSura = repository.getSuraByIndex(l + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spEndSura.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                endSura = repository.getSuraByIndex(l + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
    //endregion


    /**
     * called when btn is clicked it first check inputs then load ayahs from db
     */
    @OnClick(R.id.btnTestSave)
    public void onViewClicked() {
        checkInput();
        if (isInputValid) {
            ayahsToTest = repository.getAyahSInRange(actualStart + 1, actualEnd + 1);
            // // TODO: 7/15/2019 make list
            adapter.setAyahItemList(ayahsToTest);
            TestState();

            isFullTest = true;
        }
    }

    /**
     * check input of spinners and edit text if it valid a field is become true
     */
    private void checkInput() {
        //region check inputs
        if (startSura != null && endSura != null) {
            try {
                start = Integer.parseInt(edStartSuraAyah.getText().toString());
                if (start > startSura.getNumOfAyahs()) {
                    edStartSuraAyah.setError(getString(R.string.outofrange, startSura.getNumOfAyahs()));
                    return;
                }
                end = Integer.parseInt(edEndSuraAyah.getText().toString());
                if (end > endSura.getNumOfAyahs()) {
                    edEndSuraAyah.setError(getString(R.string.outofrange, endSura.getNumOfAyahs()));
                    return;
                }
                // compute actual start , -1 because first ayah is 0 not 1 as user enter
                actualStart = repository.getAyahByInSurahIndex(startSura.getIndex(), start).getAyahIndex() - 1;
                // compute actual end
                actualEnd = repository.getAyahByInSurahIndex(endSura.getIndex(), end).getAyahIndex() - 1;


                // check actualstart & actualEnd
                if (actualEnd < actualStart) {
                    makeRangeError();
                    return;
                }
                Log.d(TAG, "onViewClicked: actual " + actualStart + " " + actualEnd);
                // get ayas from db
                ayahsToTest = repository.getAyahSInRange(actualStart + 1, actualEnd + 1);
                isInputValid = true;
                // place data in UI
                tvTestRange.setText(getString(R.string.rangeoftest, startSura.getName(), start, endSura.getName(), end));
                // close keyboard
                closeKeyboard();

            } catch (NumberFormatException e) {
                makeRangeError();
                isInputValid = false;
            }

        } else {
            showMessage(getString(R.string.sura_select_error));
        }
        //endregion
    }

    /**
     * used to make test layout shown to screen and hide selections
     */
    private void TestState() {
        lnSelectorAyahs.setVisibility(View.GONE);
        lnTestLayout.setVisibility(View.VISIBLE);

        // used  only with random test
        tvAyahToTestAfter.setVisibility(View.GONE);
    }

    /**
     * used to make Selection layout shown to screen and hide test layouts
     */
    private void selectionState() {
        lnSelectorAyahs.setVisibility(View.VISIBLE);
        lnTestLayout.setVisibility(View.GONE);
        adapter.clear();

        // clear inputs1
        edEndSuraAyah.setText(null);
        edStartSuraAyah.setText(null);
        edEndSuraAyah.setError(null);
        edStartSuraAyah.setError(null);

    }

    /**
     * if range of ayahs is incorrect it raise error messages
     */
    private void makeRangeError() {
        edStartSuraAyah.setError(getString(R.string.start_range_error));
        showMessage(getString(R.string.start_range_error));

    }

    /**
     * close keyboard after user click to make screen free
     */
    private void closeKeyboard() {
        Util.hideInputKeyboard(getContext());
    }

    /**
     * show message by Toast
     *
     * @param message message to be shown
     */
    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    @OnClick(R.id.btnTestSaveRandom)
    public void onbtnTestSaveRandom() {
        checkInput();
        if (isInputValid) {
            ayahsToTest = repository.getAyahSInRange(actualStart + 1, actualEnd + 1);
            if (ayahsToTest.size() >= 3) {
                int r = new Random().nextInt(ayahsToTest.size() - 1);
                ayahItemTobeTest = ayahsToTest.get(r);
                tvAyahToTestAfter.setText(getString(R.string.ayahToTestRanom, ayahItemTobeTest.getTextClean()));
                ayahItemTobeTest = ayahsToTest.get(r + 1);
                ayahsToTest.clear();
                ayahsToTest.add(ayahItemTobeTest);
                adapter.setAyahItemList(ayahsToTest);

                // make list, used for Check
                ayahsToTest = new ArrayList<>();
                ayahsToTest.add(ayahItemTobeTest);

                TestRandomState();

                isFullTest = false;
            } else {
                ayahsNotSufficentError();
            }
        }

    }

    private void TestRandomState() {
        tvAyahToTestAfter.setVisibility(View.VISIBLE);
        lnSelectorAyahs.setVisibility(View.GONE);
        lnTestLayout.setVisibility(View.VISIBLE);

        isFullTest = true;

    }

    private void ayahsNotSufficentError() {
        edStartSuraAyah.setError(getString(R.string.not_sufficient_ayahs));
        edEndSuraAyah.setError(getString(R.string.not_sufficient_ayahs));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btnCheckTest)
    public void onCheckClicked() {
        String ayah = edUserTextForAyahs.getText().toString();
        String ayahToTestStr = getAyah();
        Spannable spannable = Util.getDiffSpannaled(ayahToTestStr, ayah);
        updateTotalScore(Util.getTotalScore()); // Util.getTotalScore() -> score for yours Save test
        edUserTextForAyahs.setText(spannable, TextView.BufferType.SPANNABLE);

    }

    private String getAyah() {
        if (isFullTest) {
            return concateAyahs();
        } else {
            return ayahsToTest.get(0).getTextClean();
        }

    }

    private String concateAyahs() {
        StringBuilder builder = new StringBuilder();
        for (AyahItem ayahItem : ayahsToTest) {
            builder.append(ayahItem.getTextClean());
        }
        return builder.toString();
    }

    @OnClick(R.id.btnOpenTestSound)
    public void onOpenTestSoundClicked() {
        Intent openAcivity = new Intent(getContext(), TestSoundActivity.class);
        startActivity(openAcivity);
    }
}
