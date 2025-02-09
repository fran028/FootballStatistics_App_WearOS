package com.example.footballstatistics_app_wearos.presentation.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore

val Context.matchDataStore: DataStore<Match> by dataStore(
    fileName = "match.json",
    serializer = MatchSerializer
)

object CurrentMatch {
    private var _match: Match? = null
    val match: Match?
        get() = _match

    fun setMatch(newMatch: Match) {
        _match = newMatch
    }

    fun clearMatch() {
        _match = null
    }
}

suspend fun clearMatchDataStore(context: Context) {
    context.matchDataStore.updateData {
        Match(
            date = "",
            total_time = "",
            away_corner_location = "",
            home_corner_location = "",
            kickoff_location = "",
            start_location = "",
            activityData = "",
            iniTime = "",
            endTime = "",
            matchStatus = "Not Started",
            end_location = ""
        )}
}