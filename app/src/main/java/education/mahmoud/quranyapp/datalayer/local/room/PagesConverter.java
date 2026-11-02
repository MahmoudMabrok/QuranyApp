package education.mahmoud.quranyapp.datalayer.local.room;

import androidx.collection.ArraySet;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PagesConverter {

    @TypeConverter
    public String fromPagesToString(ArraySet<Integer> pages) {
        if (pages == null) {
            return null;
        }
        // Plain JSON array. Serializing ArraySet itself is not stable under R8
        // (Gson may write internal fields, then fail to construct it on the next read).
        List<Integer> copy = new ArrayList<>(pages.size());
        for (Integer page : pages) {
            if (page != null) copy.add(page);
        }
        return new Gson().toJson(copy);
    }

    @TypeConverter
    public ArraySet<Integer> toPagesList(String pages) {
        ArraySet<Integer> set = new ArraySet<>();
        if (pages == null || pages.isEmpty()) {
            return set;
        }
        try {
            Type type = new TypeToken<List<Integer>>() {
            }.getType();
            List<Integer> list = new Gson().fromJson(pages, type);
            if (list != null) {
                for (Integer page : list) {
                    if (page != null) set.add(page);
                }
            }
        } catch (Exception ignored) {
            // A bad or legacy blob must not crash the reader. The log is only a counter.
        }
        return set;
    }



}
