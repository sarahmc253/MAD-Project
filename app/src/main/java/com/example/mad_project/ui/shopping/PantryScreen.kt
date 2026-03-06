package com.example.mad_project.ui.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mad_project.data.PantryModel


//temporary screen for testing
@Composable
fun PantryScreen(
    viewModel: PantryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.items.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        viewModel.addItem(
                            PantryModel(
                                foodName = "Milk",
                                dateScanned = "2026-03-06",
                                expiryDate = "2026-03-10",
                                quantity = 1f
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Item")
                }

                Text("No food items found")
            }
        }

        else -> {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Button(
                    onClick = {
                        viewModel.addItem(
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
                        .padding(16.dp)
                ) {
                    Text("Add Item")
                }

                PantryList(
                    pantry = uiState.items,
                    onUpdateClick = { item ->
                        viewModel.updateItem(
                            item.copy(quantity = (item.quantity ?: 0f) + 1f)
                        )
                    },
                    onDeleteClick = { item ->
                        item.itemId?.let(viewModel::deleteItem)
                    }
                )
            }
        }
    }
}