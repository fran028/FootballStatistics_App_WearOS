package com.example.footballstatistics_app_wearos.presentation.presentation

import android.content.Context
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.presentation.UploadViewModel

class AppContainer(private val applicationContext: Context) {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(applicationContext) }
    val uploadViewModel: UploadViewModel by lazy { UploadViewModel() }
}