package education.mahmoud.quranyapp.feature.test_sound;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.Repository;
import education.mahmoud.quranyapp.data_layer.local.room.RecordItem;
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem;
import education.mahmoud.quranyapp.utils.Constants;
import education.mahmoud.quranyapp.utils.Data;
import education.mahmoud.quranyapp.utils.DateOperation;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment {


    @BindView(R.id.spStartSuraRecordFragment)
    Spinner spStartSuraRecordFragment;
    @BindView(R.id.edStartSuraAyahRecordFragment)
    TextInputEditText edStartSuraAyahRecordFragment;
    @BindView(R.id.spEndSuraRecordFragment)
    Spinner spEndSuraRecordFragment;
    @BindView(R.id.edEndSuraAyahRecordFragment)
    TextInputEditText edEndSuraAyahRecordFragment;
    @BindView(R.id.btnRecord)
    FloatingActionButton btnRecord;
    @BindView(R.id.chronometer)
    Chronometer chronometer;
    @BindView(R.id.layoutDurationRecordFragemt)
    FrameLayout layoutDurationRecordFragemt;


    private Repository repository;
    private SuraItem startSura;
    private SuraItem endSura;
    private int start;
    private int actualStart;
    private int actualEnd;
    private int end;
    private boolean isInputValid;
    private boolean isRecording;
    private Intent intent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        ButterKnife.bind(this, view);
        repository = Repository.getInstance(getActivity().getApplication());
        initSpinners();

        return view;
    }

    /**
     * load data to spinners from db
     */
    private void initSpinners() {
        List<String> suraNames = Arrays.asList(Data.SURA_NAMES);
        ArrayAdapter<String> startAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, suraNames);
        spStartSuraRecordFragment.setAdapter(startAdapter);

        ArrayAdapter<String> endAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, suraNames);
        spEndSuraRecordFragment.setAdapter(endAdapter);

        spStartSuraRecordFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                startSura = repository.getSuraByIndex(l + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spEndSuraRecordFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                endSura = repository.getSuraByIndex(l + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    /**
     * check input of spinners and edit text if it valid a field is become true
     */
    private void checkInput() {
        // intialize it
        isInputValid = false;
        //region check inputs
        if (startSura != null && endSura != null) {
            try {
                start = Integer.parseInt(edStartSuraAyahRecordFragment.getText().toString());
                if (start > startSura.getNumOfAyahs()) {
                    edStartSuraAyahRecordFragment.setError(getString(R.string.outofrange, startSura.getNumOfAyahs()));
                    return;
                }
                end = Integer.parseInt(edEndSuraAyahRecordFragment.getText().toString());
                if (end > endSura.getNumOfAyahs()) {
                    edEndSuraAyahRecordFragment.setError(getString(R.string.outofrange, endSura.getNumOfAyahs()));
                    return;
                }
                // compute actual start , -1 because first ayah is 0 not 1 as user enter
                actualStart = repository.getAyahByInSurahIndex(startSura.getIndex(), start).getAyahIndex();
                // compute actual end
                actualEnd = repository.getAyahByInSurahIndex(endSura.getIndex(), end).getAyahIndex();
                // check actualstart & actualEnd
                if (actualEnd < actualStart) {
                    makeRangeError();
                    return;
                }
                Log.d(TAG, "onViewClicked: actual " + actualStart + " " + actualEnd);
                isInputValid = true;
                // place data in UI
                //     tvTestRange.setText(getString(R.string.rangeoftest, startSura.getName(), start, endSura.getName(), end));
                // close keyboard
                //   closeKeyboard();

            } catch (NumberFormatException e) {
                makeRangeError();
                isInputValid = false;
            }

        } else {
            showMessage(getString(R.string.sura_select_error));
        }
        //endregion
    }

    private void makeRangeError() {
        edStartSuraAyahRecordFragment.setError(getString(R.string.start_range_error));
        showMessage(getString(R.string.start_range_error));

    }


    @OnClick(R.id.btnRecord)
    public void onViewClicked() {
        checkInput();
        Log.d(TAG, "onViewClicked: " + isInputValid + start + end);
        if (!isRecording && isInputValid) {

            int c = repository.getRecordCount() + 1;
            String fileName = c + "_" + DateOperation.getCurrentDateAsString();
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            String path = new File(folder, fileName + ".mp4").getAbsolutePath();
            RecordItem recordItem = new RecordItem(actualStart, actualEnd, fileName, path);
            // start service
            intent = new Intent(getActivity(), RecordingService.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.RECORD_ITEM, recordItem);
            intent.putExtras(bundle);
            getActivity().startService(intent);
            isRecording = true;

            // chromometer
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            recordState();
        } else if (isRecording) {
            // stop chromorter
            chronometer.stop();
            stopRecord();
        }
    }

    private void recordState() {
        Log.d(TAG, "recordState: ");
        btnRecord.setImageResource(R.drawable.ic_stop);
        showMessage(getString(R.string.start_Record));
        layoutDurationRecordFragemt.setVisibility(View.VISIBLE);
    }


    private void stopRecord() {
        Log.d(TAG, "stopRecord: ");
        btnRecord.setImageResource(R.drawable.ic_mic_white_36dp);
        isRecording = false;
        getActivity().stopService(intent);
        showMessage(getString(R.string.finish_Record));

        layoutDurationRecordFragemt.setVisibility(View.INVISIBLE);
    }


    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
