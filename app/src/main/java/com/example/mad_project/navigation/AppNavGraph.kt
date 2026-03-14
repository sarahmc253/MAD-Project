package com.example.mad_project.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import com.example.mad_project.data.PantryModel
import com.example.mad_project.ui.inventory.InventoryListScreen
import com.example.mad_project.ui.inventory.ShelfScanBottomBar
import com.example.mad_project.ui.item.AddItemScreen
import com.example.mad_project.ui.item.ItemDetailsScreen
import com.example.mad_project.ui.login.LoginScreen
import com.example.mad_project.ui.theme.MADProjectTheme
import com.example.mad_project.ui.viewmodel.PantryViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    MADProjectTheme(dynamicColour = false) {
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.INVENTORY) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.INVENTORY) {
                InventoryListScreen(
                    onAddClick = { navController.navigate(Routes.ADD_ITEM) },
                    onItemClick = { itemId -> navController.navigate(Routes.itemDetail(itemId)) },
                    onScannerClick = { navController.navigate(Routes.ADD_ITEM) }
                )
            }

            composable(Routes.ADD_ITEM) {
                val pantryViewModel: PantryViewModel = viewModel()
                AddItemScreen(
                    onBackClick = { navController.popBackStack() },
                    onEnterManuallyClick = { navController.popBackStack() },
                    onAddScannedItem = { name, expiryDate, quantity ->
                        pantryViewModel.addItem(
                            PantryModel(
                                foodName = name,
                                dateScanned = java.text.SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    java.util.Locale.getDefault()
                                ).format(java.util.Date()),
                                expiryDate = expiryDate,
                                quantity = quantity
                            )
                        )
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Routes.ITEM_DETAIL,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                ItemDetailsScreen(
                    itemId = itemId,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.popBackStack() },
                    onDeleteClick = { navController.popBackStack() },
                    onMarkConsumed = { navController.popBackStack() },
                    onRemoveFromPantry = { navController.popBackStack() }
                )
            }
        }
    }
}