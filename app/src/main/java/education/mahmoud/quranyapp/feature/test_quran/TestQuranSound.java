package education.mahmoud.quranyapp.feature.test_quran;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import education.mahmoud.quranyapp.R;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestQuranSound extends Fragment {


    private static final String TAG = "TestQuranSound";
    private static final int REQ_CODE_VOICE = 54445;
    @BindView(R.id.tvRecongnizedText)
    TextView tvRecongnizedText;
    @BindView(R.id.imRecord)
    ImageView imRecord;

    Handler handler;
    String reco = "NA";
    @BindView(R.id.aaa)
    TextView aaa;
    @BindView(R.id.btnAA)
    Button btnAA;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_test_quran_sound, container, false);
        ButterKnife.bind(this, view);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                tvRecongnizedText.setText(reco);
                Log.d(TAG, "handleMessage: " + reco);
                try {
                    aaa.setText(String.format("aaa %s", reco));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                btnAA.setText(reco);

                //   Toast.makeText(getContext(), reco, Toast.LENGTH_SHORT).show();
            }
        };

        WeakReference<Handler> handlerWeakReference = new WeakReference<>(handler);

        tvRecongnizedText.setText("WA");

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler = null;
    }

    @OnClick(R.id.imRecord)
    public void onViewClicked() {

        tvRecongnizedText.setText("rrrrr");

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // LANGUAGE_MODEL_FREE_FORM for speech recongition
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // set Language
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        // hint for speek
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speek now...");
        try {
            startActivityForResult(intent, REQ_CODE_VOICE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_VOICE:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    // results are ordered based on confidence score, in descending order.
                    StringBuilder stringBuilder = new StringBuilder();
                    Log.d(TAG, "onActivityResult: ");
                    try {
                        for (String s : results) {
                            stringBuilder.append(s);
                            Log.d(TAG, "onActivityResult: !! " + s);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    reco = results.get(0);
                    handler.sendEmptyMessage(0);
                }
        }
    }

}
