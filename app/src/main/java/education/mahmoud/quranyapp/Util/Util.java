package education.mahmoud.quranyapp.Util;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
import education.mahmoud.quranyapp.R;
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;
import education.mahmoud.quranyapp.data_layer.model.full_quran.FullQuran;
import education.mahmoud.quranyapp.data_layer.model.full_quran.Surah;
import education.mahmoud.quranyapp.data_layer.model.tafseer.CompleteTafseer;
import education.mahmoud.quranyapp.model.Quran;

public class Util {

    private static final String TAG = "Util";

    private static TestText testText;

    public static boolean checkInput(String input) {
        return !TextUtils.isEmpty(input) && input.replaceAll(" ", "").length() != 0;
    }

    public static String getName(AyahItem item) {
        String name = Data.SURA_NAMES[item.getSurahIndex() - 1]; // surah index start from 1 but arr from 0
        return MessageFormat.format("{0},{1}",
                name, item.getAyahInSurahIndex());
    }

    public static String ayahItemToString(List<AyahItem> ayahItems) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArraySet<Integer>>(){}.getType();
        return gson.toJson(ayahItems, type);
    }

    public static List<AyahItem> fromStringToAyahItems(String ayahs){
        if (ayahs == null){
            return null;
        }
        Gson gson = new Gson();
        Type type  =new TypeToken<List<AyahItem>>(){}.getType();
        return gson.fromJson(ayahs,type);

    }


    public static Spannable getSpannable(String text) {

        Spannable spannable = new SpannableString(text);

        String REGEX = "لل";
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(text);

        int start, end;

        //region allah match
        while (m.find()) {
            start = m.start();
            while (text.charAt(start) != ' ' && start != 0) {
                start--;
            }
            end = m.end();
            while (text.charAt(end) != ' ') {
                end++;
            }
            spannable.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        //endregion


        return spannable;

    }

    public static Quran getQuranClean(Context context) {
        try (InputStream fileIn = context.getAssets().open("quran_clean.json");
             BufferedInputStream bufferedIn = new BufferedInputStream(fileIn);
             Reader reader = new InputStreamReader(bufferedIn, Charset.forName("UTF-8"))) {
            return new Gson().fromJson(reader, Quran.class);

        } catch (Exception e) {
            return null;
        }
    }

    public static List<Surah> getFullQuranSurahs(Context context) {
        try (InputStream fileIn = context.getAssets().open("quran.json");
             BufferedInputStream bufferedIn = new BufferedInputStream(fileIn);
             Reader reader = new InputStreamReader(bufferedIn, Charset.forName("UTF-8"))) {
            return new Gson().fromJson(reader, FullQuran.class).getData().getSurahs();

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * parse tafseer file and return object
     *
     * @param context
     * @return
     */
    public static CompleteTafseer getCompleteTafseer(Context context) {
        InputStream inputStream = null;
        BufferedInputStream stream = null;
        Reader reader = null;

        try {
            inputStream = context.getAssets().open("tafseer.json");
            stream = new BufferedInputStream(inputStream);
            reader = new InputStreamReader(stream, Charset.forName("UTF-8"));
            return new Gson().fromJson(reader, CompleteTafseer.class);
        } catch (Exception e) {
            return null;

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    stream.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getDirectoryPath() {
        File f = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOCUMENTS), "quran");
        return f.getAbsolutePath();
    }

    public static String getDirectoryPath(String dir) {
        File f = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOCUMENTS), dir);
        return f.getAbsolutePath();
    }

    public static String makeFilePath(String name) {
        String dirPath = getDirectoryPath("quran");
        return dirPath + name;
    }

    /**
     * checks if the device is connected to the internet or not
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public static void hideInputKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);
    }

    public static Spannable getSpanOfText(String text, String word) {
        Spannable spannable = new SpannableString(text);
        String REGEX = word;
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(text);

        //    Log.d(TAG, text + "getSpanOfText: word " + word);
        while (m.find()) {
//            Log.d(TAG, "getSpanOfText: start " + m.start());
//            Log.d(TAG, "getSpanOfText: end " + m.end());
            spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        return spannable;

    }

    public static Spannable getDiffSpannaled(String original, String toCompStr) {
        testText = new TestText();
        testText.gitDiff(original, toCompStr);
        String res = testText.getResString();
        return getSpannable(res, testText.getCorrectPoints(), testText.getInsertionPoints(), testText.getDeletionPoints());

    }

    public static Spannable getSpannable(String text, List<Point> correctPoints, List<Point> insertPoints, List<Point> delePoint) {
        Spannable spannable = new SpannableString(text);

        for (Point point : insertPoints) {
            spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), point.getStart()
                    , point.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Point point : delePoint) {
            spannable.setSpan(new ForegroundColorSpan(Color.RED), point.getStart()
                    , point.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Point point : correctPoints) {
            spannable.setSpan(new ForegroundColorSpan(Color.GREEN), point.getStart()
                    , point.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        return spannable;

    }

    public static long getTotalScore() {
        if (testText != null) {
            return testText.getTotalScore();
        }
        return 0;
    }


    public static String getArabicStrOfNum(long n) {
        ArabicTools tools = new ArabicTools();
        return tools.numberToArabicWords(String.valueOf(n));
    }


    public static Dialog getDialog(Context context, String message, String title) {
        View view = LayoutInflater.from(context).inflate(R.layout.custome_dialoge_title, null);
        TextView titleTextView = view.findViewById(R.id.tvInfo);
        titleTextView.setText(title);

        TextView textView = view.findViewById(R.id.tvDialogeText);
        textView.setText(message);

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;

    }

    public static Dialog getLoadingDialog(Context context, String message) {
        View view = LayoutInflater.from(context).inflate(R.layout.loading_dialoge, null);
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        return dialog;

    }

    public static String removeTashkeel(String ayah) {
        return RemoveTashkeel.removeTashkeel(ayah);
    }
}
