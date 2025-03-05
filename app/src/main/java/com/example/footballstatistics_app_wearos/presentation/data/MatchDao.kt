package com.example.footballstatistics_app_wearos.presentation.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("SELECT * FROM matches")
    suspend fun getAllMatches(): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: Int): MatchEntity?

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatchById(matchId: Int)

    @Query("DELETE FROM matches")
    suspend fun deleteAllMatches()

    suspend fun isThereAnyMatch(): Boolean {
        val matches = getAllMatches()
        return matches.isNotEmpty()
    }

    suspend fun isKickoffSet(): Boolean {
        val matches = getAllMatches()
        return matches.isNotEmpty() && matches[0].kickoff_location != null && matches[0].kickoff_location != ""
    }

    suspend fun isAwayCornersSet(): Boolean {
        val matches = getAllMatches()
        return matches.isNotEmpty() && matches[0].away_corner_location != null && matches[0].away_corner_location != ""
    }

    suspend fun isHomeCornersSet(): Boolean {
        val matches = getAllMatches()
        return matches.isNotEmpty() && matches[0].home_corner_location != null && matches[0].home_corner_location != ""
    }

    suspend fun isLocationSet(): Boolean {
        val matches = getAllMatches()
        val kickoff = isKickoffSet()
        val awayCorners = isAwayCornersSet()
        val homeCorners = isHomeCornersSet()
        return matches.isNotEmpty() && kickoff && awayCorners && homeCorners
    }

    suspend fun getMatchId(): Int {
        val matches = getAllMatches()
        return matches[0].id
    }

}