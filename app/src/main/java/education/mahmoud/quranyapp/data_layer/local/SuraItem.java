package education.mahmoud.quranyapp.data_layer.local;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "surah")
public class SuraItem {

    // surah start from 1 to 114
    @PrimaryKey
    private int index;
    private int startIndex;
    private int numOfAyahs;
    private String name;

    public SuraItem(int index, int startIndex, int numOfAyahs, String name) {
        this.index = index;
        this.startIndex = startIndex;
        this.numOfAyahs = numOfAyahs;
        this.name = name;
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
}
