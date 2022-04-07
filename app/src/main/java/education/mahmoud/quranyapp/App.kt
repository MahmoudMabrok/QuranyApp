package education.mahmoud.quranyapp

import android.app.Application
import com.jakewharton.rxrelay2.PublishRelay
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.di.dataModule
import education.mahmoud.quranyapp.feature.showSuraAyas.Page
import education.mahmoud.quranyapp.utils.LocaleHelper
import education.mahmoud.quranyapp.utils.log
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    val quranRepository: QuranRepository by inject()
    var quranPages: ArrayList<Page> = arrayListOf()

    val relayPages = PublishRelay.create<ArrayList<Page>>()

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
        Thread {
            try {
                loadFullQuran()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun loadFullQuran() {
        "loadFullQuran".log()
        if (quranPages.isEmpty()) {
            val pages: MutableList<Page> = ArrayList()
            var page: Page
            var ayahItems: List<AyahItem>
            val start = System.currentTimeMillis()
            for (i in 1..604) {
                ayahItems = quranRepository.getAyahsByPage(i)
                if (ayahItems.isNotEmpty()) {
                    page = Page(ayahItems)
                    page.pageNum = i
                    page.juz = ayahItems[0].juz
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
