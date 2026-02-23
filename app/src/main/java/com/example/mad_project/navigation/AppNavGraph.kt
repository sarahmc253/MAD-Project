package com.example.mad_project.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mad_project.ui.home.ShelfScanHomeScreen
import com.example.mad_project.ui.login.LoginScreen
import com.example.mad_project.ui.scanner.BarcodeScannerScreen
import com.example.mad_project.ui.theme.MADProjectTheme

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    MADProjectTheme(dynamicColour = false) {
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.SCANNER) {
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
                ShelfScanHomeScreen()
            }
        }
    }
}
