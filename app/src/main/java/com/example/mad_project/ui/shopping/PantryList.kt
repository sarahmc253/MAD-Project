package com.example.mad_project.ui.shopping

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.example.mad_project.data.PantryModel

@Composable
fun PantryList(
    pantry: List<PantryModel>,
    onUpdateClick: (PantryModel) -> Unit,
    onDeleteClick: (PantryModel) -> Unit
) {
    LazyColumn {
        items(
            items = pantry,
            key = { it.itemId ?: it.foodName ?: it.hashCode().toString() }
        ) { item ->
            PantryItem(
                item = item,
                onUpdateClick = onUpdateClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}