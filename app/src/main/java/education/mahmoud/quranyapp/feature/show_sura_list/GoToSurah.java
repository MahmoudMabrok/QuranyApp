package education.mahmoud.quranyapp.feature.show_sura_list;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Constants;
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowAyahsActivity;

public class GoToSurah extends DialogFragment {


    Unbinder unbinder;
    String tableNum;
    @BindView(R.id.edSurahNum)
    EditText edSurahNum;
    @BindView(R.id.edSurahName)
    EditText edSurahName;
    @BindView(R.id.btnGo)
    Button btnGo;
    @BindView(R.id.edJuz)
    EditText edJuz;
    @BindView(R.id.btnGoToJuz)
    Button btnGoToJuz;
    @BindView(R.id.edSurahAyahNum)
    EditText edSurahAyahNum;

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

    @OnClick(R.id.btnGo)
    public void onViewClicked() {
        int index;
        try {
            index = Integer.parseInt(edSurahNum.getText().toString());
        } catch (NumberFormatException e) {
            index = 1;
        }

        int ayahIndex;
        try {
            ayahIndex = Integer.parseInt(edSurahAyahNum.getText().toString());
        } catch (NumberFormatException e) {
            ayahIndex = 1;
        }

        if (index < 1 || index > 114) {
            edSurahNum.setError("Number must be 1-114");
            return;
        }

        goToSura(index, ayahIndex);
    }


 /*   public void goByName() {
        String name = edSurahName.getText().toString();
        List<String> suraList = new ArrayList<>(Arrays.asList(Data.SURA_NAMES));
        int index = suraList.indexOf(name);

        if (index == -1) {
            edSurahName.setError("Enter Valid Name");
            return;
        }
        goToSura(index, ayahIndex);
    }
*/

    private void goToSura(int index, int ayahIndex) {
        Intent intent = new Intent(getActivity(), ShowAyahsActivity.class);
        intent.putExtra(Constants.SURAH_GO_INDEX, index);
        intent.putExtra(Constants.AYAH_GO_INDEX, ayahIndex);
        startActivity(intent);
        dismiss();
    }

    @OnClick(R.id.btnGoToJuz)
    public void onJuz() {
        int index = 0;
        try {
            index = Integer.parseInt(edJuz.getText().toString());
        } catch (NumberFormatException e) {
            index = 1;
        }

        if (index < 1 || index > 30) {
            edJuz.setError("Number must be 1-30");
            return;
        }
        goToJuz(index);
    }

    private void goToJuz(int index) {
        Intent intent = new Intent(getActivity(), ShowAyahsActivity.class);
        intent.putExtra(Constants.JUZ_INDEX, index);
        startActivity(intent);
        dismiss();

    }
}
