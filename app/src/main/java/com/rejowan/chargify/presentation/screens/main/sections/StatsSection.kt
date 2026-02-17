package com.rejowan.chargify.presentation.screens.main.sections

import android.os.BatteryManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rejowan.chargify.R
import com.rejowan.chargify.data.model.BatteryState
import com.rejowan.chargify.data.model.SessionStats
import com.rejowan.chargify.presentation.components.SectionHeader
import kotlin.math.abs

@Composable
fun StatsSection(
    batteryState: BatteryState,
    tempHistory: List<Float>,
    voltageHistory: List<Float>,
    currentHistory: List<Float>,
    powerHistory: List<Float>,
    sessionStats: SessionStats,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = "Stats")

        // 1. Capacity & Charge card — full width hero
        CapacityCard(batteryState = batteryState)

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Battery Health + Technology — half width each
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BatteryHealthCardCompact(
                batteryState = batteryState,
                modifier = Modifier.weight(1f)
            )
            DeviceInfoCard(
                label = "Technology",
                value = batteryState.batteryTechnology,
                iconRes = R.drawable.ic_technology,
                accentColor = Color(0xFF9575CD),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Range stats — 2-column grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RangeCard(
                label = "Temperature",
                iconRes = R.drawable.ic_temp,
                accentColor = Color(0xFF4DD86C),
                minVal = tempHistory.minOrNull()?.let { "%.1f°C".format(it) } ?: "--",
                maxVal = tempHistory.maxOrNull()?.let { "%.1f°C".format(it) } ?: "--",
                modifier = Modifier.weight(1f)
            )
            RangeCard(
                label = "Voltage",
                iconRes = R.drawable.ic_voltage,
                accentColor = Color(0xFFF5AD56),
                minVal = voltageHistory.minOrNull()?.let { "%.2f V".format(it) } ?: "--",
                maxVal = voltageHistory.maxOrNull()?.let { "%.2f V".format(it) } ?: "--",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RangeCard(
                label = "Current",
                iconRes = R.drawable.ic_usage,
                accentColor = MaterialTheme.colorScheme.primary,
                minVal = currentHistory.minOrNull()?.let { "${it.toInt()} mA" } ?: "--",
                maxVal = currentHistory.maxOrNull()?.let { "${it.toInt()} mA" } ?: "--",
                modifier = Modifier.weight(1f)
            )
            RangeCard(
                label = "Power",
                iconRes = R.drawable.ic_power,
                accentColor = MaterialTheme.colorScheme.tertiary,
                minVal = powerHistory.minOrNull()?.let { "%.1f W".format(it) } ?: "--",
                maxVal = powerHistory.maxOrNull()?.let { "%.1f W".format(it) } ?: "--",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Session Summary card — full width
        SessionSummaryCard(sessionStats = sessionStats, powerHistory = powerHistory)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CapacityCard(
    batteryState: BatteryState,
    modifier: Modifier = Modifier
) {
    val designCapacity = batteryState.batteryCapacityMah
    val currentCharge = batteryState.chargeCounterMah
    val cycleCount = batteryState.cycleCount
    val energyWh = batteryState.energyCounterWh

    // Calculate charge percentage of design capacity
    val chargeOfDesign = if (designCapacity != null && currentCharge != null && designCapacity > 0) {
        ((currentCharge / designCapacity) * 100).toInt()
    } else null

    val accentColor = MaterialTheme.colorScheme.primary

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            // Gradient accent strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(accentColor, MaterialTheme.colorScheme.tertiary)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header row with icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_capacity),
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Battery Capacity",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Design Capacity
                    Column {
                        Text(
                            text = "Design",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = designCapacity?.let { "$it mAh" } ?: "N/A",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Current Charge
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Current Charge",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentCharge?.let { "${it.toInt()} mAh" } ?: "N/A",
                            style = MaterialTheme.typography.titleMedium,
                            color = accentColor
                        )
                    }

                    // Charge of Design %
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Of Design",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = chargeOfDesign?.let { "$it%" } ?: "N/A",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Progress bar showing current charge vs design
                if (chargeOfDesign != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (chargeOfDesign / 100f).coerceIn(0f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(accentColor, MaterialTheme.colorScheme.tertiary)
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Secondary metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Cycle Count
                    Column {
                        Text(
                            text = "Cycle Count",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = cycleCount?.let { "$it cycles" } ?: "N/A",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Energy remaining
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Energy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = energyWh?.let { "%.2f Wh".format(it) } ?: "N/A",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BatteryHealthCardCompact(
    batteryState: BatteryState,
    modifier: Modifier = Modifier
) {
    val healthLabel = when (batteryState.batteryHealth) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "Unknown"
    }

    val healthColor = when (batteryState.batteryHealth) {
        BatteryManager.BATTERY_HEALTH_GOOD -> Color(0xFF4DD86C)
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> Color(0xFFEF5350)
        BatteryManager.BATTERY_HEALTH_DEAD -> Color(0xFFEF5350)
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> Color(0xFFF5AD56)
        BatteryManager.BATTERY_HEALTH_COLD -> Color(0xFF64B5F6)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    OutlinedCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Health indicator dot
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(healthColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(healthColor)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Health",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = healthLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = healthColor
                )
            }
        }
    }
}

@Composable
private fun RangeCard(
    label: String,
    iconRes: Int,
    accentColor: Color,
    minVal: String,
    maxVal: String,
    modifier: Modifier = Modifier
) {
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
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = minVal,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Max",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = maxVal,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoCard(
    label: String,
    value: String,
    iconRes: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SessionSummaryCard(
    sessionStats: SessionStats,
    powerHistory: List<Float>,
    modifier: Modifier = Modifier
) {
    val durationMillis = sessionStats.durationMillis
    val hours = (durationMillis / 3600000).toInt()
    val minutes = ((durationMillis % 3600000) / 60000).toInt()
    val durationText = when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }

    val levelChange = sessionStats.levelChange
    val levelText = when {
        levelChange > 0 -> "+$levelChange%"
        levelChange < 0 -> "$levelChange%"
        else -> "0%"
    }

    val avgPower = if (powerHistory.isNotEmpty()) {
        "%.1f W".format(powerHistory.average())
    } else "--"

    val accentColor = if (sessionStats.isCharging) Color(0xFF4DD86C)
        else MaterialTheme.colorScheme.primary

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.3f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (sessionStats.isCharging) "Charging Session" else "Discharge Session",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                SummaryRow("Duration", durationText)
                Spacer(modifier = Modifier.height(6.dp))
                SummaryRow("Level Change", levelText)
                Spacer(modifier = Modifier.height(6.dp))
                SummaryRow(
                    "Avg Current",
                    "${abs(sessionStats.averageCurrentMa).toInt()} mA"
                )
                Spacer(modifier = Modifier.height(6.dp))
                SummaryRow("Avg Power", avgPower)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
