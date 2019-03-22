package education.mahmoud.quranyapp.feature.show_sura_list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.Util.Data;

public class SuraAdapter extends RecyclerView.Adapter<SuraAdapter.Holder> {


    private static final String TAG = "SuraAdapter";
    private SuraListner suraListner;
    private List<String> list;

    public SuraAdapter() {
        list = new ArrayList<>();
    }

    public void setSuraListner(SuraListner suraListner) {
        this.suraListner = suraListner;
    }

    public void setStringList(List<String> newList) {
        list = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public List<String> getList() {
        return list;
    }

    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sura_item, viewGroup, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        String s = list.get(i);
        s = Data.SURA_NAMES[i];
        holder.tvSura.setText(s);
        Log.d(TAG, "onBindViewHolder: " + i + s);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    interface SuraListner {
        void onSura(int pos);
    }

    class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvSuraName)
        TextView tvSura;


        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    suraListner.onSura(pos);
                }
            });
        }
    }

}