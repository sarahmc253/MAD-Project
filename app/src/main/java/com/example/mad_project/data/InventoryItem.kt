package com.example.mad_project.data

import androidx.compose.ui.graphics.Color
import com.example.mad_project.ui.theme.ExpiredRed
import com.example.mad_project.ui.theme.ExpiringSoonOrange
import com.example.mad_project.ui.theme.TextPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

//DUMMY DATA
data class InventoryItem(
    val id: Long,
    val name: String,
    val quantity: String,
    val location: ItemLocation,
    val expiryStatus: ExpiryStatus,
    val expiryDate: String,
    val expiryDisplay: String, // e.g. "Expired 2d ago", "Expires Tomorrow", "Expires in 6 days"
    val purchasedDate: String? = null,
    val notes: String? = null,
    val category: String? = null,
    val imageUrl: String? = null,
    val inStock: Boolean = true,
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
        id = itemId?.hashCode()?.toLong() ?: 0L,
        firebaseId = itemId,
        name = foodName ?: "Unknown Item",
        quantity = quantity?.let { if (it == it.toInt().toFloat()) "${it.toInt()}" else "$it" } ?: "1",
        location = ItemLocation.PANTRY,
        expiryStatus = expiryStatus,
        expiryDate = expiryDate ?: "",
        expiryDisplay = expiryDisplay,
        purchasedDate = dateScanned
    )
}

enum class ItemLocation(val label: String, val chipBg: Color, val chipText: Color) {
    FRIDGE("FRIDGE", com.example.mad_project.ui.theme.FridgeBlue, com.example.mad_project.ui.theme.FridgeBlueText),
    PANTRY("PANTRY", com.example.mad_project.ui.theme.PantryYellow, com.example.mad_project.ui.theme.PantryYellowText),
    FREEZER("FREEZER", com.example.mad_project.ui.theme.FreezerPurple, com.example.mad_project.ui.theme.FreezerPurpleText)
}

enum class ExpiryStatus(val label: String, val colour: Color) {
    EXPIRED("Expired", ExpiredRed),
    EXPIRES_SOON("Expires Soon", ExpiringSoonOrange),
    OK("OK", TextPrimary)
}

fun sampleInventoryItems(): List<InventoryItem> = listOf(
    InventoryItem(1L, "Organic Milk", "1L", ItemLocation.FRIDGE, ExpiryStatus.EXPIRED, "Oct 21, 2023", "Expired 2d ago", "Oct 10, 2023", category = "Dairy"),
    InventoryItem(2L, "Baby Spinach", "250g", ItemLocation.FRIDGE, ExpiryStatus.EXPIRES_SOON, "Oct 24, 2023", "Expires Tomorrow", "Oct 20, 2023", category = "Produce"),
    InventoryItem(3L, "Whole Grain Bread", "1 loaf", ItemLocation.PANTRY, ExpiryStatus.OK, "Oct 29, 2023", "Expires in 6 days", "Oct 18, 2023", category = "Bakery"),
    InventoryItem(4L, "Frozen Mixed Berries", "500g", ItemLocation.FREEZER, ExpiryStatus.OK, "Jan 24, 2024", "Expires in 3 months", "Jul 24, 2023", category = "Frozen"),
    InventoryItem(5L, "Espresso Beans", "1kg", ItemLocation.PANTRY, ExpiryStatus.OK, "Nov 16, 2023", "Expires in 24 days", "Oct 23, 2022", category = "Pantry"),
    InventoryItem(6L, "Greek Yogurt", "500g / 1 pack", ItemLocation.FRIDGE, ExpiryStatus.EXPIRES_SOON, "Oct 24, 2023", "Expiring in 5 days", "Oct 10, 2023", notes = "Unopened. Great for smoothies and morning breakfast bowls. Keep chilled at all times.", category = "Dairy")
)
