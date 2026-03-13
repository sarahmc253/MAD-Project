package com.example.mad_project.ui.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mad_project.data.PantryModel
import com.example.mad_project.ui.components.EmptyView
import com.example.mad_project.ui.components.ErrorBanner
import com.example.mad_project.ui.components.LoadingView
import com.example.mad_project.ui.theme.ShelfScanGreen
import com.example.mad_project.ui.viewmodel.PantryViewModel

@Composable
fun PantryScreen(
    viewModel: PantryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> LoadingView()
        else -> {
            Column(Modifier.fillMaxSize()) {
                uiState.errorMessage?.let { ErrorBanner(message = it) }
                if (uiState.items.isEmpty()) {
                    EmptyPantryContent(onAddSample = viewModel::addItem)
                } else {
                    PantryContent(
                        items = uiState.items,
                        writeInProgress = uiState.writeInProgress,
                        onAddSample = viewModel::addItem,
                        onUpdate = viewModel::updateItem,
                        onDelete = viewModel::deleteItem
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPantryContent(onAddSample: (PantryModel) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmptyView(message = "No food items found")
        Button(
            onClick = {
                onAddSample(
                    PantryModel(
                        foodName = "Milk",
                        dateScanned = "2026-03-06",
                        expiryDate = "2026-03-10",
                        quantity = 1f
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ShelfScanGreen)
        ) {
            Text("Add sample item")
        }
    }
}

@Composable
private fun PantryContent(
    items: List<PantryModel>,
    writeInProgress: Boolean,
    onAddSample: (PantryModel) -> Unit,
    onUpdate: (PantryModel) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = {
                onAddSample(
                    PantryModel(
                        foodName = "Bread",
                        dateScanned = "2026-03-06",
                        expiryDate = "2026-03-08",
                        quantity = 1f
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = !writeInProgress,
            colors = ButtonDefaults.buttonColors(containerColor = ShelfScanGreen)
        ) {
            Text("Add Item")
        }
        PantryList(
            pantry = items,
            onUpdateClick = onUpdate,
            onDeleteClick = { item -> item.itemId?.let(onDelete) }
        )
    }
}
