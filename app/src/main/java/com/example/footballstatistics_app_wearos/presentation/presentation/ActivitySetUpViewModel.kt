package com.example.footballstatistics_app_wearos.presentation.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivitySetUpViewModel(private val database: AppDatabase) : ViewModel() {
    private val TAG = "ActivitySetUpViewModel"

    var isThereAnyMatch by mutableStateOf(false)
        private set
    var isLocationSet by mutableStateOf(false)
        private set
    var isKickOffLocationSet by mutableStateOf(false)
        private set
    var isHomeCornerLocationSet by mutableStateOf(false)
        private set
    var isAwayCornerLocationSet by mutableStateOf(false)
        private set
    var matchId by mutableStateOf(0)
        private set
    var isLoading by mutableStateOf(true)
        private set

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Log.d(TAG, "Fetching data from database")
                    isThereAnyMatch = database.matchDao().isThereAnyMatch()
                    if (isThereAnyMatch) {
                        isLocationSet = database.matchDao().isLocationSet()
                        isKickOffLocationSet = database.matchDao().isKickoffSet()
                        isHomeCornerLocationSet = database.matchDao().isHomeCornersSet()
                        isAwayCornerLocationSet = database.matchDao().isAwayCornersSet()
                        matchId = database.matchDao().getMatchId()
                        Log.d(TAG, "Data fetched")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching data", e)
            } finally {
                isLoading = false
            }
        }
    }
}