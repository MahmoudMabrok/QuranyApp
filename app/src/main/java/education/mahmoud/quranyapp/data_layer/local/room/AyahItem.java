package education.mahmoud.quranyapp.data_layer.local.room;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "ayahs")
public class AyahItem {

    @PrimaryKey
    private int ayahIndex;
    private int surahIndex;
    private int pageNum;
    private int juz;
    private int hizbQuarter;
    private boolean sajda;
    private int manzil;
    private int ayahInSurahIndex;
    private String text;
    private String textClean;
    private String tafseer;
    private String audioPath;


    @Ignore
    public AyahItem() {
    }

    @Ignore
    public AyahItem(int ayahIndex, int surahIndex, int ayahInSurahIndex, String text, String textClean) {
        this.ayahIndex = ayahIndex;
        this.surahIndex = surahIndex;
        this.ayahInSurahIndex = ayahInSurahIndex;
        this.text = text;
        this.textClean = textClean;
    }

    public AyahItem(int ayahIndex, int surahIndex, int pageNum, int juz, int hizbQuarter, boolean sajda, int manzil, int ayahInSurahIndex, String text, String textClean, String tafseer, String audioPath) {
        this.ayahIndex = ayahIndex;
        this.surahIndex = surahIndex;
        this.pageNum = pageNum;
        this.juz = juz;
        this.hizbQuarter = hizbQuarter;
        this.sajda = sajda;
        this.manzil = manzil;
        this.ayahInSurahIndex = ayahInSurahIndex;
        this.text = text;
        this.textClean = textClean;
        this.tafseer = tafseer;
        this.audioPath = audioPath;
    }

    @Ignore
    public AyahItem(int ayahIndex, int surahIndex, int pageNum, int juz, int hizbQuarter, boolean sajda, int ayahInSurahIndex, String text, String textClean) {
        this.ayahIndex = ayahIndex;
        this.surahIndex = surahIndex;
        this.pageNum = pageNum;
        this.juz = juz;
        this.hizbQuarter = hizbQuarter;
        this.sajda = sajda;
        this.ayahInSurahIndex = ayahInSurahIndex;
        this.text = text;
        this.textClean = textClean;
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

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getJuz() {
        return juz;
    }

    public void setJuz(int juz) {
        this.juz = juz;
    }

    public int getHizbQuarter() {
        return hizbQuarter;
    }

    public void setHizbQuarter(int hizbQuarter) {
        this.hizbQuarter = hizbQuarter;
    }

    public boolean isSajda() {
        return sajda;
    }

    public void setSajda(boolean sajda) {
        this.sajda = sajda;
    }

    public int getManzil() {
        return manzil;
    }

    public void setManzil(int manzil) {
        this.manzil = manzil;
    }

    public int getAyahInSurahIndex() {
        return ayahInSurahIndex;
    }

    public void setAyahInSurahIndex(int ayahInSurahIndex) {
        this.ayahInSurahIndex = ayahInSurahIndex;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTextClean() {
        return textClean;
    }

    public void setTextClean(String textClean) {
        this.textClean = textClean;
    }

    public String getTafseer() {
        return tafseer;
    }

    public void setTafseer(String tafseer) {
        this.tafseer = tafseer;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }
}
