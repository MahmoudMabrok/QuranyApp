package education.mahmoud.quranyapp

import android.app.Application
import android.content.Context
import com.jakewharton.rxrelay2.PublishRelay
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.di.dataModule
import education.mahmoud.quranyapp.utils.LocaleHelper
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    val quranRepository: QuranRepository by inject()
    val relay = PublishRelay.create<Boolean>()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(dataModule))
        }
        LocaleHelper.setLocale(this, "ar")
    }

    companion object {
        private const val TAG = "App"
    }
}
