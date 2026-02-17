package com.rejowan.chargify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.rejowan.chargify.data.model.AlarmSettings
import com.rejowan.chargify.data.preferences.AlarmPreferences
import kotlinx.coroutines.flow.StateFlow

class ChargingAlarmsViewModel(
    private val alarmPreferences: AlarmPreferences
) : ViewModel() {

    val settings: StateFlow<AlarmSettings> = alarmPreferences.settings

    fun setAlarmsEnabled(enabled: Boolean) {
        alarmPreferences.setAlarmsEnabled(enabled)
    }

    fun setFullChargeAlarm(enabled: Boolean) {
        alarmPreferences.setFullChargeAlarm(enabled, settings.value.fullChargeThreshold)
    }

    fun setFullChargeThreshold(threshold: Int) {
        alarmPreferences.setFullChargeAlarm(settings.value.fullChargeAlarmEnabled, threshold)
    }

    fun setLowBatteryAlarm(enabled: Boolean) {
        alarmPreferences.setLowBatteryAlarm(enabled, settings.value.lowBatteryThreshold)
    }

    fun setLowBatteryThreshold(threshold: Int) {
        alarmPreferences.setLowBatteryAlarm(settings.value.lowBatteryAlarmEnabled, threshold)
    }

    fun setCustomAlarm(enabled: Boolean) {
        alarmPreferences.setCustomAlarm(enabled, settings.value.customAlarmThreshold)
    }

    fun setCustomAlarmThreshold(threshold: Int) {
        alarmPreferences.setCustomAlarm(settings.value.customAlarmEnabled, threshold)
    }

    fun setSoundEnabled(enabled: Boolean) {
        val current = settings.value
        alarmPreferences.updateSettings(current.copy(soundEnabled = enabled))
    }

    fun setVibrationEnabled(enabled: Boolean) {
        val current = settings.value
        alarmPreferences.updateSettings(current.copy(vibrationEnabled = enabled))
    }
}
