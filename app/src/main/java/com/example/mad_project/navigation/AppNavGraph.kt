package com.example.mad_project.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mad_project.ui.alerts.ExpiryAlertsScreen
import com.example.mad_project.ui.components.PantryViewModel
import com.example.mad_project.ui.home.ShelfScanHomeScreen
import com.example.mad_project.ui.login.LoginScreen
import com.example.mad_project.ui.scanner.BarcodeScannerScreen
import com.example.mad_project.ui.theme.MADProjectTheme

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    // Single ViewModel instance scoped to the Activity, shared across destinations.
    val pantryViewModel: PantryViewModel = viewModel()

    MADProjectTheme(dynamicColour = false) {
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.SCANNER) {
                BarcodeScannerScreen(
                    onDone = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SCANNER) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                ShelfScanHomeScreen(
                    pantryViewModel = pantryViewModel,
                    onAlertsClick = { navController.navigate(Routes.EXPIRY_ALERTS) }
                )
            }

            composable(Routes.EXPIRY_ALERTS) {
                val uiState by pantryViewModel.uiState.collectAsState()
                ExpiryAlertsScreen(
                    items = uiState.alertItems,
                    onBackClick = { navController.popBackStack() },
                    onDeleteItem = { itemId -> pantryViewModel.deleteItem(itemId) }
                )
            }
        }
    }
}
