package com.rejowan.chargify.data.repository

import com.rejowan.chargify.data.local.ChargingSession
import com.rejowan.chargify.data.local.ChargingSessionDao
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class ChargingHistoryRepository(
    private val dao: ChargingSessionDao
) {
    fun getAllSessions(): Flow<List<ChargingSession>> {
        Timber.tag("ChargeHistory").d("getAllSessions() called")
        return dao.getAllSessions()
    }

    fun getRecentSessions(limit: Int = 50): Flow<List<ChargingSession>> {
        Timber.tag("ChargeHistory").d("getRecentSessions(limit=$limit) called")
        return dao.getRecentSessions(limit)
    }

    suspend fun addSession(session: ChargingSession) {
        Timber.tag("ChargeHistory").d("addSession: ${session.startLevel}% -> ${session.endLevel}%, isCharging=${session.isCharging}")
        dao.insert(session)
        Timber.tag("ChargeHistory").d("Session inserted into database!")
    }

    suspend fun deleteSession(id: Long) {
        Timber.tag("ChargeHistory").d("deleteSession: id=$id")
        dao.deleteById(id)
    }

    suspend fun clearHistory() {
        Timber.tag("ChargeHistory").d("clearHistory() called")
        dao.deleteAll()
    }
}
