package education.mahmoud.quranyapp.data_layer.remote;

import education.mahmoud.quranyapp.data_layer.remote.model.full_quran.FullQuran;
import education.mahmoud.quranyapp.data_layer.remote.model.tafseer_model.Tafseer;
import education.mahmoud.quranyapp.data_layer.remote.services.QuranService;
import retrofit2.Call;

public class Remote {

    private QuranService tafseerService;

    public Remote() {
        tafseerService = ApiClient.getRetroQuran().create(QuranService.class);
    }

    // Tafseer
    public Call<Tafseer> getChapterTafser(int id) {
        return tafseerService.getTafseer(id);
    }

    // Quran

    public Call<FullQuran> getQuran() {
        return tafseerService.getQuran();
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
