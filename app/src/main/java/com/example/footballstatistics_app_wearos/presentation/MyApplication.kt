package com.example.footballstatistics_app_wearos.presentation

import android.app.Application

class MyApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}