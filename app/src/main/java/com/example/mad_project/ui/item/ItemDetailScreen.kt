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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mad_project.data.InventoryItem
import com.example.mad_project.data.ItemLocation
import com.example.mad_project.ui.theme.*
import com.example.mad_project.ui.viewmodel.ItemDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    itemId: String,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val viewModel: ItemDetailViewModel = viewModel(factory = ItemDetailViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val item = uiState.item

    var expiryState by remember(item?.expiryDate) { mutableStateOf(item?.expiryDate ?: "") }
    var showExpiryPicker by remember { mutableStateOf(false) }
    var showQuantityDialog by remember { mutableStateOf(false) }
    var quantityInput by remember { mutableStateOf("") }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationState by remember(item?.location) { mutableStateOf(item?.location ?: ItemLocation.PANTRY) }
    val colorScheme = MaterialTheme.colorScheme

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

    if (showLocationDialog) {
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colorScheme.onSurface,
            unfocusedTextColor = colorScheme.onSurface,
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.outline,
            cursorColor = colorScheme.primary,
            focusedLabelColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.onSurfaceVariant,
            focusedContainerColor = colorScheme.surface,
            unfocusedContainerColor = colorScheme.surface
        )
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            containerColor = colorScheme.surfaceVariant,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Change Location",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant
                )
            },
            text = {
                LocationDropdown(
                    selectedLocation = locationState,
                    onLocationSelect = { locationState = it },
                    label = "Location",
                    textFieldColors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateLocation(locationState.name)
                        showLocationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShelfScanGreen),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Update", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Cancel", color = colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    ExpiryPickerDialog(
        show = showExpiryPicker,
        currentExpiry = expiryState,
        onDismiss = { showExpiryPicker = false },
        onExpiryChange = {
            expiryState = it
            viewModel.updateExpiryDate(it)
        }
    )

    if (showQuantityDialog) {
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colorScheme.onSurface,
            unfocusedTextColor = colorScheme.onSurface,
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.outline,
            cursorColor = colorScheme.primary,
            focusedLabelColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.onSurfaceVariant,
            focusedContainerColor = colorScheme.surface,
            unfocusedContainerColor = colorScheme.surface
        )
        AlertDialog(
            onDismissRequest = { showQuantityDialog = false },
            containerColor = colorScheme.surfaceVariant,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Adjust Quantity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant
                )
            },
            text = {
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateQuantity(quantityInput)
                        showQuantityDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShelfScanGreen),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Update", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showQuantityDialog = false }) {
                    Text("Cancel", color = colorScheme.onSurfaceVariant)
                }
            }
        )
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
                    IconButton(onClick = {
                        currentItem.firebaseId?.let { viewModel.deleteItem(it) }
                        onDeleteClick()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpiredRed)
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
                    color = TextPrimary,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
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
                    color = currentItem.location.chipBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            currentItem.location.label,
                            color = currentItem.location.chipText,
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
                    value = expiryState.ifBlank { "—" },
                    actionLabel = "Adjust",
                    actionIcon = Icons.Default.CalendarToday,
                    onClick = { showExpiryPicker = true }
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
                    label = "LOCATION",
                    value = currentItem.location.label,
                    actionLabel = "Change",
                    actionIcon = Icons.Default.Edit,
                    onClick = {
                        locationState = currentItem.location
                        showLocationDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemDetailsScreenPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme {
        ItemDetailsScreen(
            itemId = "",
            onBackClick = {},
            onDeleteClick = {}
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
