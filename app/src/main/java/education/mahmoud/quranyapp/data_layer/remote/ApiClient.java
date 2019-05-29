package education.mahmoud.quranyapp.data_layer.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String quranUrl = "http://api.alquran.cloud/v1/";
    static Gson gson = new GsonBuilder()
            .setLenient()
            .create();
    private static Retrofit retrofitQuran = null;
    private static Retrofit retrofitUser = null;

    public static Retrofit getRetroQuran() {


        if (retrofitQuran == null) {
            retrofitQuran = new Retrofit.Builder().baseUrl(quranUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofitQuran;
    }

}
