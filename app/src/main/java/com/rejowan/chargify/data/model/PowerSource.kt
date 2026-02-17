package com.rejowan.chargify.data.model

import android.os.BatteryManager

enum class PowerSource {
    AC,
    USB,
    WIRELESS,
    NONE;

    companion object {
        fun fromPluggedValue(plugged: Int): PowerSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> AC
            BatteryManager.BATTERY_PLUGGED_USB -> USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> WIRELESS
            else -> NONE
        }
    }

    fun displayName(): String = when (this) {
        AC -> "AC"
        USB -> "USB"
        WIRELESS -> "Wireless"
        NONE -> "Unplugged"
    }
}
