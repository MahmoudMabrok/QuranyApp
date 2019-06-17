package education.mahmoud.quranyapp.data_layer.local.room;

import java.util.List;
import java.util.Set;

import androidx.collection.ArraySet;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "readlog")
public class ReadLog {

    @PrimaryKey
    private long date;
    private String strDate;

    @ColumnInfo(name = "page_num")
    private Set<Integer> pages = new ArraySet<>();


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

    public Set<Integer> getPages() {
        return pages;
    }

    public void setPages(Set<Integer> pages) {
        this.pages = pages;
    }
}
