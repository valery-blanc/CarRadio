package com.carradio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.carradio.ui.home.HomeScreen
import com.carradio.ui.home.HomeViewModel
import com.carradio.ui.settings.SettingsScreen
import com.carradio.ui.timer.SleepTimerScreen
import com.carradio.ui.timer.SleepTimerViewModel

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val SLEEP_TIMER = "sleep_timer"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    // Activity-scoped: survives navigation between screens
    val timerViewModel: SleepTimerViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()

    val isTimerRunning by timerViewModel.isRunning.collectAsState()
    val remainingSeconds by timerViewModel.remainingSeconds.collectAsState()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToTimer = { navController.navigate(Routes.SLEEP_TIMER) },
                isTimerRunning = isTimerRunning,
                remainingSeconds = remainingSeconds
            )
        }
        composable(Routes.SLEEP_TIMER) {
            SleepTimerScreen(
                viewModel = timerViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
