package education.mahmoud.quranyapp.model;

import com.google.gson.annotations.SerializedName;

public class Quran {
    @SerializedName("surahs")
    private Sura[] surahs;

    public Sura[] getSurahs() {
        return surahs;
    }

    public void setSurahs(Sura[] surahs) {
        this.surahs = surahs;
    }
}

