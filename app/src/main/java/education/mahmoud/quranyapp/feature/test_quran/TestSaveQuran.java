package education.mahmoud.quranyapp.feature.test_quran;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Util;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem;

public class TestSaveQuran extends AppCompatActivity implements SaveTestAdapter.IOnTestClick {

    private static final String TAG = "TestSaveQuran";

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
    @BindView(R.id.lnSelectorAyahs)
    LinearLayout lnSelectorAyahs;
    @BindView(R.id.rvTestText)
    RecyclerView rvTestText;
    @BindView(R.id.lnTestLayout)
    LinearLayout lnTestLayout;
    @BindView(R.id.btnTestSaveRandom)
    Button btnTestSaveRandom;
    @BindView(R.id.tvAyahToTestAfter)
    TextView tvAyahToTestAfter;
    SaveTestAdapter adapter = new SaveTestAdapter();
    private Repository repository;
    private SuraItem startSura;
    private SuraItem endSura;
    private int actualStart;
    private int actualEnd;
    private List<AyahItem> ayahsToTest;
    private boolean isInputValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_save_quran);
        ButterKnife.bind(this);

        repository = Repository.getInstance(getApplication());

        initSpinners();

        initRV();
    }

    /**
     * load data to spinners from db
     */
    private void initSpinners() {
        List<String> suraNames = repository.getSurasNames();

        ArrayAdapter<String> startAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suraNames);
        spStartSura.setAdapter(startAdapter);

        ArrayAdapter<String> endAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suraNames);
        spEndSura.setAdapter(endAdapter);

        spStartSura.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sura = (String) spStartSura.getSelectedItem();
                startSura = repository.getSuraByName(sura);
                Log.d(TAG, "onItemSelected: " + sura);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spEndSura.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sura = (String) spEndSura.getSelectedItem();
                endSura = repository.getSuraByName(sura);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    /**
     * used to initialize the recyclerview
     */
    private void initRV() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rvTestText.setLayoutManager(manager);
        rvTestText.setAdapter(adapter);

        adapter.setiOnTestClick(this);

    }

    /**
     * called when btn is clicked it first check inputs then load ayahs from db
     */
    @OnClick(R.id.btnTestSave)
    public void onViewClicked() {
        checkInput();
        if (isInputValid) {
            ayahsToTest = repository.getAyahSInRange(actualStart, actualEnd);
            adapter.setAyahItemList(ayahsToTest);

            TestState();
        }
    }

    /**
     * check input of spinners and edit text if it valid a field is become true
     */
    private void checkInput() {
        //region check inputs
        if (startSura != null && endSura != null) {
            try {
                int start = Integer.parseInt(edStartSuraAyah.getText().toString());
                if (start > startSura.getNumOfAyahs()) {
                    edStartSuraAyah.setError(getString(R.string.outofrange, startSura.getNumOfAyahs()));
                    return;
                }
                int end = Integer.parseInt(edEndSuraAyah.getText().toString());
                if (end > endSura.getNumOfAyahs()) {
                    edEndSuraAyah.setError(getString(R.string.outofrange, endSura.getNumOfAyahs()));
                    return;
                }
                // compute actual start
                actualStart = startSura.getStartIndex() + start - 1;
                // compute actual end
                actualEnd = endSura.getStartIndex() + end - 1;

                // check actualstart & actualEnd
                if (actualEnd < actualStart) {
                    makeRangeError();
                    return;
                }
                Log.d(TAG, "onViewClicked: actual " + actualStart + " " + actualEnd);

                // get ayas from db
                ayahsToTest = repository.getAyahSInRange(actualStart, actualEnd);

                isInputValid = true;

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
    }

    /**
     * if range of ayahs is incorrect it raise error messages
     */
    private void makeRangeError() {
        edStartSuraAyah.setError("Start must be before end ");
        edEndSuraAyah.setError("End must be after start");
    }

    /**
     * close keyboard after user click to make screen free
     */
    private void closeKeyboard() {
        Util.hideInputKeyboard(this);
    }

    /**
     * show message by Toast
     *
     * @param message message to be shown
     */
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * when click back if test state which means user test him self
     * and click back to return to selection
     */
    @Override
    public void onBackPressed() {
        if (lnTestLayout.getVisibility() == View.VISIBLE) {
            selectionState();
        } else {
            finish();
        }
    }

    /**
     * handle and check text input and compare with ayah text to check save
     *
     * @param item
     * @param editText
     */
    @Override
    public void onClickTestCheck(AyahItem item, TextInputEditText editText) {
        String ayah = editText.getText().toString();
        Spannable spannable = Util.getDiffSpannaled(item.getTextClean(), ayah);
        editText.setText(spannable, TextView.BufferType.SPANNABLE);
    }

    @OnClick(R.id.btnTestSaveRandom)
    public void onbtnTestSaveRandom() {
        checkInput();
        if (isInputValid) {
            ayahsToTest = repository.getAyahSInRange(actualStart, actualEnd);
            if (ayahsToTest.size() >= 3) {
                int r = new Random().nextInt(ayahsToTest.size() - 1);
                AyahItem ayahItem = ayahsToTest.get(r);
                tvAyahToTestAfter.setText(getString(R.string.ayahToTestRanom, ayahItem.getTextClean()));
                showMessage("s " + ayahItem.getAyahIndex());
                ayahItem = ayahsToTest.get(r + 1);
                ayahsToTest.clear();
                ayahsToTest.add(ayahItem);
                adapter.setAyahItemList(ayahsToTest);
                TestRandomState();
            } else {
                ayahsNotSufficentError();
            }
        }

    }

    private void TestRandomState() {
        tvAyahToTestAfter.setVisibility(View.VISIBLE);
        lnSelectorAyahs.setVisibility(View.GONE);
        lnTestLayout.setVisibility(View.VISIBLE);
    }

    private void ayahsNotSufficentError() {
        edStartSuraAyah.setError(getString(R.string.not_sufficient_ayahs));
        edEndSuraAyah.setError(getString(R.string.not_sufficient_ayahs));

    }
}
