package com.example.mad_project.ui.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mad_project.data.PantryModel
import com.example.mad_project.ui.components.AppTopBar
import com.example.mad_project.ui.components.EmptyView
import com.example.mad_project.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryAlertsScreen(
    items: List<PantryModel>,
    onBackClick: () -> Unit,
    onDeleteItem: (String) -> Unit = {},
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Expiry Alerts",
                showBack = true,
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                EmptyView("No expiry alerts")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(items, key = { it.itemId ?: it.foodName.orEmpty() }) { item ->
                    AlertItemRow(item, onDeleteItem = onDeleteItem)
                }
            }
        }
    }
}

@Composable
private fun AlertItemRow(item: PantryModel, onDeleteItem: (String) -> Unit) {
    val now = System.currentTimeMillis()
    val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val expiryTime = item.expiryDate?.let { runCatching { fmt.parse(it)?.time }.getOrNull() }
    val isExpired = expiryTime != null && expiryTime < now
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Remove item") },
            text = { Text("Are you sure you want to remove \"${item.foodName ?: "this item"}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        item.itemId?.let { onDeleteItem(it) }
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpiredRed)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.foodName ?: "Unknown item",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
                if (item.expiryDate != null) {
                    Text(
                        "Expires: ${item.expiryDate}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isExpired) ExpiredRedLight else ExpiringSoonOrangeLight
            ) {
                Text(
                    if (isExpired) "EXPIRED" else "EXPIRING SOON",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpired) ExpiredRed else ExpiringSoonOrange
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = { showConfirmDialog = true }) {
                Text("Remove", color = ExpiredRed, fontSize = 13.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpiryAlertsScreenPreview() {
    MADProjectTheme(dynamicColour = false) {
        ExpiryAlertsScreen(
            items = listOf(
                PantryModel("1", "Milk", "2024-01-01", "2024-01-02", 1f),
                PantryModel("2", "Yogurt", "2024-01-01", "2024-01-04", 2f)
            ),
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpiryAlertsScreenEmptyPreview() {
    MADProjectTheme(dynamicColour = false) {
        ExpiryAlertsScreen(items = emptyList(), onBackClick = {})
    }
}
