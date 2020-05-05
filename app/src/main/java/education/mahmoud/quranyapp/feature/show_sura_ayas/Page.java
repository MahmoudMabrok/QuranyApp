package education.mahmoud.quranyapp.feature.show_sura_ayas;

import java.util.List;

import education.mahmoud.quranyapp.datalayer.local.room.AyahItem;

public class Page {
    private List<AyahItem> ayahItems;
    private int pageNum;
    private int rubHizb;
    private int juz;


    public int getJuz() {
        return juz;
    }

    public void setJuz(int juz) {
        this.juz = juz;
    }

    public List<AyahItem> getAyahItems() {
        return ayahItems;
    }

    public void setAyahItems(List<AyahItem> ayahItems) {
        this.ayahItems = ayahItems;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getRubHizb() {
        return rubHizb;
    }

    public void setRubHizb(int rubHizb) {
        this.rubHizb = rubHizb;
    }
}
