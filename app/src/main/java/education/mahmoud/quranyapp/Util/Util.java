package education.mahmoud.quranyapp.Util;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import education.mahmoud.quranyapp.model.Quran;
import education.mahmoud.quranyapp.model.Sura;

public class Util {


    public static Sura getSurah(Context context, int index) {
        try (InputStream fileIn = context.getAssets().open("data.json");
             BufferedInputStream bufferedIn = new BufferedInputStream(fileIn);
             Reader reader = new InputStreamReader(bufferedIn, StandardCharsets.UTF_8)) {
            Quran quran = new Gson().fromJson(reader, Quran.class);
            return quran.getSurahs()[index];

        } catch (Exception e) {
            return null;
        }
    }

    public static Quran getQuran(Context context) {
        try (InputStream fileIn = context.getAssets().open("data.json");
             BufferedInputStream bufferedIn = new BufferedInputStream(fileIn);
             Reader reader = new InputStreamReader(bufferedIn, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, Quran.class);

        } catch (Exception e) {
            return null;
        }
    }

    public static Quran getQuranClean(Context context) {
        try (InputStream fileIn = context.getAssets().open("quran_clean.json");
             BufferedInputStream bufferedIn = new BufferedInputStream(fileIn);
             Reader reader = new InputStreamReader(bufferedIn, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, Quran.class);

        } catch (Exception e) {
            return null;
        }
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

}
