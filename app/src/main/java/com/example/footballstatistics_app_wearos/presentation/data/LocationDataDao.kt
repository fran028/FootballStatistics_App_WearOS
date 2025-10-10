package com.example.footballstatistics_app_wearos.presentation.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationData(locationData: LocationDataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationDataEntity>)

    @Query("SELECT * FROM location_data")
    suspend fun getAllLocationData(): List<LocationDataEntity>

    @Query("DELETE FROM location_data")
    suspend fun deleteAllLocationData()

    @Query("DELETE FROM location_data WHERE match_id = :id")
    suspend fun deleteLocationDataByMatchId(id: Int)

    @Query("UPDATE location_data SET latitude = :latitude, longitude = :longitude WHERE id = :id")
    suspend fun updateLocationDataById(id: Int, latitude: Double, longitude: Double)

    @Query("SELECT * FROM location_data ORDER BY id DESC LIMIT 1")
    suspend fun getLastLocationData(): LocationDataEntity?

    @Query("SELECT * FROM location_data ORDER BY id ASC LIMIT 1")
    suspend fun getFirstLocationData(): LocationDataEntity?

    @Query("SELECT * FROM location_data WHERE match_id = :matchId")
    suspend fun getLocationsForMatch(matchId: Int): List<LocationDataEntity>


}