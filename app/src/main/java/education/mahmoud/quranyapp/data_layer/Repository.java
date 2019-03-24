package education.mahmoud.quranyapp.data_layer;

import android.content.Context;

import education.mahmoud.quranyapp.data_layer.local.LocalShared;

public class Repository {

    private static LocalShared localShared;
    private static Repository instance;

    private Repository() {
    }

    public static Repository getInstance(Context context) {
        if (instance == null) {
            instance = new Repository();
            localShared = new LocalShared(context);
        }
        return instance;
    }

    public void addLastSura(int index) {
        localShared.addLastSura(index);
    }

    public void addLastSuraWithScroll(int index) {
        localShared.addLastSuraWithScroll(index);
    }


    public int getLastSura() {
        return localShared.getLastSura();
    }

    public int getLastSuraWithScroll() {
        return localShared.getLastSuraWithScroll();
    }


}
