package com.rejowan.chargify.presentation.screens.main

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rejowan.chargify.R
import com.rejowan.chargify.presentation.screens.main.sections.MonitorSection
import com.rejowan.chargify.presentation.screens.main.sections.OverviewSection
import com.rejowan.chargify.presentation.screens.main.sections.StatsSection
import com.rejowan.chargify.presentation.screens.main.sections.ToolsSection
import com.rejowan.chargify.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private data class NavItem(
    val label: String,
    val selectedIcon: Int,
    val unselectedIcon: Int
)

// LazyColumn item indices for each section
private const val OVERVIEW_INDEX = 0
private const val MONITOR_INDEX = 1
private const val STATS_INDEX = 2
private const val TOOLS_INDEX = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToTool: (String) -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    var isProgrammaticScroll by remember { mutableStateOf(false) }
    var showExitSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Handle back press to show exit confirmation
    BackHandler {
        showExitSheet = true
    }

    val batteryState by viewModel.batteryState.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val healthLevel by viewModel.healthLevel.collectAsState()
    val formattedEta by viewModel.formattedEta.collectAsState()
    val formattedTimeRemaining by viewModel.formattedTimeRemaining.collectAsState()
    val currentHistory by viewModel.currentUsageHistory.collectAsState()
    val tempHistory by viewModel.temperatureHistory.collectAsState()
    val voltageHistory by viewModel.voltageHistory.collectAsState()
    val levelHistory by viewModel.batteryLevelHistory.collectAsState()
    val powerHistory by viewModel.powerHistory.collectAsState()
    val sessionStats by viewModel.sessionStats.collectAsState()
    val chargeSpeedText by viewModel.chargeSpeedText.collectAsState()
    val healthText by viewModel.healthText.collectAsState()

    val navItems = listOf(
        NavItem("Overview", R.drawable.ic_nav_home_filled, R.drawable.ic_nav_home_normal),
        NavItem("Monitor", R.drawable.ic_nav_monitor_filled, R.drawable.ic_nav_monitor_normal),
        NavItem("Stats", R.drawable.ic_nav_stats_filled, R.drawable.ic_nav_stats_normal),
        NavItem("Tools", R.drawable.ic_nav_tools_filled, R.drawable.ic_nav_tools_normal),
    )

    // Sync scroll position -> bottom nav
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (!isProgrammaticScroll) {
                    selectedNavIndex = when {
                        index >= TOOLS_INDEX -> 3
                        index >= STATS_INDEX -> 2
                        index >= MONITOR_INDEX -> 1
                        else -> 0
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chargify") },
                actions = {
                    IconButton(onClick = { onNavigateToTool("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = if (selectedNavIndex == index) item.selectedIcon else item.unselectedIcon
                                ),
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        selected = selectedNavIndex == index,
                        onClick = {
                            selectedNavIndex = index
                            isProgrammaticScroll = true
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                                isProgrammaticScroll = false
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(innerPadding)
        ) {
            item {
                OverviewSection(
                    batteryState = batteryState,
                    statusText = statusText,
                    formattedEta = formattedEta,
                    formattedTimeRemaining = formattedTimeRemaining,
                    chargeSpeedText = chargeSpeedText,
                    healthText = healthText
                )
            }
            item {
                MonitorSection(
                    batteryState = batteryState,
                    currentHistory = currentHistory,
                    tempHistory = tempHistory,
                    voltageHistory = voltageHistory,
                    levelHistory = levelHistory,
                    powerHistory = powerHistory,
                    sessionStats = sessionStats,
                    chargeSpeedText = chargeSpeedText
                )
            }
            item {
                StatsSection(
                    batteryState = batteryState,
                    tempHistory = tempHistory,
                    voltageHistory = voltageHistory,
                    currentHistory = currentHistory,
                    powerHistory = powerHistory,
                    sessionStats = sessionStats
                )
            }
            item {
                ToolsSection(onToolClick = onNavigateToTool)
            }
        }
    }

    // Exit Confirmation Bottom Sheet
    if (showExitSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExitSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            ExitConfirmationContent(
                onConfirmExit = {
                    showExitSheet = false
                    (context as? Activity)?.finish()
                },
                onDismiss = { showExitSheet = false }
            )
        }
    }
}

@Composable
private fun ExitConfirmationContent(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_battery_charging),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Exit Chargify?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Are you sure you want to exit?\nBattery monitoring will stop.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onConfirmExit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Exit")
            }
        }
    }
}
