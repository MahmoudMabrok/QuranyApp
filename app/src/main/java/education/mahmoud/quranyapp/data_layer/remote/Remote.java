package education.mahmoud.quranyapp.data_layer.remote;

import education.mahmoud.quranyapp.data_layer.model.full_quran.FullQuran;
import education.mahmoud.quranyapp.data_layer.model.tafseer_model.Tafseer;
import education.mahmoud.quranyapp.data_layer.remote.services.MLModelService;
import education.mahmoud.quranyapp.data_layer.remote.services.QuranService;
import okhttp3.ResponseBody;
import retrofit2.Call;

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


    public Call<ResponseBody> calcutaeSalary(String year) {
        return modelService.calcutaeSalary(year);
    }

    public Call<ResponseBody> calcutaeSalary2(String years) {
        return modelService.calcutaeSalary2(years);
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
