package education.mahmoud.quranyapp.data_layer.remote;

import java.io.File;

import education.mahmoud.quranyapp.data_layer.model.full_quran.FullQuran;
import education.mahmoud.quranyapp.data_layer.model.tafseer_model.Tafseer;
import education.mahmoud.quranyapp.data_layer.remote.model.MLResponse;
import education.mahmoud.quranyapp.data_layer.remote.services.MLModelService;
import education.mahmoud.quranyapp.data_layer.remote.services.QuranService;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Part;

public class Remote {

    private QuranService tafseerService;
    private MLModelService modelService;

    public Remote() {
        tafseerService = ApiClient.getRetroQuran().create(QuranService.class);
        modelService = ApiClient.getModel().create(MLModelService.class);
    }

    // Tafseer
    public Call<Tafseer> getChapterTafser(int id) {
        return tafseerService.getTafseer(id);
    }

    // Quran
    public Call<FullQuran> getQuran() {
        return tafseerService.getQuran();
    }


    public Call<MLResponse> calcutaeSalary(String year) {
        return modelService.calcutaeSalary(year);
    }

    public Call<MLResponse> calcutaeSalary2(String years) {
        return modelService.calcutaeSalary2(years);
    }

    public Call<MLResponse> upload(@Part("file") RequestBody file) {
        return modelService.upload(file);
    }

    public Call<MLResponse> uploadFile(File file) {
        return modelService.uploadFile(file);
    }

    public Call<MLResponse> upload(MultipartBody.Part filePart) {
        return modelService.upload(filePart);
    }


/*

    // Users

    public Call<String> signUp(User user) {
        return userServices.signUp(user);
    }

    public Call<String> signUp(String name , String mail ,long score, int n_ayahs) {
        return userServices.signUp(name ,mail ,score, n_ayahs);
    }
*/
/*
    public Call<List<User>> getUsers() {
        return userServices.getUsers();
    }*//*


    public Call<String> getUsers() {
        return userServices.getUsers();
    }

    public Call<Void> sendFeedback( String pros ,  String cons ,  String suggs ) {
        return userServices.sendFeedback(pros, cons , suggs);
    }
*/

}
