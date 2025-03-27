package com.example.footballstatistics_app_wearos.presentation

import android.app.Application
import com.example.footballstatistics_app_wearos.presentation.presentation.AppContainer

class FootballStatisticsApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}