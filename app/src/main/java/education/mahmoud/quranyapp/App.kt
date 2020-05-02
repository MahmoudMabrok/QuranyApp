package education.mahmoud.quranyapp

import android.app.Application
import android.util.Log
import education.mahmoud.quranyapp.data_layer.Repository
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem
import education.mahmoud.quranyapp.data_layer.model.full_quran.Surah
import education.mahmoud.quranyapp.di.dataModule
import education.mahmoud.quranyapp.feature.show_sura_ayas.Page
import education.mahmoud.quranyapp.utils.Util
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.*

class App : Application() {
    val repository: Repository by inject()

    var quranPages: List<Page> = listOf()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(listOf(dataModule))
        }


        val ahays = repository.totlaAyahs
        if (ahays == 0) {
            persistanscePages()
        } else {
            persistanscePages()
        }

    }

    private fun persistanscePages() {
        Thread(Runnable {
            try {
                loadFullQuran()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    fun loadQuran() {
        Log.d(TAG, "loadQuran: ")
        val surahs = Util.getFullQuranSurahs(this)
        StoreInDb(surahs)
    }

    private fun StoreInDb(surahs: List<Surah>) {
        Thread(Runnable { Store(surahs) }).start()
    }

    private fun Store(surahs: List<Surah>) {
        var suraItem: SuraItem
        var ayahItem: AyahItem
        val quran = Util.getQuranClean(this)
        val surahClean = quran.surahs
        var clean: String?
        for (surah in surahs) {
            suraItem = SuraItem(surah.number
                    , surah.ayahs.size
                    , surah.name, surah.englishName
                    , surah.englishNameTranslation, surah.revelationType)
            // add start page
            suraItem.index = surah.number
            suraItem.startIndex = surah.ayahs[0].page
            try {
                repository.addSurah(suraItem)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            for (ayah in surah.ayahs) {
                ayahItem = AyahItem(ayah.number, surah.number
                        , ayah.page, ayah.juz
                        , ayah.hizbQuarter, false
                        , ayah.numberInSurah, ayah.text
                        , ayah.text)
                //    ayahItem.setTextClean(Util.removeTashkeel(ayahItem.getText()));
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
    }

    private fun loadFullQuran() {
        val pages: MutableList<Page> = ArrayList()
        var page: Page
        var ayahItems: List<AyahItem>
        for (i in 1..604) {
            ayahItems = repository.getAyahsByPage(i)
            if (ayahItems.size > 0) {
                page = Page()
                page.ayahItems = ayahItems
                page.pageNum = i
                page.juz = ayahItems[0].juz
                pages.add(page)
            }
        }
        quranPages = ArrayList(pages)
    }

    companion object {
        private const val TAG = "App"
    }
}