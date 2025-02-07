package com.example.footballstatistics_app_wearos.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date

data class Match(
    var date: Date,
    var total_time: String,
    var away_corner_location: String,
    var home_corner_location: String,
    var kickoff_location: String,
    val start_location: String,
    var matchStatus: String = "Not Started",

    val activityData: String

)
