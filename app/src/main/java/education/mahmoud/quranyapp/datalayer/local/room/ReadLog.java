package education.mahmoud.quranyapp.datalayer.local.room;

import androidx.collection.ArraySet;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "readlog")
public class ReadLog {

    @PrimaryKey
    private long date;
    private String strDate;

    @ColumnInfo(name = "page_num")
    private ArraySet<Integer> pages ;


    public ReadLog() {
    }

    @Ignore
    public ReadLog(long date, String strDate, ArraySet<Integer> pages) {
        this.date = date;
        this.strDate = strDate;
        this.pages = pages;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getStrDate() {
        return strDate;
    }

    public void setStrDate(String strDate) {
        this.strDate = strDate;
    }


    public ArraySet<Integer> getPages() {
        return pages;
    }

    public void setPages(ArraySet<Integer> pages) {
        this.pages = pages;
    }
}
