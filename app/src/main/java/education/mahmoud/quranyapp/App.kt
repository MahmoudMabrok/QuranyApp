package education.mahmoud.quranyapp

import android.app.Application
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.di.dataModule
import education.mahmoud.quranyapp.feature.showSuraAyas.Page
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.HizbMark
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderInfo
import education.mahmoud.quranyapp.utils.getSuraNameFromIndex
import education.mahmoud.quranyapp.utils.LocaleHelper
import education.mahmoud.quranyapp.utils.log
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    val quranRepository: QuranRepository by inject()
    var quranPages: ArrayList<Page> = arrayListOf()
    val relay = PublishRelay.create<Boolean>()
    /**
     * Holds the latest list of pages and replays it to every new subscriber, so a reading
     * screen opened after the pages were already built (e.g. the second time) still gets them.
     */
    val relayPages = BehaviorRelay.create<ArrayList<Page>>()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(listOf(dataModule))
        }

        //  persistanscePages()

        //   LocaleHelper.setLocale(this, "ar")

        LocaleHelper.setLocale(this, "ar")
    }

    fun persistanscePages() {
        // pages already built and cached in [relayPages]; new subscribers get them immediately
        if (quranPages.isNotEmpty() && relayPages.hasValue()) return
        Thread(
            Runnable {
                try {
                    loadFullQuran()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        ).start()
    }

    @Synchronized
    fun loadFullQuran() {
        "loadFullQuran".log()
        if (quranPages.isEmpty()) {
            val pages: MutableList<Page> = ArrayList()
            var page: Page
            var ayahItems: List<AyahItem>
            val start = System.currentTimeMillis()
            // sura header data (Makki/Madani, ayah count) for the Mushaf-style headers
            val suraInfos = try {
                quranRepository.suras.associate { sura ->
                    sura.index to SuraHeaderInfo(
                        index = sura.index,
                        name = sura.index.getSuraNameFromIndex(),
                        revelationType = sura.revelationType,
                        numOfAyahs = sura.numOfAyahs,
                    )
                }
            } catch (e: Exception) {
                emptyMap()
            }
            var previousQuarter = 0
            for (i in 1..604) {
                ayahItems = quranRepository.getAyahsByPage(i)
                if (ayahItems.isNotEmpty()) {
                    page = Page(ayahItems)
                    page.pageNum = i
                    page.juz = ayahItems[0].juz
                    page.suraInfos = ayahItems.filter { it.ayahInSurahIndex == 1 }
                        .mapNotNull { suraInfos[it.surahIndex] }
                        .associateBy { it.index }
                    page.hizbMark = HizbMark.find(ayahItems.map { it.hizbQuarter }, previousQuarter)
                    previousQuarter = ayahItems.last().hizbQuarter
                    pages.add(page)
                }
            }
            val end = System.currentTimeMillis()
            "tttime : ${(end - start)}ms ".log() // 18966ms
            quranPages.addAll(pages)
        }
        relayPages.accept(quranPages)
    }

    companion object {
        private const val TAG = "App"
    }
}
