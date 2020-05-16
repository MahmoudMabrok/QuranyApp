package education.mahmoud.quranyapp.di

import androidx.room.Room
import com.google.gson.GsonBuilder
import education.mahmoud.quranyapp.datalayer.Repository
import education.mahmoud.quranyapp.datalayer.local.LocalShared
import education.mahmoud.quranyapp.datalayer.local.room.QuranDB
import education.mahmoud.quranyapp.feature.showSuraAyas.AyahsViewModel
import education.mahmoud.quranyapp.feature.show_sura_list.SuraListViewModel
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val dataModule = module {

    single {
        GsonBuilder()
                .setLenient()
                .create()
    }

    single {
        OkHttpClient().newBuilder()
                .connectTimeout(40, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
    }

    single { LocalShared(get()) }

    single {
        Room.databaseBuilder(get(), QuranDB::class.java, "quran")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
    }

    single { Repository(get(), get()) }

    viewModel { SuraListViewModel(get()) }
    viewModel { AyahsViewModel(get()) }
}