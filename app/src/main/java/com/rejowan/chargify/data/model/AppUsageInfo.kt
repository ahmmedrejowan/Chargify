package com.rejowan.chargify.data.model

import android.graphics.drawable.Drawable

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val usageTimeMs: Long,
    val lastUsedTime: Long,
    val foregroundTimeMs: Long = 0L
) {
    val usageTimeFormatted: String
        get() {
            val hours = usageTimeMs / (1000 * 60 * 60)
            val minutes = (usageTimeMs % (1000 * 60 * 60)) / (1000 * 60)
            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "<1m"
            }
        }

    val usagePercentage: Float
        get() = 0f // Will be calculated relative to total
}
