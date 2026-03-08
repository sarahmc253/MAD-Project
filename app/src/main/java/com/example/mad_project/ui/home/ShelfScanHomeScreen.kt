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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mad_project.data.PantryModel
import com.example.mad_project.ui.components.PantryViewModel
import com.example.mad_project.ui.inventory.InventoryListScreen
import com.example.mad_project.ui.inventory.ShelfScanBottomBar
import com.example.mad_project.ui.item.AddItemScreen
import com.example.mad_project.ui.item.ItemDetailsScreen
import com.example.mad_project.ui.shopping.ShoppingListScreen
import com.example.mad_project.ui.theme.MADProjectTheme

/**
 * Main ShelfScan home: tabs (Inventory, List, Recipes, Settings) and overlays (Add Item, Item Details).
 * Used as the HOME destination after Login and Scanner in the merged app.
 */
@Composable
fun ShelfScanHomeScreen(
    pantryViewModel: PantryViewModel = viewModel()
) {
    var currentTab by rememberSaveable { mutableStateOf("Inventory") }
    var addItemOverlay by rememberSaveable { mutableStateOf(false) }
    var detailItemId by rememberSaveable { mutableStateOf<Long?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentTab) {
            "Inventory" -> {
                InventoryListScreen(
                    onAddClick = { addItemOverlay = true },
                    onItemClick = { id: Long -> detailItemId = id },
                    onShoppingClick = { currentTab = "List" },
                    onRecipesClick = { currentTab = "Recipes" },
                    onSettingsClick = { currentTab = "Settings" }
                )
            }
            "List" -> PlaceholderTabScreen(
                title = "List",
                currentTab = currentTab,
                onInventoryClick = { currentTab = "Inventory" },
                onListClick = { currentTab = "List" },
                onRecipesClick = { currentTab = "Recipes" },
                onSettingsClick = { currentTab = "Settings" },
                onAddClick = { addItemOverlay = true }
            )
            "Recipes" -> PlaceholderTabScreen(
                title = "Recipes",
                currentTab = currentTab,
                onInventoryClick = { currentTab = "Inventory" },
                onListClick = { currentTab = "List" },
                onRecipesClick = { currentTab = "Recipes" },
                onSettingsClick = { currentTab = "Settings" },
                onAddClick = { addItemOverlay = true }
            )
            "Settings" -> PlaceholderTabScreen(
                title = "Settings",
                currentTab = currentTab,
                onInventoryClick = { currentTab = "Inventory" },
                onListClick = { currentTab = "List" },
                onRecipesClick = { currentTab = "Recipes" },
                onSettingsClick = { currentTab = "Settings" },
                onAddClick = { addItemOverlay = true }
            )
        }

        if (addItemOverlay) {
            AddItemScreen(
                onBackClick = { addItemOverlay = false },
                onEnterManuallyClick = { addItemOverlay = false },
                onAddScannedItem = { name, expiryDate, quantity ->
                    pantryViewModel.addItem(
                        PantryModel(
                            foodName = name,
                            dateScanned = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                            expiryDate = expiryDate,
                            quantity = quantity
                        )
                    )
                    addItemOverlay = false
                }
            )
        }

        detailItemId?.let { id ->
            ItemDetailsScreen(
                itemId = id,
                onBackClick = { detailItemId = null },
                onEditClick = { detailItemId = null },
                onDeleteClick = { detailItemId = null },
                onMarkConsumed = { detailItemId = null },
                onRemoveFromPantry = { detailItemId = null }
            )
        }
    }
}

@Composable
private fun PlaceholderTabScreen(
    title: String,
    currentTab: String,
    onInventoryClick: () -> Unit,
    onListClick: () -> Unit,
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
                onListClick = onListClick,
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
            if (currentTab == "List") {
                ShoppingListScreen()
            } else {
                Text(
                    text = "$title – Coming soon",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShelfScanHomeScreenPreview() {
    MADProjectTheme(dynamicColour = false) { ShelfScanHomeScreen() }
}
