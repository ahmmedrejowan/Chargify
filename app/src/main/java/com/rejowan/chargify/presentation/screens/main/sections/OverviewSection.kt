package com.rejowan.chargify.presentation.screens.main.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rejowan.chargify.R
import com.rejowan.chargify.data.model.BatteryState
import com.rejowan.chargify.data.model.PowerSource
import com.rejowan.chargify.presentation.components.WaveDirection
import com.rejowan.chargify.presentation.components.WaveProgress

@Composable
fun OverviewSection(
    batteryState: BatteryState,
    statusText: String,
    formattedEta: String,
    formattedTimeRemaining: String,
    chargeSpeedText: String,
    healthText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(24.dp))

        // Wave progress circle
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .size(250.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .border(5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                WaveProgress(
                    progress = batteryState.chargeLevel / 100f,
                    modifier = Modifier.fillMaxSize(),
                    fillBrush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            if (batteryState.isCharging) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.error
                        )
                    ),
                    waveDirection = WaveDirection.RIGHT,
                    amplitudeRange = 20f..50f,
                    waveFrequency = 3,
                    waveSteps = 20,
                    phaseShiftDuration = 2000,
                    amplitudeDuration = 2000
                ) {
                    // Overlay content
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${batteryState.chargeLevel}",
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                                    color = Color.White
                                )
                                Text(
                                    text = "%",
                                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 13.sp),
                                    color = Color.White
                                )
                            }

                            OutlinedCard(
                                colors = CardDefaults.elevatedCardColors().copy(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (batteryState.isCharging) R.drawable.ic_charging
                                            else R.drawable.ic_discharging
                                        ),
                                        contentDescription = statusText,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status badges row: Power Source + Charge Speed
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            // Power source badge
            if (batteryState.isCharging && batteryState.powerSource != PowerSource.NONE) {
                StatusBadge(
                    label = "via ${batteryState.powerSource.displayName()}",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Charge speed badge
            if (chargeSpeedText.isNotEmpty()) {
                StatusBadge(
                    label = chargeSpeedText,
                    color = when {
                        chargeSpeedText.contains("Rapid") -> Color(0xFF4DD86C)
                        chargeSpeedText.contains("Fast") -> MaterialTheme.colorScheme.tertiary
                        chargeSpeedText.contains("Slow") -> Color(0xFFF5AD56)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ETA to Full / Time Left card - full width
        val etaAccent = if (batteryState.isCharging) Color(0xFF4DD86C)
            else MaterialTheme.colorScheme.primary
        OutlinedCard(
            modifier = Modifier
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
                                listOf(etaAccent, MaterialTheme.colorScheme.tertiary)
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(etaAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_time),
                            contentDescription = null,
                            tint = etaAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (batteryState.isCharging) "ETA to Full" else "Time Left",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (batteryState.isCharging) formattedEta else formattedTimeRemaining,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Capacity + Health cards row
        val healthColor = when (healthText) {
            "Good" -> Color(0xFF4DD86C)
            "Overheat", "Over Voltage" -> Color(0xFFEF5350)
            "Cold" -> Color(0xFF64B5F6)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricIconCard(
                label = "Capacity",
                value = batteryState.batteryCapacityMah?.toString() ?: "N/A",
                unit = if (batteryState.batteryCapacityMah != null) "mAh" else "",
                iconRes = R.drawable.ic_capacity,
                accentColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            MetricIconCard(
                label = "Health",
                value = healthText,
                unit = "",
                iconRes = R.drawable.ic_battery_full,
                accentColor = healthColor,
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
            MetricIconCard(
                label = "Temperature",
                value = "${batteryState.temperatureCelsius}",
                unit = "°C",
                iconRes = R.drawable.ic_temp,
                accentColor = when {
                    batteryState.temperatureCelsius > 45 -> Color(0xFFEF5350)
                    batteryState.temperatureCelsius > 38 -> Color(0xFFF5AD56)
                    batteryState.temperatureCelsius < 5 -> Color(0xFF64B5F6)
                    else -> Color(0xFF4DD86C)
                },
                modifier = Modifier.weight(1f)
            )
            MetricIconCard(
                label = "Voltage",
                value = "${batteryState.voltage}",
                unit = "V",
                iconRes = R.drawable.ic_voltage,
                accentColor = Color(0xFFF5AD56),
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
            MetricIconCard(
                label = "Current",
                value = "${batteryState.currentUsageMa}",
                unit = "mA",
                iconRes = R.drawable.ic_usage,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MetricIconCard(
                label = "Technology",
                value = batteryState.batteryTechnology,
                unit = "",
                iconRes = R.drawable.ic_technology,
                accentColor = Color(0xFF9575CD),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MetricIconCard(
    label: String,
    value: String,
    unit: String,
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
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}
