package com.example.mad_project.ui.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mad_project.data.ItemLocation

/**
 * AI-generated. Location dropdown for add-item dialog; options are Fridge, Freezer, Pantry, Other.
 * Prompt: Allow user to choose storage location from a dropdown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LocationDropdown(
    selectedLocation: ItemLocation,
    onLocationSelect: (ItemLocation) -> Unit,
    label: String,
    textFieldColors: TextFieldColors,
    modifier: Modifier = Modifier
) {
    val locationOptions = ItemLocation.entries.filter { it != ItemLocation.UNKNOWN }
    var expanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLocation.label,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = colorScheme.surface
        ) {
            locationOptions.forEach { loc ->
                DropdownMenuItem(
                    text = {
                        Text(
                            loc.label,
                            color = if (loc == selectedLocation) colorScheme.primary else colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onLocationSelect(loc)
                        expanded = false
                    }
                )
            }
        }
    }
}
