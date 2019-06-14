package education.mahmoud.quranyapp.feature.test_quran;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;

public class SaveTestAdapter extends RecyclerView.Adapter<SaveTestAdapter.Holder> {

    private List<AyahItem> list;
    private IOnTestClick iOnTestClick;


    public SaveTestAdapter() {
        list = new ArrayList<>();
    }


    public void setiOnTestClick(IOnTestClick iOnTestClick) {
        this.iOnTestClick = iOnTestClick;
    }

    public void setAyahItemList(List<AyahItem> newList) {
        list = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public List<AyahItem> getList() {
        return list;
    }

    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.test_item, viewGroup, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int i) {
        final AyahItem item = list.get(i);
        holder.edTextToTest.setText("");
//        holder.edTextToTest.setEnabled(true);
        holder.tvAyahNum.setText("" + item.getAyahInSurahIndex());

        holder.btnCheck.setVisibility(View.VISIBLE);

        holder.btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                holder.edTextToTest.setEnabled(false);
                holder.btnCheck.setVisibility(View.GONE);
                iOnTestClick.onClickTestCheck(item, holder.edTextToTest);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    interface IOnTestClick {
        void onClickTestCheck(AyahItem item, TextInputEditText editText);
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvAyahNum)
        TextView tvAyahNum;
        @BindView(R.id.edTextToTest)
        TextInputEditText edTextToTest;
        @BindView(R.id.btnCheck)
        Button btnCheck;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


}