package com.example.mad_project.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import com.example.mad_project.data.PantryModel
import com.example.mad_project.ui.inventory.InventoryListScreen
import com.example.mad_project.ui.item.AddItemScreen
import com.example.mad_project.ui.item.ItemDetailsScreen
import com.example.mad_project.ui.theme.MADProjectTheme
import com.example.mad_project.ui.viewmodel.InventoryViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    MADProjectTheme {
        NavHost(
            navController = navController,
            startDestination = Routes.INVENTORY,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(Routes.INVENTORY) {
                InventoryListScreen(
                    onItemClick = { itemId -> navController.navigate(Routes.itemDetail(itemId)) },
                    onScannerClick = { navController.navigate(Routes.ADD_ITEM) }
                )
            }

            composable(Routes.ADD_ITEM) {
                val inventoryViewModel: InventoryViewModel = viewModel()
                AddItemScreen(
                    onBackClick = { navController.popBackStack() },
                    onEnterManuallyClick = { navController.popBackStack() },
                    onAddScannedItem = { name, expiryDate, quantity, location ->
                        inventoryViewModel.upsertItem(
                            PantryModel(
                                foodName = name,
                                dateScanned = java.text.SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    java.util.Locale.getDefault()
                                ).format(java.util.Date()),
                                expiryDate = expiryDate,
                                quantity = quantity,
                                location = location
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
