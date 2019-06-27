package education.mahmoud.quranyapp.data_layer.remote.services;

import java.io.File;

import education.mahmoud.quranyapp.data_layer.remote.model.MLResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface MLModelService {

    @FormUrlEncoded
    @POST("test")
    Call<MLResponse> calcutaeSalary(@Field("years") String years);

    @FormUrlEncoded
    @POST("sound")
    Call<MLResponse> calcutaeSalary2(@Field("years") String years);

    @POST("uploadfile")
    @Multipart
    Call<MLResponse> upload(@Part("file") RequestBody file);

    @POST("uploadfile")
    @Multipart
    Call<MLResponse> uploadFile(@Part("file") File file);


    @POST("uploadfile")
    @Multipart
    Call<MLResponse> upload(@Part MultipartBody.Part filePart);
}
