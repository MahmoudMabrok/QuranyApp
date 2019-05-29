package education.mahmoud.quranyapp.feature.scores;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;

public class ScoreboardAdapter extends RecyclerView.Adapter<ScoreboardAdapter.Holder> {

    private List<Scoreboard> list;

    public ScoreboardAdapter() {
        list = new ArrayList<>();
    }

    public void addScoreboard(Scoreboard item) {
        list.add(item);
        notifyItemInserted(list.size() - 1);
        notifyItemRangeChanged(list.size() - 1, list.size());
    }


    public void setScoreboardList(List<Scoreboard> newList) {
        list = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public List<Scoreboard> getList() {
        return list;
    }

    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.score_item, viewGroup, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        Scoreboard item = list.get(i);
        holder.tvScoreuserName.setText(item.name);
        holder.tvScore.setText(String.valueOf(item.score));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder {


        @BindView(R.id.tvScoreuserName)
        TextView tvScoreuserName;
        @BindView(R.id.tvScore)
        TextView tvScore;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}