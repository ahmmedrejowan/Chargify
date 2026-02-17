package com.rejowan.chargify.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.rejowan.chargify.data.preferences.AlarmPreferences
import com.rejowan.chargify.notification.BatteryAlarmNotification
import timber.log.Timber

class BatteryAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BATTERY_CHANGED) return

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val batteryLevel = if (scale > 0) (level * 100) / scale else 0

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        Timber.tag("BatteryAlarm").d("Battery check: level=$batteryLevel%, isCharging=$isCharging")

        val prefs = AlarmPreferences(context)
        val settings = prefs.settings.value
        val lastNotifiedLevel = prefs.getLastNotifiedLevel()

        // Don't notify if we already notified at this level
        if (lastNotifiedLevel == batteryLevel) {
            return
        }

        var shouldUpdateLastNotified = false

        // Check full charge alarm
        if (settings.fullChargeAlarmEnabled && isCharging) {
            if (batteryLevel >= settings.fullChargeThreshold) {
                Timber.tag("BatteryAlarm").d("Triggering full charge alarm at $batteryLevel%")
                BatteryAlarmNotification.showFullChargeNotification(
                    context,
                    batteryLevel,
                    settings.soundEnabled,
                    settings.vibrationEnabled
                )
                shouldUpdateLastNotified = true
            }
        }

        // Check low battery alarm
        if (settings.lowBatteryAlarmEnabled && !isCharging) {
            if (batteryLevel <= settings.lowBatteryThreshold) {
                Timber.tag("BatteryAlarm").d("Triggering low battery alarm at $batteryLevel%")
                BatteryAlarmNotification.showLowBatteryNotification(
                    context,
                    batteryLevel,
                    settings.soundEnabled,
                    settings.vibrationEnabled
                )
                shouldUpdateLastNotified = true
            }
        }

        // Check custom alarm
        if (settings.customAlarmEnabled) {
            if (isCharging && batteryLevel >= settings.customAlarmThreshold) {
                Timber.tag("BatteryAlarm").d("Triggering custom alarm at $batteryLevel%")
                BatteryAlarmNotification.showCustomAlarmNotification(
                    context,
                    batteryLevel,
                    settings.customAlarmThreshold,
                    isCharging,
                    settings.soundEnabled,
                    settings.vibrationEnabled
                )
                shouldUpdateLastNotified = true
            }
        }

        if (shouldUpdateLastNotified) {
            prefs.setLastNotifiedLevel(batteryLevel)
        }

        // Reset last notified when charging state changes significantly
        if (!isCharging && lastNotifiedLevel >= 80) {
            prefs.clearLastNotifiedLevel()
        }
        if (isCharging && lastNotifiedLevel <= 30) {
            prefs.clearLastNotifiedLevel()
        }
    }
}
