package com.example.footballstatistics_app_wearos.presentation.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.data.MatchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityResultViewModel(private val database: AppDatabase) : ViewModel() {
    private val TAG = "ActivityResultViewModel"

    var isThereAnyMatch by mutableStateOf(false)
        private set
    var matchId by mutableIntStateOf(0)
        private set
    var currentMatch: MatchEntity? by mutableStateOf(null)
        private set
    var isLoading by mutableStateOf(true)
        private set

    init {
        fetchMatchData()
    }
    private fun fetchMatchData() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Log.d(TAG, "Fetching match from database")
                    isThereAnyMatch = database.matchDao().isThereAnyMatch()
                    if (isThereAnyMatch) {
                        matchId = database.matchDao().getMatchId()
                        currentMatch = database.matchDao().getMatchById(matchId)
                        Log.d(TAG, "Match Data fetched")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching match", e)
            } finally {
                isLoading = false
            }
        }
    }
}