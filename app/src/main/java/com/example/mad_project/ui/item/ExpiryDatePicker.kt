package com.example.mad_project.ui.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mad_project.ui.theme.ShelfScanGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * AI-generated. Expiry date calendar picker; opens a DatePickerDialog when the field is tapped.
 * Prompt: Use a calendar selector for expiry date instead of a text box.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpiryPickerDialog(
    show: Boolean,
    currentExpiry: String,
    onDismiss: () -> Unit,
    onExpiryChange: (String) -> Unit
) {
    if (!show) return
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val initialMillis = runCatching {
        currentExpiry.trim().takeIf { it.isNotBlank() }?.let { dateFormatter.parse(it)?.time }
    }.getOrNull() ?: Calendar.getInstance().timeInMillis
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        yearRange = (Calendar.getInstance().get(Calendar.YEAR) - 1)..(Calendar.getInstance().get(Calendar.YEAR) + 10)
    )
    val datePickerColors = DatePickerDefaults.colors(
        containerColor = Color.White,
        titleContentColor = Color.Black,
        headlineContentColor = Color.Black,
        weekdayContentColor = Color.Black,
        subheadContentColor = Color.Black,
        navigationContentColor = Color.Black,
        yearContentColor = Color.Black,
        disabledYearContentColor = Color.Gray,
        currentYearContentColor = Color.Black,
        selectedYearContentColor = Color.White,
        dayContentColor = Color.Black,
        disabledDayContentColor = Color.Gray,
        selectedDayContentColor = Color.White,
        todayContentColor = Color.Black,
        todayDateBorderColor = Color.Black
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = datePickerColors,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onExpiryChange(dateFormatter.format(Date(millis)))
                    }
                    onDismiss()
                }
            ) { Text("OK", color = ShelfScanGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Black) }
        }
    ) {
        DatePicker(state = datePickerState, colors = datePickerColors, showModeToggle = false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpiryDatePicker(
    expiry: String,
    onExpiryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textFieldColors: TextFieldColors? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = expiry,
            onValueChange = {},
            readOnly = true,
            label = { Text("Expiry date (optional)") },
            placeholder = { Text("Select date", color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            trailingIcon = {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date", tint = colorScheme.onSurfaceVariant)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors ?: OutlinedTextFieldDefaults.colors()
        )
        Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
    }

    ExpiryPickerDialog(
        show = showDatePicker,
        currentExpiry = expiry,
        onDismiss = { showDatePicker = false },
        onExpiryChange = onExpiryChange
    )
}
