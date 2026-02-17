package com.rejowan.chargify.presentation.screens.tools

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.rejowan.chargify.R
import com.rejowan.chargify.presentation.viewmodel.ChargingAlarmsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargingAlarmsScreen(
    onBackClick: () -> Unit,
    viewModel: ChargingAlarmsViewModel = koinViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    // Track pending alarm action for after permission is granted
    var pendingAlarmAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingAlarmAction?.invoke()
        }
        pendingAlarmAction = null
    }

    // Helper function to check/request permission before enabling alarm
    fun enableAlarmWithPermissionCheck(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    action()
                }
                else -> {
                    pendingAlarmAction = action
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            action()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Charging Alarms") },
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

            // Info card
            InfoCard()

            Spacer(modifier = Modifier.height(16.dp))

            // Master toggle card
            MasterToggleCard(
                enabled = settings.alarmsEnabled,
                onEnabledChange = { enabled ->
                    if (enabled) {
                        enableAlarmWithPermissionCheck { viewModel.setAlarmsEnabled(true) }
                    } else {
                        viewModel.setAlarmsEnabled(false)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Alarm settings header
            Text(
                text = "Alarm Settings",
                style = MaterialTheme.typography.titleMedium,
                color = if (settings.alarmsEnabled) MaterialTheme.colorScheme.onSurface
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Full Charge Alarm
            AlarmSettingCard(
                title = "Full Charge Alert",
                description = "Notify when battery is fully charged",
                iconRes = R.drawable.ic_charge_abv,
                accentColor = Color(0xFF4DD86C),
                enabled = settings.fullChargeAlarmEnabled,
                onEnabledChange = { enabled ->
                    if (enabled) {
                        enableAlarmWithPermissionCheck { viewModel.setFullChargeAlarm(true) }
                    } else {
                        viewModel.setFullChargeAlarm(false)
                    }
                },
                threshold = settings.fullChargeThreshold,
                onThresholdChange = { viewModel.setFullChargeThreshold(it) },
                thresholdRange = 80f..100f,
                thresholdLabel = "Alert at ${settings.fullChargeThreshold}%",
                isDisabled = !settings.alarmsEnabled
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Low Battery Alarm
            AlarmSettingCard(
                title = "Low Battery Alert",
                description = "Notify when battery is running low",
                iconRes = R.drawable.ic_warning_abv,
                accentColor = Color(0xFFEF5350),
                enabled = settings.lowBatteryAlarmEnabled,
                onEnabledChange = { enabled ->
                    if (enabled) {
                        enableAlarmWithPermissionCheck { viewModel.setLowBatteryAlarm(true) }
                    } else {
                        viewModel.setLowBatteryAlarm(false)
                    }
                },
                threshold = settings.lowBatteryThreshold,
                onThresholdChange = { viewModel.setLowBatteryThreshold(it) },
                thresholdRange = 5f..30f,
                thresholdLabel = "Alert at ${settings.lowBatteryThreshold}%",
                isDisabled = !settings.alarmsEnabled
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Alarm (80% for battery health)
            AlarmSettingCard(
                title = "Battery Health Alert",
                description = "Alert at custom level to preserve battery health",
                iconRes = R.drawable.ic_battery_level,
                accentColor = Color(0xFFF5AD56),
                enabled = settings.customAlarmEnabled,
                onEnabledChange = { enabled ->
                    if (enabled) {
                        enableAlarmWithPermissionCheck { viewModel.setCustomAlarm(true) }
                    } else {
                        viewModel.setCustomAlarm(false)
                    }
                },
                threshold = settings.customAlarmThreshold,
                onThresholdChange = { viewModel.setCustomAlarmThreshold(it) },
                thresholdRange = 50f..95f,
                thresholdLabel = "Alert at ${settings.customAlarmThreshold}%",
                isDisabled = !settings.alarmsEnabled
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Notification options header
            Text(
                text = "Notification Options",
                style = MaterialTheme.typography.titleMedium,
                color = if (settings.alarmsEnabled) MaterialTheme.colorScheme.onSurface
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sound toggle
            ToggleOptionCard(
                title = "Sound",
                description = "Play notification sound",
                iconRes = R.drawable.ic_alarm,
                enabled = settings.soundEnabled,
                onEnabledChange = { viewModel.setSoundEnabled(it) },
                isDisabled = !settings.alarmsEnabled
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Vibration toggle
            ToggleOptionCard(
                title = "Vibration",
                description = "Vibrate on notification",
                iconRes = R.drawable.ic_power,
                enabled = settings.vibrationEnabled,
                onEnabledChange = { viewModel.setVibrationEnabled(it) },
                isDisabled = !settings.alarmsEnabled
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoCard(modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_tips),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Battery Health Tip",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Charging to 80% instead of 100% can significantly extend your battery's lifespan. Consider enabling the Battery Health Alert at 80%.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MasterToggleCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        if (enabled) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF4DD86C), Color(0xFF00BCD4))
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.outline,
                                    MaterialTheme.colorScheme.outline
                                )
                            )
                        }
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
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (enabled) Color(0xFF4DD86C).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_alarm),
                        contentDescription = null,
                        tint = if (enabled) Color(0xFF4DD86C) else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Battery Alarms",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (enabled) "Alarms are active" else "All alarms disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) Color(0xFF4DD86C) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4DD86C),
                        checkedTrackColor = Color(0xFF4DD86C).copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

@Composable
private fun AlarmSettingCard(
    title: String,
    description: String,
    iconRes: Int,
    accentColor: Color,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    threshold: Int,
    onThresholdChange: (Int) -> Unit,
    thresholdRange: ClosedFloatingPointRange<Float>,
    thresholdLabel: String,
    isDisabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val displayColor = if (isDisabled) accentColor.copy(alpha = 0.4f) else accentColor

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(displayColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = displayColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                               else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDisabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = enabled && !isDisabled,
                    onCheckedChange = { if (!isDisabled) onEnabledChange(it) },
                    enabled = !isDisabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = displayColor,
                        checkedTrackColor = displayColor.copy(alpha = 0.5f)
                    )
                )
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = thresholdLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = accentColor
                    )
                    Text(
                        text = "${thresholdRange.start.toInt()}% - ${thresholdRange.endInclusive.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = threshold.toFloat(),
                    onValueChange = { onThresholdChange(it.toInt()) },
                    valueRange = thresholdRange,
                    steps = ((thresholdRange.endInclusive - thresholdRange.start) / 5).toInt() - 1,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor
                    )
                )
            }
        }
    }
}

@Composable
private fun ToggleOptionCard(
    title: String,
    description: String,
    iconRes: Int,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    isDisabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val tintColor = if (isDisabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.primary

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tintColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                           else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDisabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = enabled && !isDisabled,
                onCheckedChange = { if (!isDisabled) onEnabledChange(it) },
                enabled = !isDisabled
            )
        }
    }
}
