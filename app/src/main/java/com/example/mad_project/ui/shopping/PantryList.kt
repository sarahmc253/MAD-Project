package com.example.mad_project.ui.shopping

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.example.mad_project.data.FoodItem

@Composable
fun PantryList(
    pantry: List<FoodItem>,
    onItemClick: (FoodItem) -> Unit
) {
    LazyColumn {
        items(pantry) { food ->
            PantryItem(
                food = food,
                onClick = { onItemClick(food) }
            )
        }
    }
}