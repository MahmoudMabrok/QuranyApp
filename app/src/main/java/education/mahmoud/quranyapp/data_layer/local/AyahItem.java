package education.mahmoud.quranyapp.data_layer.local;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "ayahs")
public class AyahItem {

    @PrimaryKey
    private int ayahIndex;
    private int surahIndex;
    private int ayahInSurahIndex;
    private String text;

    public AyahItem(int ayahIndex, int surahIndex, int ayahInSurahIndex, String text) {
        this.ayahIndex = ayahIndex;
        this.surahIndex = surahIndex;
        this.ayahInSurahIndex = ayahInSurahIndex;
        this.text = text;
    }

    public int getAyahIndex() {
        return ayahIndex;
    }

    public void setAyahIndex(int ayahIndex) {
        this.ayahIndex = ayahIndex;
    }

    public int getSurahIndex() {
        return surahIndex;
    }

    public void setSurahIndex(int surahIndex) {
        this.surahIndex = surahIndex;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getAyahInSurahIndex() {
        return ayahInSurahIndex;
    }

    public void setAyahInSurahIndex(int ayahInSurahIndex) {
        this.ayahInSurahIndex = ayahInSurahIndex;
    }
}
