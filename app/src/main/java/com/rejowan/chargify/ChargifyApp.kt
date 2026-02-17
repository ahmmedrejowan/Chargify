package com.rejowan.chargify

import android.app.Application
import com.rejowan.chargify.di.appModule
import com.rejowan.chargify.notification.BatteryAlarmNotification
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class ChargifyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@ChargifyApp)
            modules(listOf(appModule))
        }

        // Create notification channel for battery alarms
        BatteryAlarmNotification.createNotificationChannel(this)
    }
}
