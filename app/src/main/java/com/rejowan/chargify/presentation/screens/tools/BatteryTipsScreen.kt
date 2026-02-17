package com.rejowan.chargify.presentation.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rejowan.chargify.R
import com.rejowan.chargify.data.model.BatteryState
import com.rejowan.chargify.presentation.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel

data class BatteryTip(
    val title: String,
    val description: String,
    val iconRes: Int,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryTipsScreen(
    onBackClick: () -> Unit,
    viewModel: MainViewModel = koinViewModel()
) {
    val batteryState by viewModel.batteryState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battery Tips") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Live Status Card
            LiveStatusCard(batteryState = batteryState)

            Spacer(modifier = Modifier.height(20.dp))

            // Tips Section Header
            Text(
                text = "Optimization Tips",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tips List
            val tips = listOf(
                BatteryTip(
                    title = "Charge between 20-80%",
                    description = "Keeping your battery between 20% and 80% helps extend its lifespan. Avoid letting it drain completely or charging to 100% regularly.",
                    iconRes = R.drawable.ic_charge_abv,
                    accentColor = Color(0xFF4DD86C)
                ),
                BatteryTip(
                    title = "Avoid extreme temperatures",
                    description = "Batteries perform best between 20°C and 35°C. Avoid exposing your device to extreme heat or cold, especially while charging.",
                    iconRes = R.drawable.ic_temp,
                    accentColor = Color(0xFFF5AD56)
                ),
                BatteryTip(
                    title = "Use original charger",
                    description = "Using the original or certified charger ensures optimal charging speed and protects your battery from damage.",
                    iconRes = R.drawable.ic_charging,
                    accentColor = Color(0xFF64B5F6)
                ),
                BatteryTip(
                    title = "Reduce screen brightness",
                    description = "The display is one of the biggest battery drains. Use auto-brightness or keep it at a comfortable lower level.",
                    iconRes = R.drawable.ic_tips,
                    accentColor = Color(0xFF9575CD)
                ),
                BatteryTip(
                    title = "Disable unused features",
                    description = "Turn off Wi-Fi, Bluetooth, GPS, and NFC when not in use. These features constantly search for connections and drain battery.",
                    iconRes = R.drawable.ic_power,
                    accentColor = Color(0xFFEF5350)
                ),
                BatteryTip(
                    title = "Update your apps",
                    description = "App updates often include battery optimizations. Keep your apps updated to benefit from the latest improvements.",
                    iconRes = R.drawable.ic_apps,
                    accentColor = MaterialTheme.colorScheme.primary
                )
            )

            tips.forEach { tip ->
                TipCard(tip = tip)
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LiveStatusCard(
    batteryState: BatteryState,
    modifier: Modifier = Modifier
) {
    val tempStatus = when {
        batteryState.temperatureCelsius > 40 -> StatusLevel.WARNING
        batteryState.temperatureCelsius > 35 -> StatusLevel.CAUTION
        batteryState.temperatureCelsius < 10 -> StatusLevel.CAUTION
        else -> StatusLevel.GOOD
    }

    val chargeStatus = when {
        batteryState.chargeLevel > 80 && batteryState.isCharging -> StatusLevel.CAUTION
        batteryState.chargeLevel < 20 -> StatusLevel.WARNING
        else -> StatusLevel.GOOD
    }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Current Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusItem(
                        label = "Temperature",
                        value = "%.1f°C".format(batteryState.temperatureCelsius),
                        status = tempStatus,
                        modifier = Modifier.weight(1f)
                    )
                    StatusItem(
                        label = "Battery Level",
                        value = "${batteryState.chargeLevel}%",
                        status = chargeStatus,
                        modifier = Modifier.weight(1f)
                    )
                    StatusItem(
                        label = "Voltage",
                        value = "%.2fV".format(batteryState.voltage),
                        status = StatusLevel.GOOD,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (tempStatus != StatusLevel.GOOD || chargeStatus != StatusLevel.GOOD) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val warningText = when {
                        tempStatus == StatusLevel.WARNING -> "⚠️ Battery temperature is high. Let it cool down."
                        tempStatus == StatusLevel.CAUTION && batteryState.temperatureCelsius > 35 -> "Temperature is slightly elevated."
                        tempStatus == StatusLevel.CAUTION -> "Battery is cold. Warm up before heavy use."
                        chargeStatus == StatusLevel.WARNING -> "⚠️ Battery is low. Consider charging soon."
                        chargeStatus == StatusLevel.CAUTION -> "Battery above 80% while charging. Consider unplugging."
                        else -> ""
                    }

                    if (warningText.isNotEmpty()) {
                        Text(
                            text = warningText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (tempStatus == StatusLevel.WARNING || chargeStatus == StatusLevel.WARNING)
                                Color(0xFFEF5350) else Color(0xFFF5AD56)
                        )
                    }
                }
            }
        }
    }
}

private enum class StatusLevel {
    GOOD, CAUTION, WARNING
}

@Composable
private fun StatusItem(
    label: String,
    value: String,
    status: StatusLevel,
    modifier: Modifier = Modifier
) {
    val statusColor = when (status) {
        StatusLevel.GOOD -> Color(0xFF4DD86C)
        StatusLevel.CAUTION -> Color(0xFFF5AD56)
        StatusLevel.WARNING -> Color(0xFFEF5350)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TipCard(
    tip: BatteryTip,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tip.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = tip.iconRes),
                    contentDescription = null,
                    tint = tip.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
