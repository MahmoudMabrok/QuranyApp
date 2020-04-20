package education.mahmoud.quranyapp.utils.testsomefeature;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;


public class PageAdapter extends RecyclerView.Adapter<PageAdapter.Holder> {

    private static final String TAG = "PageAdapter";
    Context context;
    List<String> names = new ArrayList<>();

    public PageAdapter(Context context1) {
        context = context1;
    }

    public void setLsit(List<String> list) {
        names = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        String name = 'a' + Integer.toString(i + 1);
        int resID;

    /*    holder.imageView.setImageResource(resID);

        InputStream inputstream= null;
        try {
            inputstream = context.getAssets().open(names[i]);
            Drawable drawable = Drawable.createFromStream(inputstream, null);
            holder.imageView.setImageDrawable(drawable);

            Bitmap bitmap = BitmapFactory.decodeStream(inputstream);
            Log.d(TAG, "onBindViewHolder: --- " + String.valueOf(bitmap == null));

            inputstream.close();

            Log.d(TAG, "onBindViewHolder: "+names[i]);
        } catch (IOException e) {
            e.printStackTrace();
            Glide.with(context).load("file:///assets/a0001.jpg").into(holder.imageView);

        }
*/

        resID = context.getResources().getIdentifier(names.get(i), "drawable", context.getPackageName());
        Log.d(TAG, "onBindViewHolder: " + resID);
        holder.imageView.setImageResource(resID);


    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageView)
        ImageView imageView;


        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}