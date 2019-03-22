package education.mahmoud.quranyapp.feature.show_sura_list;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.Util.Data;
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowSuarhAyas;

public class GoToSurah extends DialogFragment {

    Unbinder unbinder;

    String tableNum;
    @BindView(R.id.edSurahNum)
    EditText edSurahNum;
    @BindView(R.id.btnGoToSura)
    Button btnGoToSura;
    @BindView(R.id.edSurahName)
    EditText edSurahName;
    @BindView(R.id.btnGoToSuraByName)
    Button btnGoToSuraByName;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.go_to_sura, container);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btnGoToSura)
    public void onViewClicked() {
        int index = Integer.parseInt(edSurahNum.getText().toString());
        index--;
        if (index < 0 || index >= 114) {
            edSurahNum.setError("Number must be 1-114");
            return;
        }
        goToSura(index);
    }

    @OnClick(R.id.btnGoToSuraByName)
    public void onViewBtnname() {
        String name = edSurahName.getText().toString();
        List<String> suraList = new ArrayList<>(Arrays.asList(Data.SURA_NAMES));
        int index = suraList.indexOf(name);

        if (index == -1) {
            edSurahName.setError("Enter Valid Name");
            return;
        }
        goToSura(index);
    }

    private void goToSura(int index) {
        Intent intent = new Intent(getActivity(), ShowSuarhAyas.class);
        intent.putExtra(Constants.SURAH_INDEX, index);
        startActivity(intent);
        dismiss();
    }
}
