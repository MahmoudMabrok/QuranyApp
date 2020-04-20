package education.mahmoud.quranyapp.di

import education.mahmoud.quranyapp.data_layer.Repository
import education.mahmoud.quranyapp.feature.show_sura_ayas.AyahsViewModel
import education.mahmoud.quranyapp.feature.show_sura_list.SuraListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val dataModule = module {
    single {Repository()}

    viewModel { SuraListViewModel(get()) }
    viewModel { AyahsViewModel(get()) }
}