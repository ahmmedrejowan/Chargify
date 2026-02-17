package com.rejowan.chargify.worker

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rejowan.chargify.data.preferences.AlarmPreferences
import com.rejowan.chargify.notification.BatteryAlarmNotification
import timber.log.Timber

class BatterySamplingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "battery_sampling_work"
        private const val PREFS_NAME = "charging_session_prefs"
        private const val KEY_CURRENT_SAMPLES = "current_samples"
        private const val KEY_TEMP_SAMPLES = "temp_samples"
        private const val KEY_SAMPLE_COUNT = "sample_count"
    }

    override suspend fun doWork(): Result {
        Timber.d("BatterySamplingWorker: Sampling battery data")

        val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Get current usage
        val rawCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentMa = if (kotlin.math.abs(rawCurrent / 1000) < 1) {
            rawCurrent.toFloat()
        } else {
            rawCurrent / 1000f
        }

        // Get temperature
        val intent = applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = temp / 10f

        // Accumulate samples
        val currentTotal = prefs.getFloat(KEY_CURRENT_SAMPLES, 0f)
        val tempTotal = prefs.getFloat(KEY_TEMP_SAMPLES, 0f)
        val sampleCount = prefs.getInt(KEY_SAMPLE_COUNT, 0)

        prefs.edit()
            .putFloat(KEY_CURRENT_SAMPLES, currentTotal + kotlin.math.abs(currentMa))
            .putFloat(KEY_TEMP_SAMPLES, tempTotal + tempCelsius)
            .putInt(KEY_SAMPLE_COUNT, sampleCount + 1)
            .apply()

        Timber.d("Sampled: current=${currentMa}mA, temp=${tempCelsius}°C, total samples=${sampleCount + 1}")

        // Check battery alarms during sampling
        checkBatteryAlarms()

        return Result.success()
    }

    private fun checkBatteryAlarms() {
        val prefs = AlarmPreferences(applicationContext)
        val settings = prefs.settings.value

        // Check master toggle
        if (!settings.alarmsEnabled) {
            return
        }

        val intent = applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryLevel = if (scale > 0) (level * 100) / scale else 0

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val lastNotifiedLevel = prefs.getLastNotifiedLevel()

        // Don't notify if we already notified at this level
        if (lastNotifiedLevel == batteryLevel) {
            return
        }

        var shouldUpdateLastNotified = false

        // Check full charge alarm
        if (settings.fullChargeAlarmEnabled && isCharging) {
            if (batteryLevel >= settings.fullChargeThreshold) {
                Timber.tag("BatteryAlarm").d("Worker: Triggering full charge alarm at $batteryLevel%")
                BatteryAlarmNotification.showFullChargeNotification(
                    applicationContext,
                    batteryLevel,
                    settings.soundEnabled,
                    settings.vibrationEnabled
                )
                shouldUpdateLastNotified = true
            }
        }

        // Check custom alarm (battery health)
        if (settings.customAlarmEnabled && isCharging) {
            if (batteryLevel >= settings.customAlarmThreshold) {
                Timber.tag("BatteryAlarm").d("Worker: Triggering custom alarm at $batteryLevel%")
                BatteryAlarmNotification.showCustomAlarmNotification(
                    applicationContext,
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
    }
}
