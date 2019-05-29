package education.mahmoud.quranyapp.data_layer.remote.services;

import education.mahmoud.quranyapp.data_layer.remote.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface UserServices {

    @POST("signup.php")
    Call<String> signUp(@Body User user);

    @POST("signup.php")
    Call<String> signUp(@Field("name") String name, @Field("mail") String mail, @Field("score") long score, @Field("n_ayahs") int n_ayahs);

/*
    @GET("getusers.php")
    Call<List<User>> getUsers();
    */

    @GET("getusers.php")
    Call<String> getUsers();

    @FormUrlEncoded
    @POST("addFeedback.php")
    Call<Void> sendFeedback(@Field("pros") String pros, @Field("cons") String cons, @Field("suggs") String suggs);

}
