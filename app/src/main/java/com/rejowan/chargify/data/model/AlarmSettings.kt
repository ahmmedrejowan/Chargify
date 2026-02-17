package com.rejowan.chargify.data.model

data class AlarmSettings(
    val alarmsEnabled: Boolean = true,
    val fullChargeAlarmEnabled: Boolean = false,
    val fullChargeThreshold: Int = 100,
    val lowBatteryAlarmEnabled: Boolean = false,
    val lowBatteryThreshold: Int = 20,
    val customAlarmEnabled: Boolean = false,
    val customAlarmThreshold: Int = 80,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)
