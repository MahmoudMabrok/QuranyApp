
package education.mahmoud.quranyapp.data_layer.remote.model.tafseer_model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("number")
    @Expose
    private int number;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("englishName")
    @Expose
    private String englishName;
    @SerializedName("englishNameTranslation")
    @Expose
    private String englishNameTranslation;
    @SerializedName("revelationType")
    @Expose
    private String revelationType;
    @SerializedName("numberOfAyahs")
    @Expose
    private int numberOfAyahs;
    @SerializedName("ayahs")
    @Expose
    private List<Ayah> ayahs = null;
    @SerializedName("edition")
    @Expose
    private Edition edition;

    /**
     * No args constructor for use in serialization
     */
    public Data() {
    }

    /**
     * @param englishName
     * @param edition
     * @param numberOfAyahs
     * @param name
     * @param number
     * @param revelationType
     * @param ayahs
     * @param englishNameTranslation
     */
    public Data(int number, String name, String englishName, String englishNameTranslation, String revelationType, int numberOfAyahs, List<Ayah> ayahs, Edition edition) {
        super();
        this.number = number;
        this.name = name;
        this.englishName = englishName;
        this.englishNameTranslation = englishNameTranslation;
        this.revelationType = revelationType;
        this.numberOfAyahs = numberOfAyahs;
        this.ayahs = ayahs;
        this.edition = edition;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getEnglishNameTranslation() {
        return englishNameTranslation;
    }

    public void setEnglishNameTranslation(String englishNameTranslation) {
        this.englishNameTranslation = englishNameTranslation;
    }

    public String getRevelationType() {
        return revelationType;
    }

    public void setRevelationType(String revelationType) {
        this.revelationType = revelationType;
    }

    public int getNumberOfAyahs() {
        return numberOfAyahs;
    }

    public void setNumberOfAyahs(int numberOfAyahs) {
        this.numberOfAyahs = numberOfAyahs;
    }

    public List<Ayah> getAyahs() {
        return ayahs;
    }

    public void setAyahs(List<Ayah> ayahs) {
        this.ayahs = ayahs;
    }

    public Edition getEdition() {
        return edition;
    }

    public void setEdition(Edition edition) {
        this.edition = edition;
    }

}
