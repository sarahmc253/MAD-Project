package com.example.mad_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mad_project.ui.inventory.InventoryListScreen
import com.example.mad_project.ui.inventory.ShelfScanBottomBar
import com.example.mad_project.ui.item.AddItemScreen
import com.example.mad_project.ui.item.ItemDetailsScreen
import com.example.mad_project.ui.theme.MADProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MADProjectTheme(dynamicColour = false) {
                ShelfScanApp()
            }
        }
    }
}

@Composable
private fun ShelfScanApp() {
    var currentTab by remember { mutableStateOf("Inventory") }
    var addItemOverlay by remember { mutableStateOf(false) }
    var detailItemId by remember { mutableStateOf<Long?>(null) }

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
            "List" -> PlaceholderScreen(
                title = "List",
                currentTab = currentTab,
                onInventoryClick = { currentTab = "Inventory" },
                onListClick = { currentTab = "List" },
                onRecipesClick = { currentTab = "Recipes" },
                onSettingsClick = { currentTab = "Settings" }
            )
            "Recipes" -> PlaceholderScreen(
                title = "Recipes",
                currentTab = currentTab,
                onInventoryClick = { currentTab = "Inventory" },
                onListClick = { currentTab = "List" },
                onRecipesClick = { currentTab = "Recipes" },
                onSettingsClick = { currentTab = "Settings" }
            )
            "Settings" -> PlaceholderScreen(
                title = "Settings",
                currentTab = currentTab,
                onInventoryClick = { currentTab = "Inventory" },
                onListClick = { currentTab = "List" },
                onRecipesClick = { currentTab = "Recipes" },
                onSettingsClick = { currentTab = "Settings" }
            )
        }

        // Overlay: Add Item (scanner)
        if (addItemOverlay) {
            AddItemScreen(
                onBackClick = { addItemOverlay = false },
                onEnterManuallyClick = { addItemOverlay = false },
                onAddScannedItem = { addItemOverlay = false }
            )
        }

        // Overlay: Item Details
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
private fun PlaceholderScreen(
    title: String,
    currentTab: String,
    onInventoryClick: () -> Unit,
    onListClick: () -> Unit,
    onRecipesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = { },
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
            contentAlignment = Alignment.Center
        ) {
            Text("$title – Coming soon", modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShelfScanAppPreview() {
    MADProjectTheme(dynamicColour = false) { ShelfScanApp() }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderScreenPreview() {
    MADProjectTheme(dynamicColour = false) {
        PlaceholderScreen(
            title = "List",
            currentTab = "List",
            onInventoryClick = {},
            onListClick = {},
            onRecipesClick = {},
            onSettingsClick = {}
        )
    }
}
