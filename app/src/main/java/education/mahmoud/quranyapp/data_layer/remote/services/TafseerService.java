package education.mahmoud.quranyapp.data_layer.remote.services;

import education.mahmoud.quranyapp.data_layer.remote.model.Tafseer;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TafseerService {

    @GET("surah/{id}/ar.muyassar")
    Call<Tafseer> getTafseer(@Path("id") int id);

}
