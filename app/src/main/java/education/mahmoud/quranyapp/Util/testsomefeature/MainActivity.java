package education.mahmoud.quranyapp.Util.testsomefeature;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.model.Quran;
import education.mahmoud.quranyapp.model.Sura;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.rv)
    RecyclerView rv;

    List<String> namesofFile = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        PageAdapter pageAdapter = new PageAdapter(this);
        rv.setLayoutManager(linearLayoutManager);
        rv.setAdapter(pageAdapter);
        intializeList();
        pageAdapter.setLsit(namesofFile);

        parseJson();

    }

    private void parseJson() {
        try (InputStream fileIn = this.getAssets().open("data.json");
             BufferedInputStream bufferedIn = new BufferedInputStream(fileIn);
             Reader reader = new InputStreamReader(bufferedIn, StandardCharsets.UTF_8)) {
            Quran quran = new Gson().fromJson(reader, Quran.class);

            Sura[] suras = quran.getSurahs();
            StringBuilder sb = new StringBuilder();
            String s = "String[] SURA_NAMES = new String[]{";
            for (int i = 0; i < suras.length; i++) {
                sb.append("\"" + suras[i].getName() + "\"");
                if (i != suras.length - 1)
                    sb.append(",");
            }

            String res = s + sb.toString() + "};";
            System.out.println(res);


        } catch (Exception e) {
            Log.d(TAG, "parseJson: " + '#');
        }
    }

    private void intializeList() {
        int COUNT = 604;
        namesofFile = new ArrayList<>();
        for (int i = 1; i <= COUNT; i++) {
            namesofFile.add(new String("prefix_" + getNumStr(i)));
        }
    }

    @org.jetbrains.annotations.NotNull
    private String getNumStr(int i) {
        String numStr = String.valueOf(i);
        StringBuilder sb = new StringBuilder(numStr);
        while (sb.length() < 4) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }
}
