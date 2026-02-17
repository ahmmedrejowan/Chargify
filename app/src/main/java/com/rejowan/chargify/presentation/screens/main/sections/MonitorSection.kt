package com.rejowan.chargify.presentation.screens.main.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rejowan.chargify.R
import com.rejowan.chargify.data.model.BatteryState
import com.rejowan.chargify.data.model.SessionStats
import com.rejowan.chargify.presentation.components.LineChartView
import com.rejowan.chargify.presentation.components.MetricCard
import com.rejowan.chargify.presentation.components.SectionHeader
import kotlin.math.abs

@Composable
fun MonitorSection(
    batteryState: BatteryState,
    currentHistory: List<Float>,
    tempHistory: List<Float>,
    voltageHistory: List<Float>,
    levelHistory: List<Float>,
    powerHistory: List<Float>,
    sessionStats: SessionStats,
    chargeSpeedText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = "Monitor")

        // 1. Energy Flow chart — full width
        MetricCard(
            modifier = Modifier.padding(horizontal = 16.dp),
            title = "Energy Flow",
            leadingIcon = R.drawable.ic_usage,
            value = "${batteryState.currentUsageMa.toInt()}",
            unit = "mA",
            chart = {
                LineChartView(
                    data = currentHistory,
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                )
            },
            minValue = currentHistory.minOrNull()?.let { "${it.toInt()}" } ?: "-",
            avgValue = currentHistory.takeIf { it.isNotEmpty() }
                ?.average()?.toInt()?.toString() ?: "-",
            maxValue = currentHistory.maxOrNull()?.let { "${it.toInt()}" } ?: "-"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Power (Watts) chart — full width
        MetricCard(
            modifier = Modifier.padding(horizontal = 16.dp),
            title = "Power",
            leadingIcon = R.drawable.ic_power,
            value = if (powerHistory.isNotEmpty()) "${"%.2f".format(powerHistory.last())}" else "0",
            unit = "W",
            chart = {
                LineChartView(
                    data = powerHistory,
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                )
            },
            minValue = powerHistory.minOrNull()?.let { "%.1f".format(it) } ?: "-",
            avgValue = powerHistory.takeIf { it.isNotEmpty() }
                ?.average()?.let { "%.1f".format(it) } ?: "-",
            maxValue = powerHistory.maxOrNull()?.let { "%.1f".format(it) } ?: "-"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Temperature chart — full width
        MetricCard(
            modifier = Modifier.padding(horizontal = 16.dp),
            title = "Temperature",
            leadingIcon = R.drawable.ic_temp,
            value = "${batteryState.temperatureCelsius}",
            unit = "\u00B0C",
            chart = {
                LineChartView(
                    data = tempHistory,
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                )
            },
            minValue = tempHistory.minOrNull()?.let { "%.1f".format(it) } ?: "-",
            avgValue = tempHistory.takeIf { it.isNotEmpty() }
                ?.average()?.let { "%.1f".format(it) } ?: "-",
            maxValue = tempHistory.maxOrNull()?.let { "%.1f".format(it) } ?: "-"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Voltage chart — full width
        MetricCard(
            modifier = Modifier.padding(horizontal = 16.dp),
            title = "Voltage",
            leadingIcon = R.drawable.ic_voltage,
            value = "${batteryState.voltage}",
            unit = "V",
            chart = {
                LineChartView(
                    data = voltageHistory,
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                )
            },
            minValue = voltageHistory.minOrNull()?.let { "%.2f".format(it) } ?: "-",
            avgValue = voltageHistory.takeIf { it.isNotEmpty() }
                ?.average()?.let { "%.2f".format(it) } ?: "-",
            maxValue = voltageHistory.maxOrNull()?.let { "%.2f".format(it) } ?: "-"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Battery Level chart — full width
        MetricCard(
            modifier = Modifier.padding(horizontal = 16.dp),
            title = "Battery Level",
            leadingIcon = R.drawable.ic_battery_level,
            value = "${batteryState.chargeLevel}",
            unit = "%",
            chart = {
                LineChartView(
                    data = levelHistory,
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                )
            },
            minValue = levelHistory.minOrNull()?.let { "${it.toInt()}" } ?: "-",
            avgValue = levelHistory.takeIf { it.isNotEmpty() }
                ?.average()?.toInt()?.toString() ?: "-",
            maxValue = levelHistory.maxOrNull()?.let { "${it.toInt()}" } ?: "-"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 6. Charging Speed gauge + 7. Session Stats — side by side
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Charging Speed gauge
            ChargingSpeedCard(
                currentMa = abs(batteryState.currentUsageMa),
                speedLabel = chargeSpeedText,
                isCharging = batteryState.isCharging,
                modifier = Modifier.weight(1f)
            )

            // Session Stats
            SessionStatsCard(
                sessionStats = sessionStats,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ChargingSpeedCard(
    currentMa: Float,
    speedLabel: String,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val maxCurrent = 5000f
    val progress = (currentMa / maxCurrent).coerceIn(0f, 1f)
    val speedColor = when {
        currentMa >= 3000 -> Color(0xFF4DD86C)
        currentMa >= 1500 -> MaterialTheme.colorScheme.tertiary
        currentMa >= 500 -> MaterialTheme.colorScheme.primary
        else -> Color(0xFFF5AD56)
    }

    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(speedColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_charging),
                        contentDescription = null,
                        tint = speedColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Speed",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isCharging && speedLabel.isNotEmpty()) speedLabel else "Not Charging",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = speedColor,
                trackColor = speedColor.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${currentMa.toInt()} / ${maxCurrent.toInt()} mA",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SessionStatsCard(
    sessionStats: SessionStats,
    modifier: Modifier = Modifier
) {
    val durationMillis = sessionStats.durationMillis
    val hours = (durationMillis / 3600000).toInt()
    val minutes = ((durationMillis % 3600000) / 60000).toInt()
    val seconds = ((durationMillis % 60000) / 1000).toInt()
    val durationText = when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }

    val levelChange = sessionStats.levelChange
    val levelText = when {
        levelChange > 0 -> "+$levelChange%"
        levelChange < 0 -> "$levelChange%"
        else -> "0%"
    }

    val accentColor = if (sessionStats.isCharging) Color(0xFF4DD86C)
        else MaterialTheme.colorScheme.primary

    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_time),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Session",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            SessionStatRow("Duration", durationText)
            Spacer(modifier = Modifier.height(4.dp))
            SessionStatRow("Level", levelText)
            Spacer(modifier = Modifier.height(4.dp))
            SessionStatRow("Avg Current", "${sessionStats.averageCurrentMa.toInt()} mA")
        }
    }
}

@Composable
private fun SessionStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
