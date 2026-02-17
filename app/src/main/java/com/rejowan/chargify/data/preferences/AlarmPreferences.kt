package com.rejowan.chargify.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.rejowan.chargify.data.model.AlarmSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlarmPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AlarmSettings> = _settings.asStateFlow()

    companion object {
        private const val PREFS_NAME = "alarm_preferences"
        private const val KEY_ALARMS_ENABLED = "alarms_enabled"
        private const val KEY_FULL_CHARGE_ENABLED = "full_charge_enabled"
        private const val KEY_FULL_CHARGE_THRESHOLD = "full_charge_threshold"
        private const val KEY_LOW_BATTERY_ENABLED = "low_battery_enabled"
        private const val KEY_LOW_BATTERY_THRESHOLD = "low_battery_threshold"
        private const val KEY_CUSTOM_ALARM_ENABLED = "custom_alarm_enabled"
        private const val KEY_CUSTOM_ALARM_THRESHOLD = "custom_alarm_threshold"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_LAST_NOTIFIED_LEVEL = "last_notified_level"
    }

    private fun loadSettings(): AlarmSettings {
        return AlarmSettings(
            alarmsEnabled = prefs.getBoolean(KEY_ALARMS_ENABLED, true),
            fullChargeAlarmEnabled = prefs.getBoolean(KEY_FULL_CHARGE_ENABLED, false),
            fullChargeThreshold = prefs.getInt(KEY_FULL_CHARGE_THRESHOLD, 100),
            lowBatteryAlarmEnabled = prefs.getBoolean(KEY_LOW_BATTERY_ENABLED, false),
            lowBatteryThreshold = prefs.getInt(KEY_LOW_BATTERY_THRESHOLD, 20),
            customAlarmEnabled = prefs.getBoolean(KEY_CUSTOM_ALARM_ENABLED, false),
            customAlarmThreshold = prefs.getInt(KEY_CUSTOM_ALARM_THRESHOLD, 80),
            soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
            vibrationEnabled = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        )
    }

    fun updateSettings(settings: AlarmSettings) {
        prefs.edit()
            .putBoolean(KEY_ALARMS_ENABLED, settings.alarmsEnabled)
            .putBoolean(KEY_FULL_CHARGE_ENABLED, settings.fullChargeAlarmEnabled)
            .putInt(KEY_FULL_CHARGE_THRESHOLD, settings.fullChargeThreshold)
            .putBoolean(KEY_LOW_BATTERY_ENABLED, settings.lowBatteryAlarmEnabled)
            .putInt(KEY_LOW_BATTERY_THRESHOLD, settings.lowBatteryThreshold)
            .putBoolean(KEY_CUSTOM_ALARM_ENABLED, settings.customAlarmEnabled)
            .putInt(KEY_CUSTOM_ALARM_THRESHOLD, settings.customAlarmThreshold)
            .putBoolean(KEY_SOUND_ENABLED, settings.soundEnabled)
            .putBoolean(KEY_VIBRATION_ENABLED, settings.vibrationEnabled)
            .apply()
        _settings.value = settings
    }

    fun setAlarmsEnabled(enabled: Boolean) {
        val current = _settings.value
        updateSettings(current.copy(alarmsEnabled = enabled))
    }

    fun setFullChargeAlarm(enabled: Boolean, threshold: Int = 100) {
        val current = _settings.value
        updateSettings(current.copy(fullChargeAlarmEnabled = enabled, fullChargeThreshold = threshold))
    }

    fun setLowBatteryAlarm(enabled: Boolean, threshold: Int = 20) {
        val current = _settings.value
        updateSettings(current.copy(lowBatteryAlarmEnabled = enabled, lowBatteryThreshold = threshold))
    }

    fun setCustomAlarm(enabled: Boolean, threshold: Int = 80) {
        val current = _settings.value
        updateSettings(current.copy(customAlarmEnabled = enabled, customAlarmThreshold = threshold))
    }

    // Track last notified level to avoid repeated notifications
    fun getLastNotifiedLevel(): Int = prefs.getInt(KEY_LAST_NOTIFIED_LEVEL, -1)

    fun setLastNotifiedLevel(level: Int) {
        prefs.edit().putInt(KEY_LAST_NOTIFIED_LEVEL, level).apply()
    }

    fun clearLastNotifiedLevel() {
        prefs.edit().remove(KEY_LAST_NOTIFIED_LEVEL).apply()
    }
}
