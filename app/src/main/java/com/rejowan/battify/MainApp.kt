package com.rejowan.battify

import android.app.Application
import com.rejowan.battify.di.homeModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApp)
            modules(listOf(homeModule))
        }

    }
}