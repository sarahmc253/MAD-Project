package com.example.mad_project.ui.shopping

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mad_project.data.FoodItem
import com.example.mad_project.data.getPantry

@Composable
fun PantryScreen(
    onItemClick: (FoodItem) -> Unit = {}
) {

    val pantry by getPantry().collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    if (pantry.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No food items found")
        }
    } else {
        PantryList(
            pantry = pantry,
            onItemClick = onItemClick
        )
    }
}