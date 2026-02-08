package com.example.nugget.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.nugget.ui.login.LoginScreen
import com.example.nugget.ui.scanner.BarcodeScannerScreen
import com.example.nugget.MainActivity

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

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
                    navController.navigate(Routes.HOME)
                }
            )
        }

        composable(Routes.HOME) {
            MainActivity()
        }
    }
}
