package education.mahmoud.quranyapp.datalayer.local.room;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "surah")
public class SuraItem {

    String englishName;
    String englishNameTranslation;
    String revelationType;
    // surah start from 1 to 114
    @PrimaryKey
    private int index;
    private int startIndex;
    private int numOfAyahs;
    private String name;
    private String audioPath;

    @Ignore
    public SuraItem() {
    }

    @Ignore
    public SuraItem(int index, int startIndex, int numOfAyahs, String name, String audioPath) {
        this.index = index;
        this.startIndex = startIndex;
        this.numOfAyahs = numOfAyahs;
        this.name = name;
        this.audioPath = audioPath;
    }


    @Ignore
    public SuraItem(int index, int startIndex, int numOfAyahs, String name) {
        this.index = index;
        this.startIndex = startIndex;
        this.numOfAyahs = numOfAyahs;
        this.name = name;
    }

    public SuraItem(int index, int numOfAyahs, String name, String englishName, String englishNameTranslation, String revelationType) {
        this.index = index;
        this.numOfAyahs = numOfAyahs;
        this.name = name;
        this.englishName = englishName;
        this.englishNameTranslation = englishNameTranslation;
        this.revelationType = revelationType;
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getNumOfAyahs() {
        return numOfAyahs;
    }

    public void setNumOfAyahs(int numOfAyahs) {
        this.numOfAyahs = numOfAyahs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

}
