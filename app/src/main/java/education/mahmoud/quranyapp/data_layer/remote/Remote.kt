package education.mahmoud.quranyapp.data_layer.remote

import education.mahmoud.quranyapp.data_layer.model.full_quran.FullQuran
import education.mahmoud.quranyapp.data_layer.model.tafseer_model.Tafseer
import education.mahmoud.quranyapp.data_layer.remote.model.MLResponse
import education.mahmoud.quranyapp.data_layer.remote.services.MLModelService
import education.mahmoud.quranyapp.data_layer.remote.services.QuranService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Part
import java.io.File

class Remote(private val tafseerService: QuranService, private val modelService: MLModelService) {

    // Tafseer
    fun getChapterTafser(id: Int): Call<Tafseer> {
        return tafseerService.getTafseer(id)
    }

    // Quran
    val quran: Call<FullQuran>
        get() = tafseerService.quran

    fun calcutaeSalary(year: String?): Call<MLResponse> {
        return modelService.calcutaeSalary(year)
    }

    fun calcutaeSalary2(years: String?): Call<MLResponse> {
        return modelService.calcutaeSalary2(years)
    }

    fun upload(@Part("file") file: RequestBody?): Call<MLResponse> {
        return modelService.upload(file)
    }

    fun uploadFile(file: File?): Call<MLResponse> {
        return modelService.uploadFile(file)
    }

    fun upload(filePart: MultipartBody.Part?): Call<MLResponse> {
        return modelService.upload(filePart)
    } /*

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
    }*/
/*


    public Call<String> getUsers() {
        return userServices.getUsers();
    }

    public Call<Void> sendFeedback( String pros ,  String cons ,  String suggs ) {
        return userServices.sendFeedback(pros, cons , suggs);
    }
*/

}