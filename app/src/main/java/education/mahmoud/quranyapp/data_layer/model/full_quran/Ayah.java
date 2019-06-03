
package education.mahmoud.quranyapp.data_layer.model.full_quran;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ayah {

    @SerializedName("number")
    @Expose
    private int number;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("numberInSurah")
    @Expose
    private int numberInSurah;
    @SerializedName("juz")
    @Expose
    private int juz;
    @SerializedName("manzil")
    @Expose
    private int manzil;
    @SerializedName("page")
    @Expose
    private int page;
    @SerializedName("ruku")
    @Expose
    private int ruku;
    @SerializedName("hizbQuarter")
    @Expose
    private int hizbQuarter;


    /**
     * No args constructor for use in serialization
     */
    public Ayah() {
    }

    /**
     * @param text
     * @param page
     * @param juz
     * @param ruku
     * @param manzil
     * @param number
     * @param numberInSurah
     * @param hizbQuarter
     */
    public Ayah(int number, String text, int numberInSurah, int juz
            , int manzil, int page, int ruku, int hizbQuarter) {
        super();
        this.number = number;
        this.text = text;
        this.numberInSurah = numberInSurah;
        this.juz = juz;
        this.manzil = manzil;
        this.page = page;
        this.ruku = ruku;
        this.hizbQuarter = hizbQuarter;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getNumberInSurah() {
        return numberInSurah;
    }

    public void setNumberInSurah(int numberInSurah) {
        this.numberInSurah = numberInSurah;
    }

    public int getJuz() {
        return juz;
    }

    public void setJuz(int juz) {
        this.juz = juz;
    }

    public int getManzil() {
        return manzil;
    }

    public void setManzil(int manzil) {
        this.manzil = manzil;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getRuku() {
        return ruku;
    }

    public void setRuku(int ruku) {
        this.ruku = ruku;
    }

    public int getHizbQuarter() {
        return hizbQuarter;
    }

    public void setHizbQuarter(int hizbQuarter) {
        this.hizbQuarter = hizbQuarter;
    }


}
