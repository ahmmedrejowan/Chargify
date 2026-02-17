package com.rejowan.chargify.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rejowan.chargify.MainActivity
import com.rejowan.chargify.R

object BatteryAlarmNotification {

    private const val CHANNEL_ID = "battery_alarm_channel"
    private const val CHANNEL_NAME = "Battery Alarms"
    private const val NOTIFICATION_ID_FULL_CHARGE = 1001
    private const val NOTIFICATION_ID_LOW_BATTERY = 1002
    private const val NOTIFICATION_ID_CUSTOM = 1003

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for battery level alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showFullChargeNotification(context: Context, level: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        showNotification(
            context = context,
            notificationId = NOTIFICATION_ID_FULL_CHARGE,
            title = "Battery Full",
            message = "Battery is at $level%. You can unplug your charger now.",
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled
        )
    }

    fun showLowBatteryNotification(context: Context, level: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        showNotification(
            context = context,
            notificationId = NOTIFICATION_ID_LOW_BATTERY,
            title = "Low Battery",
            message = "Battery is at $level%. Please charge your device.",
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled
        )
    }

    fun showCustomAlarmNotification(context: Context, level: Int, threshold: Int, isCharging: Boolean, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        val message = if (isCharging) {
            "Battery reached $level%. Consider unplugging to preserve battery health."
        } else {
            "Battery is at $level%."
        }

        showNotification(
            context = context,
            notificationId = NOTIFICATION_ID_CUSTOM,
            title = "Battery Alert",
            message = message,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled
        )
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (soundEnabled) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        } else {
            builder.setSilent(true)
        }

        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 500, 200, 500))
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun cancelAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_FULL_CHARGE)
        notificationManager.cancel(NOTIFICATION_ID_LOW_BATTERY)
        notificationManager.cancel(NOTIFICATION_ID_CUSTOM)
    }
}
