package education.mahmoud.quranyapp.feature.test_quran;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;

public class SuraNameAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    List<String> suraNames;

    @BindView(R.id.textView1)
    TextView textView1;



    public SuraNameAdapter(Context context, List<String> names) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        suraNames = names;
    }

    @Override
    public int getCount() {
        return suraNames.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.recycler_view_item, null);
        ButterKnife.bind(this, view);
        textView1.setText(suraNames.get(index));
        return view;
    }


}
