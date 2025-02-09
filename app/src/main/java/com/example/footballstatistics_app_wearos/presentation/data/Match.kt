package com.example.footballstatistics_app_wearos.presentation.data

import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Match(
    var date: String,
    var total_time: String,
    var iniTime: String,
    var endTime: String,
    var away_corner_location: String,
    var home_corner_location: String,
    var kickoff_location: String,
    var start_location: String,
    var end_location: String,
    var matchStatus: String = "Not Started",

    val activityData: String

)
