package com.example.footballstatistics_app_wearos.presentation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase

class ActivitySetUpViewModelFactory(private val database: AppDatabase) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivitySetUpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivitySetUpViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}