package com.rejowan.chargify.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charging_sessions")
data class ChargingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val startLevel: Int,
    val endLevel: Int,
    val isCharging: Boolean,
    val powerSource: String,
    val averageCurrentMa: Float = 0f,
    val averageTempCelsius: Float = 0f
) {
    val durationMillis: Long
        get() = endTime - startTime

    val levelChange: Int
        get() = endLevel - startLevel
}
