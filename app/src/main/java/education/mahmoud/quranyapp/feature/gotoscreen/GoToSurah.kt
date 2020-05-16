package education.mahmoud.quranyapp.feature.gotoscreen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.feature.showSuraAyas.ShowAyahsActivity
import education.mahmoud.quranyapp.utils.Constants
import kotlinx.android.synthetic.main.go_to_sura.*

class GoToSurah : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.go_to_sura, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListners()
    }

    private fun setClickListners() {
        btnGo.setOnClickListener {
            onViewClicked()
        }

        btnGoToJuz.setOnClickListener {
            onJuz()
        }

        btnGoToPage.setOnClickListener {
            onbtnGoToPage()
        }


    }

    fun onViewClicked() {
        val index: Int
        index = try {
            edSurahNum.text.toString().toInt()
        } catch (e: NumberFormatException) {
            1
        }
        val ayahIndex: Int
        ayahIndex = try {
            edSurahAyahNum.text.toString().toInt()
        } catch (e: NumberFormatException) {
            1
        }
        if (index < 1 || index > 114) {
            edSurahNum.error = "Number must be 1-114"
            return
        }
        goToSura(index, ayahIndex)
    }

    /*   public void goByName() {
        String name = edSurahName.getText().toString();
        List<String> suraList = new ArrayList<>(Arrays.asList(Data.SURA_NAMES));
        int index = suraList.indexOf(name);

        if (index == -1) {
            edSurahName.setError("Enter Valid Name");
            return;
        }
        goToSura(index, ayahIndex);
    }
*/
    private fun goToSura(index: Int, ayahIndex: Int) {
        val intent = Intent(activity, ShowAyahsActivity::class.java)
        intent.putExtra(Constants.SURAH_GO_INDEX, index)
        intent.putExtra(Constants.AYAH_GO_INDEX, ayahIndex)
        startActivity(intent)
        dismiss()
    }

    fun onJuz() {
        var index = 0
        index = try {
            edJuz.text.toString().toInt()
        } catch (e: NumberFormatException) {
            1
        }
        if (index < 1 || index > 30) {
            edJuz.error = "Number must be 1-30"
            return
        }
        goToJuz(index)
    }

    private fun goToJuz(index: Int) {
        val intent = Intent(activity, ShowAyahsActivity::class.java)
        intent.putExtra(Constants.JUZ_INDEX, index)
        startActivity(intent)
        dismiss()
    }


    fun onbtnGoToPage() {
        try {
            val page: Int = edPage.text.toString().toInt()
            if (page > 0 && page <= 604) {
                val intent = Intent(activity, ShowAyahsActivity::class.java)
                intent.putExtra(Constants.PAGE_INDEX, page)
                startActivity(intent)
                dismiss()
            } else {
                edPage.error = getString(R.string.page_limit)
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }
}