package education.mahmoud.quranyapp.data_layer.local.room;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookmark")
public class BookmarkItem {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private long timemills;
    private int scrollIndex;
    private int pageNum;
    private int ayahNum;
    private String suraName;

    public BookmarkItem(int id, long timemills, int scrollIndex, int pageNum, int ayahNum, String suraName) {
        this.id = id;
        this.timemills = timemills;
        this.scrollIndex = scrollIndex;
        this.pageNum = pageNum;
        this.ayahNum = ayahNum;
        this.suraName = suraName;
    }

    @Ignore
    public BookmarkItem() {
    }

    @Ignore
    public BookmarkItem(long timemills, int scrollIndex, int pageNum, String suraName) {
        this.timemills = timemills;
        this.scrollIndex = scrollIndex;
        this.pageNum = pageNum;
        this.suraName = suraName;
    }

    public int getAyahNum() {
        return ayahNum;
    }

    public void setAyahNum(int ayahNum) {
        this.ayahNum = ayahNum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimemills() {
        return timemills;
    }

    public void setTimemills(long timemills) {
        this.timemills = timemills;
    }

    public int getScrollIndex() {
        return scrollIndex;
    }

    public void setScrollIndex(int scrollIndex) {
        this.scrollIndex = scrollIndex;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public String getSuraName() {
        return suraName;
    }

    public void setSuraName(String suraName) {
        this.suraName = suraName;
    }
}
