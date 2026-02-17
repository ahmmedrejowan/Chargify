package com.rejowan.chargify.data.repository

import android.content.Intent
import com.rejowan.chargify.data.model.BatteryState
import com.rejowan.chargify.data.model.SessionStats
import kotlinx.coroutines.flow.StateFlow

interface BatteryRepository {
    val batteryState: StateFlow<BatteryState>
    val currentUsageHistory: StateFlow<List<Float>>
    val temperatureHistory: StateFlow<List<Float>>
    val voltageHistory: StateFlow<List<Float>>
    val batteryLevelHistory: StateFlow<List<Float>>
    val powerHistory: StateFlow<List<Float>>
    val sessionStats: StateFlow<SessionStats>

    fun processIntent(intent: Intent)
    fun startMonitoring()
    fun stopMonitoring()
}
