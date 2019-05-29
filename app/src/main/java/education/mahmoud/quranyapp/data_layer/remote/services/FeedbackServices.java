package education.mahmoud.quranyapp.data_layer.remote.services;

import java.util.List;

import education.mahmoud.quranyapp.data_layer.remote.model.Feedback;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface FeedbackServices {

    @POST("/insertfeedback.php")
    Call<Void> insertFeedback(@Body Feedback feedback);

    @GET("/getFeedbacks")
    Call<List<Feedback>> getFeedbaks();
}
