package com.carradio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carradio.ui.favorites.CountryPickerScreen
import com.carradio.ui.favorites.FavoritesPickerScreen
import com.carradio.ui.favorites.StationListScreen
import com.carradio.ui.home.HomeScreen
import com.carradio.ui.settings.SettingsScreen
import com.carradio.ui.timer.SleepTimerScreen
import com.carradio.ui.timer.SleepTimerViewModel

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val FAVORITES_PICKER = "favorites_picker"
    const val COUNTRY_PICKER = "country_picker/{slotPosition}"
    const val STATION_LIST = "station_list/{countryIso}/{countryName}/{slotPosition}"
    const val SLEEP_TIMER = "sleep_timer"

    fun countryPicker(slotPosition: Int) = "country_picker/$slotPosition"
    fun stationList(countryIso: String, countryName: String, slotPosition: Int) =
        "station_list/$countryIso/${countryName.encodeForNav()}/$slotPosition"

    private fun String.encodeForNav() = replace("/", "%2F")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    // Activity-scoped: survives navigation between screens
    val timerViewModel: SleepTimerViewModel = hiltViewModel()
    val isTimerRunning by timerViewModel.isRunning.collectAsState()
    val remainingSeconds by timerViewModel.remainingSeconds.collectAsState()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
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
                onBack = { navController.popBackStack() },
                onManageFavorites = { navController.navigate(Routes.FAVORITES_PICKER) }
            )
        }
        composable(Routes.FAVORITES_PICKER) {
            FavoritesPickerScreen(
                onBack = { navController.popBackStack() },
                onAddFavorite = { slotPosition ->
                    navController.navigate(Routes.countryPicker(slotPosition))
                }
            )
        }
        composable(
            Routes.COUNTRY_PICKER,
            arguments = listOf(navArgument("slotPosition") { type = NavType.IntType })
        ) { backStack ->
            val slotPosition = backStack.arguments?.getInt("slotPosition") ?: 0
            CountryPickerScreen(
                slotPosition = slotPosition,
                onBack = { navController.popBackStack() },
                onCountrySelected = { iso, name ->
                    navController.navigate(Routes.stationList(iso, name, slotPosition))
                },
                onStationDirectlySelected = {
                    navController.popBackStack(Routes.FAVORITES_PICKER, inclusive = false)
                }
            )
        }
        composable(
            Routes.STATION_LIST,
            arguments = listOf(
                navArgument("countryIso") { type = NavType.StringType },
                navArgument("countryName") { type = NavType.StringType },
                navArgument("slotPosition") { type = NavType.IntType }
            )
        ) { backStack ->
            val iso = backStack.arguments?.getString("countryIso") ?: ""
            val name = backStack.arguments?.getString("countryName") ?: ""
            val slotPosition = backStack.arguments?.getInt("slotPosition") ?: 0
            StationListScreen(
                countryIso = iso,
                countryName = name,
                slotPosition = slotPosition,
                onBack = { navController.popBackStack() },
                onStationSelected = {
                    navController.popBackStack(Routes.FAVORITES_PICKER, inclusive = false)
                }
            )
        }
    }
}
