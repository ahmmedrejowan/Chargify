package com.rejowan.chargify.data.model

enum class BatteryHealthLevel {
    NORMAL,
    WARNING,
    CRITICAL;

    companion object {
        fun fromChargeLevel(level: Int): BatteryHealthLevel = when {
            level <= 10 -> CRITICAL
            level <= 30 -> WARNING
            else -> NORMAL
        }
    }
}
