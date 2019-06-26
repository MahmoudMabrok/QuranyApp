package education.mahmoud.quranyapp.data_layer.remote.services;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface MLModelService {

    @POST("predict")
    Call<ResponseBody> calcutaeSalary(@Body String years);

    @FormUrlEncoded
    @POST("test")
    Call<ResponseBody> calcutaeSalary2(@Field("years") String years);


}
