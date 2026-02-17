package com.rejowan.chargify.data.model

data class SessionStats(
    val isCharging: Boolean = false,
    val startLevel: Int = 0,
    val currentLevel: Int = 0,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val sampleCount: Int = 0,
    val totalCurrentMa: Float = 0f
) {
    val levelChange: Int get() = currentLevel - startLevel
    val durationMillis: Long get() = System.currentTimeMillis() - startTimeMillis
    val averageCurrentMa: Float get() = if (sampleCount > 0) totalCurrentMa / sampleCount else 0f
}
