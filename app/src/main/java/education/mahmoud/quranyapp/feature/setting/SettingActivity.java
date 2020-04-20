package education.mahmoud.quranyapp.feature.setting;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.Repository;

public class SettingActivity extends AppCompatActivity {

    private static final String TAG = "SettingActivity";
    @BindView(R.id.cbNightMode)
    CheckBox cbNightMode;

    Repository repository;
    @BindView(R.id.spColorReqularMode)
    Spinner spColorReqularMode;
    @BindView(R.id.linearColor)
    LinearLayout linearColor;
    @BindView(R.id.btnSetColor)
    Button btnSetColor;


    int colorPosForBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        List<String> colorSet = new ArrayList<>(Arrays.asList(getString(R.string.white)
                , getString(R.string.yellow)
                , getString(R.string.green)));

        repository = Repository.getInstance(getApplication());

        cbNightMode.setChecked(repository.getNightModeState());
        if (cbNightMode.isChecked()) {
            nightMode();
        } else {
            defaultMode();
        }

        cbNightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean state) {
                Log.d(TAG, "onCheckedChanged: " + state);
                repository.setNightModeState(state);
                if (state) {
                    nightMode();
                } else {
                    defaultMode();
                }

                //// TODO: 6/18/2019 add Toast but handle it correctly
                //   showMessage(getString(R.string.setting_updated));
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, colorSet);
        spColorReqularMode.setAdapter(adapter);
        // load from shared preference and set to spinner.
        colorPosForBackground = repository.getBackColorState();
        spColorReqularMode.setSelection(colorPosForBackground);

        spColorReqularMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: " + i + ":: " + l);
                int pos = spColorReqularMode.getSelectedItemPosition();
                Log.d(TAG, "onItemSelected: pos " + pos);
                colorPosForBackground = pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        setTitle(getString(R.string.setting));

    }

    private void defaultMode() {
        linearColor.setVisibility(View.VISIBLE);
    }

    private void nightMode() {
        linearColor.setVisibility(View.GONE);
    }

    @OnClick(R.id.btnSetColor)
    public void onViewClicked() {
        repository.setBackColorState(colorPosForBackground);
        showMessage(getString(R.string.setting_updated));
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
