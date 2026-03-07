package com.example.mad_project.ui.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mad_project.data.PantryModel

@Composable
fun PantryItem(
    item: PantryModel,
    onUpdateClick: (PantryModel) -> Unit,
    onDeleteClick: (PantryModel) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.foodName ?: "No name",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(text = "Scanned: ${item.dateScanned ?: "Unknown"}", fontSize = 16.sp)
            Text(text = "Expires: ${item.expiryDate ?: "Unknown"}", fontSize = 16.sp)
            Text(text = "Quantity: ${item.quantity ?: 0f}", fontSize = 16.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onUpdateClick(item) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Update")
                }

                OutlinedButton(
                    onClick = { onDeleteClick(item) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}