package education.mahmoud.quranyapp.data_layer.local.room;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import androidx.collection.ArraySet;
import androidx.room.TypeConverter;

public class PagesConverter {

    @TypeConverter
    public String fromPagesToString(ArraySet<Integer> pages){
        if (pages == null){
            return null;
        }
        // create json
        Gson gson = new Gson();
        Type type = new TypeToken<ArraySet<Integer>>(){}.getType();
        return gson.toJson(pages,type);
    }

    @TypeConverter
    public ArraySet<Integer> toPagesList(String pages){
        if (pages == null){
            return null ;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<ArraySet<Integer>>(){}.getType();
        return gson.fromJson(pages,type);

    }



}
