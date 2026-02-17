package com.rejowan.chargify.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rejowan.chargify.presentation.screens.main.MainScreen
import com.rejowan.chargify.presentation.screens.onboarding.OnboardingScreen
import com.rejowan.chargify.presentation.screens.tools.AppUsageScreen
import com.rejowan.chargify.presentation.screens.tools.BatteryTipsScreen
import com.rejowan.chargify.presentation.screens.tools.ChargingAlarmsScreen
import com.rejowan.chargify.presentation.screens.tools.ChargingHistoryScreen
import com.rejowan.chargify.presentation.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel = koinViewModel()) {
    val navController = rememberNavController()
    val startDestination = if (viewModel.appPreferences.isFirstLaunch()) {
        Screen.Onboarding.route
    } else {
        Screen.Main.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    viewModel.appPreferences.setFirstLaunchComplete()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToTool = { route ->
                    navController.navigate(route)
                }
            )
        }

        // Tool screens
        composable(Screen.ChargingAlarms.route) {
            ChargingAlarmsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.ChargingHistory.route) {
            ChargingHistoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.BatteryTips.route) {
            BatteryTipsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.AppUsage.route) {
            AppUsageScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
