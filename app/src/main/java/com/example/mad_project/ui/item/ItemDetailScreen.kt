package com.example.mad_project.ui.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.example.mad_project.ui.components.LoadingView
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mad_project.data.InventoryItem
import com.example.mad_project.ui.theme.*
import com.example.mad_project.ui.viewmodel.ItemDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    itemId: String,
    onBackClick: () -> Unit,
) {
    val viewModel: ItemDetailViewModel = viewModel(factory = ItemDetailViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val item = uiState.item

    val colorScheme = MaterialTheme.colorScheme
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var showQuantityDialog by remember { mutableStateOf(false) }
    var quantityInput by remember { mutableStateOf("") }

    if (uiState.isLoading) {
        LoadingView()
        return
    }
    val currentItem = item
    if (currentItem == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Item not found", color = TextSecondary)
        }
        return
    }

    if (showQuantityDialog) {
        AlertDialog(
            onDismissRequest = { showQuantityDialog = false },
            title = { Text("Adjust Quantity") },
            text = {
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Quantity") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateQuantity(quantityInput)
                    showQuantityDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showQuantityDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        viewModel.updateExpiryDate(newDate)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface,
                    navigationIconContentColor = colorScheme.onSurface,
                    actionIconContentColor = colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.surfaceVariant)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    currentItem.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ShelfScanGreen
                ) {
                    Text(
                        "IN STOCK",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ExpiringSoonOrangeLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = ExpiringSoonOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            currentItem.expiryDisplay,
                            color = ExpiringSoonOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChipBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            currentItem.category ?: currentItem.location.label,
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailCard(
                    modifier = Modifier.weight(1f),
                    label = "EXPIRY DATE",
                    value = currentItem.expiryDate,
                    actionLabel = "Change",
                    actionIcon = Icons.Default.CalendarToday,
                    onClick = { showDatePicker = true }
                )
                DetailCard(
                    modifier = Modifier.weight(1f),
                    label = "QUANTITY",
                    value = currentItem.quantity,
                    actionLabel = "Adjust",
                    actionIcon = Icons.Default.Add,
                    onClick = {
                        quantityInput = currentItem.quantity
                        showQuantityDialog = true
                    }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailCard(
                    modifier = Modifier.weight(1f),
                    label = "PURCHASED",
                    value = currentItem.purchasedDate ?: "—"
                )
                DetailCard(
                    modifier = Modifier.weight(1f),
                    label = "LOCATION",
                    value = currentItem.location.label
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp)
            ) {
                Text(
                    "NOTES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    currentItem.notes ?: "No notes.",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemDetailsScreenPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme {
        ItemDetailsScreen(
            itemId = "",
            onBackClick = {}
        )
    }
}

@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    actionLabel: String? = null,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                label,
                fontSize = 10.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (actionLabel != null && actionIcon != null && onClick != null) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        actionIcon,
                        contentDescription = null,
                        tint = ShelfScanGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(actionLabel, color = ShelfScanGreen, fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailCardPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme {
        DetailCard(
            label = "Location",
            value = "Fridge",
            actionLabel = "Change",
            actionIcon = androidx.compose.material.icons.Icons.Default.Edit,
            onClick = {}
        )
    }
}
