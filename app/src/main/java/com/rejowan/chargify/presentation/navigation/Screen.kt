package com.rejowan.chargify.presentation.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Main : Screen("main")

    // Tool screens
    data object ChargingAlarms : Screen("charging_alarms")
    data object ChargingHistory : Screen("charging_history")
    data object BatteryTips : Screen("battery_tips")
    data object AppUsage : Screen("app_usage")
}
