package com.rejowan.chargify.domain.util

import kotlin.math.abs

object BatteryCalculator {

    /**
     * Calculate ETA to full charge in milliseconds.
     * @param chargeLevel current battery percentage (0-100)
     * @param batteryCapacityMah total battery capacity in mAh
     * @param avgCurrentMa average charging current in mA (positive value)
     * @return estimated time to full charge in milliseconds, or null if cannot calculate
     */
    fun calculateEtaToFull(
        chargeLevel: Int,
        batteryCapacityMah: Int,
        avgCurrentMa: Double
    ): Long? {
        if (avgCurrentMa <= 0 || batteryCapacityMah <= 0 || chargeLevel >= 100) return null
        val remainingMah = (100 - chargeLevel) / 100.0 * batteryCapacityMah
        val etaHours = abs(remainingMah / avgCurrentMa)
        return (etaHours * 3600_000).toLong()
    }

    /**
     * Calculate time remaining until battery depletes in milliseconds.
     * @param chargeLevel current battery percentage (0-100)
     * @param batteryCapacityMah total battery capacity in mAh
     * @param avgCurrentMa average discharge current in mA (positive value)
     * @return estimated time remaining in milliseconds, or null if cannot calculate
     */
    fun calculateTimeRemaining(
        chargeLevel: Int,
        batteryCapacityMah: Int,
        avgCurrentMa: Double
    ): Long? {
        if (avgCurrentMa <= 0 || batteryCapacityMah <= 0 || chargeLevel <= 0) return null
        val remainingMah = chargeLevel / 100.0 * batteryCapacityMah
        val timeHours = abs(remainingMah / avgCurrentMa)
        return abs((timeHours * 3600_000).toLong())
    }

    /**
     * Validate that a set of current samples are consistent (all same sign).
     * Returns true if all samples are in the same state (all charging or all discharging).
     */
    fun areSamplesConsistent(samples: List<Float>): Boolean {
        if (samples.isEmpty()) return false
        val sumOfValues = abs(samples.sum())
        val sumOfAbsValues = samples.sumOf { abs(it.toDouble()) }.toFloat()
        return sumOfValues == sumOfAbsValues
    }
}
