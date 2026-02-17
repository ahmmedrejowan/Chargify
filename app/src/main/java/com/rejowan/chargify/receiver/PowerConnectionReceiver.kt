package com.rejowan.chargify.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rejowan.chargify.data.local.ChargingSession
import com.rejowan.chargify.data.preferences.AlarmPreferences
import com.rejowan.chargify.data.repository.ChargingHistoryRepository
import com.rejowan.chargify.notification.BatteryAlarmNotification
import com.rejowan.chargify.worker.BatterySamplingWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PowerConnectionReceiver : BroadcastReceiver(), KoinComponent {

    private val chargingHistoryRepository: ChargingHistoryRepository by inject()

    companion object {
        private const val PREFS_NAME = "charging_session_prefs"
        private const val KEY_SESSION_START_TIME = "session_start_time"
        private const val KEY_SESSION_START_LEVEL = "session_start_level"
        private const val KEY_SESSION_IS_CHARGING = "session_is_charging"
        private const val KEY_SESSION_POWER_SOURCE = "session_power_source"
        private const val KEY_CURRENT_SAMPLES = "current_samples"
        private const val KEY_TEMP_SAMPLES = "temp_samples"
        private const val KEY_SAMPLE_COUNT = "sample_count"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Timber.tag("ChargeHistory").d("=== PowerConnectionReceiver received: $action ===")

        when (action) {
            Intent.ACTION_POWER_CONNECTED -> {
                Timber.tag("ChargeHistory").d("Power CONNECTED - handling state change")
                handlePowerStateChange(context, isNowCharging = true)
                startSamplingWorker(context)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                Timber.tag("ChargeHistory").d("Power DISCONNECTED - handling state change")
                handlePowerStateChange(context, isNowCharging = false)
                stopSamplingWorker(context)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Timber.tag("ChargeHistory").d("BOOT COMPLETED - initializing")
                handleBootCompleted(context)
            }
        }
    }

    private fun handlePowerStateChange(context: Context, isNowCharging: Boolean) {
        Timber.tag("ChargeHistory").d("handlePowerStateChange: isNowCharging=$isNowCharging")

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val batteryStatus = getBatteryStatus(context)
        Timber.tag("ChargeHistory").d("Current battery: level=${batteryStatus.level}%, temp=${batteryStatus.temperature}, source=${batteryStatus.powerSource}")

        val previousStartTime = prefs.getLong(KEY_SESSION_START_TIME, 0L)
        val previousStartLevel = prefs.getInt(KEY_SESSION_START_LEVEL, -1)
        val previousWasCharging = prefs.getBoolean(KEY_SESSION_IS_CHARGING, false)
        val previousPowerSource = prefs.getString(KEY_SESSION_POWER_SOURCE, "Battery") ?: "Battery"
        Timber.tag("ChargeHistory").d("Previous session: startTime=$previousStartTime, startLevel=$previousStartLevel, wasCharging=$previousWasCharging")

        // Get accumulated samples
        val currentTotal = prefs.getFloat(KEY_CURRENT_SAMPLES, 0f)
        val tempTotal = prefs.getFloat(KEY_TEMP_SAMPLES, 0f)
        val sampleCount = prefs.getInt(KEY_SAMPLE_COUNT, 0)
        Timber.tag("ChargeHistory").d("Accumulated samples: count=$sampleCount, currentTotal=$currentTotal, tempTotal=$tempTotal")

        val currentLevel = batteryStatus.level
        val currentTime = System.currentTimeMillis()

        // Save previous session if valid (> 1 minute and level changed)
        if (previousStartTime > 0 && previousStartLevel >= 0) {
            val duration = currentTime - previousStartTime
            val levelChange = currentLevel - previousStartLevel
            Timber.tag("ChargeHistory").d("Session check: duration=${duration}ms, levelChange=$levelChange")

            if (duration > 60_000 && levelChange != 0) {
                val avgCurrent = if (sampleCount > 0) currentTotal / sampleCount else 0f
                val avgTemp = if (sampleCount > 0) tempTotal / sampleCount else batteryStatus.temperature

                val session = ChargingSession(
                    startTime = previousStartTime,
                    endTime = currentTime,
                    startLevel = previousStartLevel,
                    endLevel = currentLevel,
                    isCharging = previousWasCharging,
                    powerSource = previousPowerSource,
                    averageCurrentMa = avgCurrent,
                    averageTempCelsius = avgTemp
                )

                Timber.tag("ChargeHistory").d("SAVING SESSION: $previousStartLevel% -> $currentLevel%, duration=${duration/1000}s")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        chargingHistoryRepository.addSession(session)
                        Timber.tag("ChargeHistory").d("SESSION SAVED SUCCESSFULLY!")
                    } catch (e: Exception) {
                        Timber.tag("ChargeHistory").e(e, "FAILED TO SAVE SESSION!")
                    }
                }
            } else {
                Timber.tag("ChargeHistory").d("Session NOT saved: duration=${duration}ms (need >60000), levelChange=$levelChange (need !=0)")
            }
        } else {
            Timber.tag("ChargeHistory").d("No previous session to save (first run or no startTime)")
        }

        // Clear samples and start new session
        prefs.edit()
            .putLong(KEY_SESSION_START_TIME, currentTime)
            .putInt(KEY_SESSION_START_LEVEL, currentLevel)
            .putBoolean(KEY_SESSION_IS_CHARGING, isNowCharging)
            .putString(KEY_SESSION_POWER_SOURCE, batteryStatus.powerSource)
            .putFloat(KEY_CURRENT_SAMPLES, 0f)
            .putFloat(KEY_TEMP_SAMPLES, 0f)
            .putInt(KEY_SAMPLE_COUNT, 0)
            .apply()

        Timber.tag("ChargeHistory").d("Started NEW ${if (isNowCharging) "charging" else "discharge"} session at $currentLevel%")

        // Check battery alarms
        checkBatteryAlarms(context, currentLevel, isNowCharging)
    }

    private fun handleBootCompleted(context: Context) {
        Timber.d("Boot completed - checking charging state")

        val batteryStatus = getBatteryStatus(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Check if currently charging
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // Initialize new session after boot
        val currentTime = System.currentTimeMillis()
        prefs.edit()
            .putLong(KEY_SESSION_START_TIME, currentTime)
            .putInt(KEY_SESSION_START_LEVEL, batteryStatus.level)
            .putBoolean(KEY_SESSION_IS_CHARGING, isCharging)
            .putString(KEY_SESSION_POWER_SOURCE, batteryStatus.powerSource)
            .putFloat(KEY_CURRENT_SAMPLES, 0f)
            .putFloat(KEY_TEMP_SAMPLES, 0f)
            .putInt(KEY_SAMPLE_COUNT, 0)
            .apply()

        // Start worker if charging
        if (isCharging) {
            startSamplingWorker(context)
        }

        Timber.d("Boot: Initialized ${if (isCharging) "charging" else "discharge"} session at ${batteryStatus.level}%")
    }

    private fun startSamplingWorker(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<BatterySamplingWorker>(
            15, TimeUnit.MINUTES // Minimum interval for WorkManager
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BatterySamplingWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        Timber.d("Started battery sampling worker")
    }

    private fun stopSamplingWorker(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(BatterySamplingWorker.WORK_NAME)
        Timber.d("Stopped battery sampling worker")
    }

    private fun getBatteryStatus(context: Context): BatteryStatus {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val levelPercent = if (scale > 0) (level * 100) / scale else 0

        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = temp / 10.0f

        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val powerSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Battery"
        }

        return BatteryStatus(levelPercent, tempCelsius, powerSource)
    }

    private data class BatteryStatus(
        val level: Int,
        val temperature: Float,
        val powerSource: String
    )

    private fun checkBatteryAlarms(context: Context, batteryLevel: Int, isCharging: Boolean) {
        val prefs = AlarmPreferences(context)
        val settings = prefs.settings.value

        // Check master toggle
        if (!settings.alarmsEnabled) {
            return
        }

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
