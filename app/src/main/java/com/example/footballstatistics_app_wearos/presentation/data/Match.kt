package com.example.footballstatistics_app_wearos.presentation.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "total_time") var total_time: String,
    @ColumnInfo(name = "iniTime") var iniTime: String,
    @ColumnInfo(name = "endTime") var endTime: String,
    @ColumnInfo(name = "away_corner_location") var away_corner_location: String,
    @ColumnInfo(name = "home_corner_location") var home_corner_location: String,
    @ColumnInfo(name = "kickoff_location") var kickoff_location: String,
    @ColumnInfo(name = "start_location") var start_location: String,
    @ColumnInfo(name = "end_location") var end_location: String,
    @ColumnInfo(name = "matchStatus") var matchStatus: String = "Not Started",
)
