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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_splash.*
import org.koin.android.ext.android.inject

class Splash : DataLoadingBaseFragment() {

    private val repository: Repository by inject()
    private var ayhasCount = repository.totlaAyahs
    val relay = PublishRelay.create<Boolean>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews(view)
    }

    override fun initViews(view: View) {
        super.initViews(view)

        if (ayhasCount > 0) {
            view.postDelayed({
                (activity as? HomeActivity)?.afterSplash()
            }, 3000)
        } else {
            group.visibility = View.VISIBLE
            startLoadingData()
            startObserving()
        }
    }

    override fun startLoadingData() {
        super.startLoadingData()
        if (ayhasCount == 0) {
            laodData()
        }
    }

    override fun startObserving() {
        super.startObserving()
        relay.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    "onNext relay".log()
                    (activity as? HomeActivity)?.afterSplash()
                }, {
                    "error ${it.message}".log()
                })?.addTo(bg)
    }

    fun laodData() {
        "loadQuran".log()
        val surahs = Util.getFullQuranSurahs(requireContext())
        StoreInDb(surahs)
    }

    private fun StoreInDb(surahs: List<Surah>) {
        Thread(Runnable { Store(surahs) }).start()
    }


    data class Extra(val clean: String, val tafseer: String)

    private fun Store(surahs: List<Surah>) {
        "start load Json".log()
        val cleanQuran = Util.getQuranClean(requireContext())
        "end load Json".log()
        val completeTafseer = Util.getCompleteTafseer(requireContext())
        "start maping ".log()
        // get all ayahs clean
        val ayhasClean = cleanQuran.surahs.flatMap { sura -> sura.ayahs.toMutableList() }
        // get all ayahs tafseer
        val ayhasTafseer = completeTafseer.data.surahs.flatMap { it.ayahs }
        // mix them
        val mixed = ayhasClean.zip(ayhasTafseer).map { Extra(it.first.text, it.second.text) }
        "data data ${ayhasClean.size}  ${ayhasTafseer.size} ${mixed.size}".log()
        val ayahss = mutableListOf<AyahItem>()
        // map it to db schema
        val surrahs = surahs.map {
            // add ayahs to list to be updated later with tafseer
            ayahss.addAll(it.ayahs.map { ayah -> AyahItem(ayah.number, it.number, ayah.page, ayah.juz, ayah.hizbQuarter, false, ayah.numberInSurah, ayah.text, ayah.text) })
            // create sura item
            SuraItem(it.number, it.ayahs.size, it.name, it.englishName, it.englishNameTranslation, it.revelationType).apply {
                index = it.number
                startIndex = it.ayahs.first().page
            }
        }


        ayahss.forEachIndexed { index, ayahItem ->
            ayahItem.apply {
                textClean = mixed[index].clean
                tafseer = mixed[index].tafseer
            }
        }

/*
        val ayahs = surahs.mapIndexed { index, surah ->
            surah.ayahs.map { ayah ->
                AyahItem(ayah.number, index + 1, ayah.page, ayah.juz, ayah.hizbQuarter, false, ayah.numberInSurah, ayah.text, ayah.text)
            }
        }.flatMap { list -> list.toMutableList() }
                .mapIndexed{ index, ayahItem ->
                    ayahItem.apply {
                        textClean = mixed[index].clean
                        tafseer = mixed[index].tafseer
                    }
                }*/


        "end maping ".log()
        try {
            repository.addSurahs(surrahs)
            repository.addAyahs(ayahss)
        } catch (e: Exception) {
        }
        "end inserting ".log()


        relay.accept(true)

        /*

            "start store".log()
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
                "sto s ".log()
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
                "finish sto s ".log()
            }
            "finis store ".log()
    */
        //  loadTafseerAndUpdateData()
    }

    private fun loadTafseerAndUpdateData() {
        "loadTafseerAndUpdateData".log()
        val completeTafseer = Util.getCompleteTafseer(requireContext())
        "loadTafseerAndUpdateData 2 ".log()
        var ayahItem: AyahItem
        if (completeTafseer != null) {
            val surahs = completeTafseer.data.surahs
            "AA".log()
            for (surah1 in surahs) {
                for (ayah in surah1.ayahs) {
                    ayahItem = repository.getAyahByIndex(ayah.number)
                    ayahItem.tafseer = ayah.text
                    try {
                        repository.updateAyahItem(ayahItem)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
            "AA finsh ".log()
        }
        "loadTafseerAndUpdateData finsih".log()
        relay.accept(true)

    }
}