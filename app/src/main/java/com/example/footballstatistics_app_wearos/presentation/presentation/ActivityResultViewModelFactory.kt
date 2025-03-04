package com.example.footballstatistics_app_wearos.presentation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase

class ActivityResultViewModelFactory(private val database: AppDatabase) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivityResultViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}