package com.example.mad_project.ui.shopping

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.mad_project.ui.inventory.ShelfScanBottomBar
import com.example.mad_project.ui.theme.MADProjectTheme
import com.example.mad_project.ui.theme.ShelfScanGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onInventoryClick: () -> Unit = {},
    onScannerClick: () -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = ShelfScanGreen,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        },
        bottomBar = {
            ShelfScanBottomBar(
                currentTab = "List",
                onInventoryClick = onInventoryClick,
                onScannerClick = onScannerClick
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PantryScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShoppingListScreenPreview() {
    MADProjectTheme(dynamicColour = false) {
        ShoppingListScreen()
    }
}
