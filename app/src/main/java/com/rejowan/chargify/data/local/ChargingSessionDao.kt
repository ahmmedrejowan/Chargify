package com.rejowan.chargify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargingSessionDao {

    @Insert
    suspend fun insert(session: ChargingSession)

    @Query("SELECT * FROM charging_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ChargingSession>>

    @Query("SELECT * FROM charging_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<ChargingSession>>

    @Query("SELECT * FROM charging_sessions WHERE startTime >= :startOfDay ORDER BY startTime DESC")
    fun getSessionsFromToday(startOfDay: Long): Flow<List<ChargingSession>>

    @Query("DELETE FROM charging_sessions")
    suspend fun deleteAll()

    @Query("DELETE FROM charging_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
