package com.rejowan.chargify.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.BatteryManager
import com.rejowan.chargify.data.model.BatteryHealthLevel
import com.rejowan.chargify.data.model.BatteryState
import com.rejowan.chargify.data.model.ChargingStatus
import com.rejowan.chargify.data.model.SessionStats
import com.rejowan.chargify.data.preferences.AppPreferences
import com.rejowan.chargify.data.repository.BatteryRepository
import com.rejowan.chargify.domain.util.ValueConvertUtils
import kotlin.math.abs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val repository: BatteryRepository,
    val appPreferences: AppPreferences
) : ViewModel() {

    val batteryState: StateFlow<BatteryState> = repository.batteryState

    val currentUsageHistory: StateFlow<List<Float>> = repository.currentUsageHistory
    val temperatureHistory: StateFlow<List<Float>> = repository.temperatureHistory
    val voltageHistory: StateFlow<List<Float>> = repository.voltageHistory
    val batteryLevelHistory: StateFlow<List<Float>> = repository.batteryLevelHistory
    val powerHistory: StateFlow<List<Float>> = repository.powerHistory
    val sessionStats: StateFlow<SessionStats> = repository.sessionStats

    val healthLevel: StateFlow<BatteryHealthLevel> = batteryState
        .map { BatteryHealthLevel.fromChargeLevel(it.chargeLevel) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BatteryHealthLevel.NORMAL)

    val statusText: StateFlow<String> = batteryState
        .map { state ->
            when {
                state.chargingStatus == ChargingStatus.FULL -> "Fully Charged"
                state.isCharging -> "Charging"
                state.chargeLevel <= 10 -> "Nearly Empty"
                state.chargeLevel <= 30 -> "Low Battery"
                else -> "Discharging"
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Unknown")

    val formattedEta: StateFlow<String> = batteryState
        .map { ValueConvertUtils.convertMillisToHoursAndMinutes(it.etaToFullMillis) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Estimating...")

    val formattedTimeRemaining: StateFlow<String> = batteryState
        .map { ValueConvertUtils.convertMillisToHoursAndMinutes(it.timeRemainingMillis) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Estimating...")

    val chargeSpeedText: StateFlow<String> = batteryState
        .map { state ->
            if (!state.isCharging) "" else {
                val absCurrent = abs(state.currentUsageMa)
                when {
                    absCurrent >= 3000 -> "Rapid Charging"
                    absCurrent >= 1500 -> "Fast Charging"
                    absCurrent >= 500 -> "Charging"
                    absCurrent > 0 -> "Slow Charging"
                    else -> "Charging"
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val healthText: StateFlow<String> = batteryState
        .map { state ->
            when (state.batteryHealth) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Unknown")

    fun processIntent(intent: Intent) {
        repository.processIntent(intent)
    }

    fun startMonitoring() {
        repository.startMonitoring()
    }

    fun stopMonitoring() {
        repository.stopMonitoring()
    }
}
