package com.example.mad_project.data

import androidx.compose.ui.graphics.Color
import com.example.mad_project.ui.theme.ChipBg
import com.example.mad_project.ui.theme.ExpiredRed
import com.example.mad_project.ui.theme.ExpiringSoonOrange
import com.example.mad_project.ui.theme.TextPrimary
import com.example.mad_project.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

//DUMMY DATA
data class InventoryItem(
    val id: String,
    val name: String,
    val quantity: String,
    val location: ItemLocation,
    val expiryStatus: ExpiryStatus,
    val expiryDate: String,
    val expiryDisplay: String, // e.g. "Expired 2d ago", "Expires Tomorrow", "Expires in 6 days"
    val firebaseId: String? = null
)

fun PantryModel.toInventoryItem(): InventoryItem {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()

    val expiry = try {
        expiryDate?.let { LocalDate.parse(it, formatter) }
    } catch (e: Exception) {
        null
    }

    val daysUntilExpiry = expiry?.let { ChronoUnit.DAYS.between(today, it) }

    val expiryStatus = when {
        daysUntilExpiry == null -> ExpiryStatus.OK
        daysUntilExpiry < 0 -> ExpiryStatus.EXPIRED
        daysUntilExpiry <= 2 -> ExpiryStatus.EXPIRES_SOON
        else -> ExpiryStatus.OK
    }

    val expiryDisplay = when {
        daysUntilExpiry == null -> expiryDate ?: "Unknown"
        daysUntilExpiry < 0 -> "Expired ${-daysUntilExpiry}d ago"
        daysUntilExpiry == 0L -> "Expires Today"
        daysUntilExpiry == 1L -> "Expires Tomorrow"
        else -> "Expires in $daysUntilExpiry days"
    }

    return InventoryItem(
        id = itemId ?: "",
        firebaseId = itemId,
        name = foodName ?: "Unknown Item",
        quantity = quantity?.let { if (it == it.toInt().toFloat()) "${it.toInt()}" else "$it" } ?: "1",
        location = ItemLocation.entries.firstOrNull { it.name == location } ?: ItemLocation.UNKNOWN,
        expiryStatus = expiryStatus,
        expiryDate = expiryDate ?: "",
        expiryDisplay = expiryDisplay
    )
}

enum class ItemLocation(val label: String, val chipBg: Color, val chipText: Color) {
    FRIDGE("Fridge", com.example.mad_project.ui.theme.FridgeBlue, com.example.mad_project.ui.theme.FridgeBlueText),
    FREEZER("Freezer", com.example.mad_project.ui.theme.FreezerPurple, com.example.mad_project.ui.theme.FreezerPurpleText),
    PANTRY("Pantry", com.example.mad_project.ui.theme.PantryYellow, com.example.mad_project.ui.theme.PantryYellowText),
    OTHER("Other", ChipBg, TextSecondary),
    UNKNOWN("Unknown", ChipBg, TextSecondary)
}

enum class ExpiryStatus(val label: String, val colour: Color) {
    EXPIRED("Expired", ExpiredRed),
    EXPIRES_SOON("Expires Soon", ExpiringSoonOrange),
    OK("OK", TextPrimary)
}
