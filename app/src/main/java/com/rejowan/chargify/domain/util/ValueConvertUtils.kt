package com.rejowan.chargify.domain.util

object ValueConvertUtils {

    fun convertMillisToHoursAndMinutes(millis: Long?): String {
        if (millis == null || millis <= 0) return "N/A"
        val totalMinutes = millis / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) {
            String.format("%d hr %d min", hours, minutes)
        } else {
            String.format("%d min", minutes)
        }
    }
}
