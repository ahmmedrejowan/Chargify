package com.rejowan.chargify.di

import androidx.room.Room
import com.rejowan.chargify.data.local.ChargifyDatabase
import com.rejowan.chargify.data.preferences.AlarmPreferences
import com.rejowan.chargify.data.preferences.AppPreferences
import com.rejowan.chargify.data.repository.AppUsageRepository
import com.rejowan.chargify.data.repository.BatteryRepository
import com.rejowan.chargify.data.repository.BatteryRepositoryImpl
import com.rejowan.chargify.data.repository.ChargingHistoryRepository
import com.rejowan.chargify.presentation.viewmodel.AppUsageViewModel
import com.rejowan.chargify.presentation.viewmodel.ChargingAlarmsViewModel
import com.rejowan.chargify.presentation.viewmodel.ChargingHistoryViewModel
import com.rejowan.chargify.presentation.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            ChargifyDatabase::class.java,
            "chargify_database"
        ).build()
    }
    single { get<ChargifyDatabase>().chargingSessionDao() }

    // Preferences
    single { AppPreferences(androidContext()) }
    single { AlarmPreferences(androidContext()) }

    // Repositories
    single { ChargingHistoryRepository(get()) }
    single<BatteryRepository> { BatteryRepositoryImpl(androidContext(), get()) }
    single { AppUsageRepository(androidContext()) }

    // ViewModels
    viewModel { MainViewModel(get(), get()) }
    viewModel { ChargingHistoryViewModel(get()) }
    viewModel { ChargingAlarmsViewModel(get()) }
    viewModel { AppUsageViewModel(get()) }
}
