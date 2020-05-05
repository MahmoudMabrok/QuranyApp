package education.mahmoud.quranyapp.feature.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxrelay2.PublishRelay
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.DataLoadingBaseFragment
import education.mahmoud.quranyapp.datalayer.Repository
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.datalayer.local.room.SuraItem
import education.mahmoud.quranyapp.datalayer.model.full_quran.Surah
import education.mahmoud.quranyapp.feature.home_Activity.HomeActivity
import education.mahmoud.quranyapp.utils.Util
import education.mahmoud.quranyapp.utils.log
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_splash.*
import org.koin.android.ext.android.inject

class Splash : DataLoadingBaseFragment() {

    private val repository: Repository by inject()
    private val ayhasCount by lazy { repository.totlaAyahs }
    val relay = PublishRelay.create<Boolean>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_splash, container, false)
    }

    override fun initViews(view: View) {
        super.initViews(view)

        if (ayhasCount > 0) {
            view.postDelayed({
                (activity as? HomeActivity)?.afterSplash()
            }, 3000)
        } else {
            progressBar.visibility = View.VISIBLE
        }
    }

    override fun startLoadingData() {
        super.startLoadingData()
        if (ayhasCount == 0) {
            loadQuran()
        }
    }

    override fun startObserving() {
        super.startObserving()

        relay.doAfterNext {
            "onNext relay".log()
            (activity as? HomeActivity)?.afterSplash()
        }?.subscribe()?.addTo(bg)
    }

    fun loadQuran() {
        "loadQuran".log()
        val surahs = Util.getFullQuranSurahs(requireContext())
        StoreInDb(surahs)
    }

    private fun StoreInDb(surahs: List<Surah>) {
        Thread(Runnable { Store(surahs) }).start()
    }

    private fun Store(surahs: List<Surah>) {
        var suraItem: SuraItem
        var ayahItem: AyahItem
        val quran = Util.getQuranClean(requireContext())
        val surahClean = quran.surahs
        var clean: String?

        "start".log()

        for (surah in surahs) {
            suraItem = SuraItem(surah.number, surah.ayahs.size, surah.name, surah.englishName, surah.englishNameTranslation, surah.revelationType)
            // add start page
            suraItem.index = surah.number
            suraItem.startIndex = surah.ayahs[0].page
            try {
                repository.addSurah(suraItem)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            for (ayah in surah.ayahs) {
                ayahItem = AyahItem(ayah.number, surah.number, ayah.page, ayah.juz, ayah.hizbQuarter, false, ayah.numberInSurah, ayah.text, ayah.text)

                if (surahClean != null) {
                    clean = surahClean[surah.number - 1].ayahs[ayahItem.ayahInSurahIndex - 1].text
                    ayahItem.textClean = clean
                } else {
                    ayahItem.textClean = Util.removeTashkeel(ayahItem.text)
                }
                try {
                    repository.addAyah(ayahItem)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        "finis".log()
    }
}