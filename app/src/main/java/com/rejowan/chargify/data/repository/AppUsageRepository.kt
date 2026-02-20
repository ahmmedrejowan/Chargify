package com.rejowan.chargify.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import com.rejowan.chargify.data.model.AppUsageInfo
import timber.log.Timber
import java.util.Calendar
import java.util.Date

class AppUsageRepository(private val context: Context) {

    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    private val packageManager: PackageManager by lazy {
        context.packageManager
    }

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        @Suppress("DEPRECATION")
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getUsageStatsSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    fun getAppUsageStats(timeRangeMs: Long = 24 * 60 * 60 * 1000L): List<AppUsageInfo> {
        if (!hasUsageStatsPermission()) {
            Timber.tag("AppUsage").w("No usage stats permission")
            return emptyList()
        }

        val endTime = System.currentTimeMillis()
        val startTime = endTime - timeRangeMs

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (usageStatsList.isNullOrEmpty()) {
            Timber.tag("AppUsage").d("No usage stats available")
            return emptyList()
        }

        // Aggregate stats by package name
        val aggregatedStats = mutableMapOf<String, UsageStats>()
        for (stats in usageStatsList) {
            if (stats.totalTimeInForeground > 0) {
                val existing = aggregatedStats[stats.packageName]
                if (existing == null || stats.lastTimeUsed > existing.lastTimeUsed) {
                    aggregatedStats[stats.packageName] = stats
                }
            }
        }

        Timber.tag("AppUsage").d("Found ${aggregatedStats.size} apps with usage")

        return aggregatedStats.values
            .filter { it.totalTimeInForeground > 60000 } // At least 1 minute
            .sortedByDescending { it.totalTimeInForeground }
            .take(20) // Top 20 apps
            .mapNotNull { stats -> createAppUsageInfo(stats) }
    }

    fun getTodayUsageStats(): List<AppUsageInfo> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        return getAppUsageStatsInRange(startOfDay, now)
    }

    fun getWeekUsageStats(): List<AppUsageInfo> {
        return getAppUsageStats(7 * 24 * 60 * 60 * 1000L)
    }

    fun getUsageStatsForDay(date: Date): List<AppUsageInfo> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis

        // For today, use current time as end
        val now = System.currentTimeMillis()
        val actualEndTime = if (endOfDay > now) now else endOfDay

        return getAppUsageStatsInRange(startOfDay, actualEndTime)
    }

    private fun getAppUsageStatsInRange(startTime: Long, endTime: Long): List<AppUsageInfo> {
        if (!hasUsageStatsPermission()) {
            return emptyList()
        }

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (usageStatsList.isNullOrEmpty()) {
            return emptyList()
        }

        val aggregatedStats = mutableMapOf<String, Long>()
        val lastUsedTimes = mutableMapOf<String, Long>()

        for (stats in usageStatsList) {
            if (stats.totalTimeInForeground > 0) {
                val currentTotal = aggregatedStats[stats.packageName] ?: 0L
                aggregatedStats[stats.packageName] = currentTotal + stats.totalTimeInForeground

                val currentLastUsed = lastUsedTimes[stats.packageName] ?: 0L
                if (stats.lastTimeUsed > currentLastUsed) {
                    lastUsedTimes[stats.packageName] = stats.lastTimeUsed
                }
            }
        }

        return aggregatedStats.entries
            .filter { it.value > 60000 }
            .sortedByDescending { it.value }
            .take(20)
            .mapNotNull { (packageName, totalTime) ->
                createAppUsageInfoFromData(
                    packageName,
                    totalTime,
                    lastUsedTimes[packageName] ?: 0L
                )
            }
    }

    private fun createAppUsageInfo(stats: UsageStats): AppUsageInfo? {
        return try {
            val appInfo = packageManager.getApplicationInfo(stats.packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val appIcon = packageManager.getApplicationIcon(appInfo)

            AppUsageInfo(
                packageName = stats.packageName,
                appName = appName,
                appIcon = appIcon,
                usageTimeMs = stats.totalTimeInForeground,
                lastUsedTime = stats.lastTimeUsed,
                foregroundTimeMs = stats.totalTimeInForeground
            )
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.tag("AppUsage").d("Package not found: ${stats.packageName}")
            null
        }
    }

    private fun createAppUsageInfoFromData(
        packageName: String,
        totalTime: Long,
        lastUsed: Long
    ): AppUsageInfo? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val appIcon = packageManager.getApplicationIcon(appInfo)

            AppUsageInfo(
                packageName = packageName,
                appName = appName,
                appIcon = appIcon,
                usageTimeMs = totalTime,
                lastUsedTime = lastUsed,
                foregroundTimeMs = totalTime
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun getTotalScreenTime(timeRangeMs: Long = 24 * 60 * 60 * 1000L): Long {
        val stats = getAppUsageStats(timeRangeMs)
        return stats.sumOf { it.usageTimeMs }
    }
}
