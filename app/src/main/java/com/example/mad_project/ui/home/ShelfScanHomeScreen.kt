package com.example.mad_project.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mad_project.data.PantryModel
import com.example.mad_project.ui.viewmodel.HomeViewModel
import com.example.mad_project.ui.viewmodel.InventoryViewModel
import com.example.mad_project.ui.inventory.InventoryListScreen
import com.example.mad_project.ui.inventory.ShelfScanBottomBar
import com.example.mad_project.ui.item.AddItemScreen
import com.example.mad_project.ui.item.ItemDetailsScreen
import com.example.mad_project.ui.theme.MADProjectTheme

/**
 * Main ShelfScan home: tabs (Inventory, List, Recipes, Settings) and overlays (Add Item, Item Details).
 * Used as the HOME destination after Login and Scanner in the merged app.
 */
@Composable
fun ShelfScanHomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when (homeState.currentTab) {
            "Inventory" -> {
                InventoryListScreen(
                    onAddClick = homeViewModel::showAddItemOverlay,
                    onItemClick = homeViewModel::openItemDetail,
                    onRecipesClick = { homeViewModel.setCurrentTab("Recipes") },
                    onSettingsClick = { homeViewModel.setCurrentTab("Settings") }
                )
            }
            "List" -> PlaceholderTabScreen(
                title = "List",
                currentTab = homeState.currentTab,
                onInventoryClick = { homeViewModel.setCurrentTab("Inventory") },
                onRecipesClick = { homeViewModel.setCurrentTab("Recipes") },
                onSettingsClick = { homeViewModel.setCurrentTab("Settings") },
                onAddClick = homeViewModel::showAddItemOverlay
            )
            "Recipes" -> PlaceholderTabScreen(
                title = "Recipes",
                currentTab = homeState.currentTab,
                onInventoryClick = { homeViewModel.setCurrentTab("Inventory") },
                onRecipesClick = { homeViewModel.setCurrentTab("Recipes") },
                onSettingsClick = { homeViewModel.setCurrentTab("Settings") },
                onAddClick = homeViewModel::showAddItemOverlay
            )
            "Settings" -> PlaceholderTabScreen(
                title = "Settings",
                currentTab = homeState.currentTab,
                onInventoryClick = { homeViewModel.setCurrentTab("Inventory") },
                onRecipesClick = { homeViewModel.setCurrentTab("Recipes") },
                onSettingsClick = { homeViewModel.setCurrentTab("Settings") },
                onAddClick = homeViewModel::showAddItemOverlay
            )
        }

        if (homeState.addItemOverlayVisible) {
            AddItemScreen(
                onBackClick = homeViewModel::hideAddItemOverlay,
                onEnterManuallyClick = homeViewModel::hideAddItemOverlay,
                onAddScannedItem = { name, expiryDate, quantity ->
                    inventoryViewModel.upsertItem(
                        PantryModel(
                            foodName = name,
                            dateScanned = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                            expiryDate = expiryDate,
                            quantity = quantity
                        )
                    )
                    homeViewModel.hideAddItemOverlay()
                }
            )
        }

        homeState.detailItemId?.let { id ->
            ItemDetailsScreen(
                itemId = id,
                onBackClick = homeViewModel::closeItemDetail,
                onEditClick = homeViewModel::closeItemDetail,
                onDeleteClick = homeViewModel::closeItemDetail,
                onMarkConsumed = homeViewModel::closeItemDetail,
                onRemoveFromPantry = homeViewModel::closeItemDetail
            )
        }
    }
}

@Composable
private fun PlaceholderTabScreen(
    title: String,
    currentTab: String,
    onInventoryClick: () -> Unit,
    onRecipesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit = {}
) {
    Scaffold(
        topBar = { },
        floatingActionButton = {
            if (currentTab == "List") {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = com.example.mad_project.ui.theme.ShelfScanGreen,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        },
        bottomBar = {
            ShelfScanBottomBar(
                currentTab = currentTab,
                onInventoryClick = onInventoryClick,
                onRecipesClick = onRecipesClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = if (currentTab == "List") Alignment.TopStart else Alignment.Center
        ) {
            Text(
                text = "$title – Coming soon",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShelfScanHomeScreenPreview() {
    MADProjectTheme(dynamicColour = false) { ShelfScanHomeScreen() }
}
