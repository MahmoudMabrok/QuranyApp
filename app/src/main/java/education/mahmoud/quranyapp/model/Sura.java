package education.mahmoud.quranyapp.model;


import com.google.gson.annotations.SerializedName;

public class Sura {
    @SerializedName("name")
    private String name;
    @SerializedName("ayahs")
    private Aya[] ayahs;
    @SerializedName("num")
    private String num;

    public void setNum(String num) {
        this.num = num;
    }

    public Aya[] getAyahs() {
        return ayahs;
    }

    public void setAyahs(Aya[] ayahs) {
        this.ayahs = ayahs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
