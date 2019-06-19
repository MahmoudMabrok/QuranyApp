package education.mahmoud.quranyapp.feature.test_sound;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import education.mahmoud.quranyapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment {


    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

}
