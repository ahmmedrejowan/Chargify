package com.rejowan.chargify.data.model

import android.os.BatteryManager

data class BatteryState(
    val chargeLevel: Int = 0,
    val isCharging: Boolean = false,
    val chargingStatus: ChargingStatus = ChargingStatus.UNKNOWN,
    val powerSource: PowerSource = PowerSource.NONE,
    val temperatureCelsius: Float = 0f,
    val temperatureFahrenheit: Float = 0f,
    val voltage: Float = 0f,
    val currentUsageMa: Float = 0f,
    val currentAverageMa: Float = 0f,
    val batteryCapacityMah: Int? = null,
    val chargeCounterMah: Float? = null,
    val energyCounterWh: Float? = null,
    val cycleCount: Int? = null,
    val etaToFullMillis: Long? = null,
    val timeRemainingMillis: Long? = null,
    val batteryHealth: Int = BatteryManager.BATTERY_HEALTH_UNKNOWN,
    val batteryTechnology: String = "Unknown"
)
