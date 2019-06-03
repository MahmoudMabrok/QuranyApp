package education.mahmoud.quranyapp.data_layer.remote.services;

import education.mahmoud.quranyapp.data_layer.model.full_quran.FullQuran;
import education.mahmoud.quranyapp.data_layer.model.tafseer_model.Tafseer;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface QuranService {

    @GET("surah/{id}/ar.muyassar")
    Call<Tafseer> getTafseer(@Path("id") int id);

    @GET("quran/quran-uthmani")
    Call<FullQuran> getQuran();

}
