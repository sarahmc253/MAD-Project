package com.example.mad_project.ui.item

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mad_project.data.ItemLocation
import com.example.mad_project.ui.theme.ShelfScanGreen

@Composable
fun AddDetailsDialog(
    isManualEntry: Boolean,
    name: String,
    expiry: String,
    quantity: String,
    location: ItemLocation,
    onNameChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onLocationChange: (ItemLocation) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
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
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                Icons.Default.Keyboard,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = if (isManualEntry) "Enter item details" else "Add to list",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Product name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                ExpiryDatePicker(
                    expiry = expiry,
                    onExpiryChange = onExpiryChange,
                    textFieldColors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                LocationDropdown(
                    selectedLocation = location,
                    onLocationSelect = onLocationChange,
                    label = "Location",
                    textFieldColors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ShelfScanGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colorScheme.onSurfaceVariant)
            }
        }
    )
}
