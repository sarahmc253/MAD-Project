package com.example.mad_project.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mad_project.data.PantryModel

/**
 * AI-generated: Room entity mirroring PantryModel for local offline storage.
 * Two extra columns track sync state:
 *   - isSynced: false when this item has a pending write not yet pushed to Firebase.
 *   - pendingDelete: true when the user deleted offline; row is kept until Firebase
 *     can be reached and the remote record removed.
 *
 * Prompt: "Create a Room @Entity for an offline-first app that mirrors a Firebase model
 * and adds isSynced / pendingDelete flags for sync tracking."
 */
@Entity(tableName = "pantry_items")
data class PantryItemEntity(
    @PrimaryKey val itemId: String,
    @ColumnInfo(name = "food_name") val foodName: String? = null,
    @ColumnInfo(name = "date_scanned") val dateScanned: String? = null,
    @ColumnInfo(name = "expiry_date") val expiryDate: String? = null,
    val quantity: Float? = null,
    val location: String? = null,
    val notes: String? = null,
    val category: String? = null,
    @ColumnInfo(name = "image_url") val imageUrl: String? = null,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false,
    @ColumnInfo(name = "pending_delete") val pendingDelete: Boolean = false
)

fun PantryItemEntity.toPantryModel() = PantryModel(
    itemId = itemId,
    foodName = foodName,
    dateScanned = dateScanned,
    expiryDate = expiryDate,
    quantity = quantity,
    location = location,
    notes = notes,
    category = category,
    imageUrl = imageUrl
)

fun PantryModel.toEntity(isSynced: Boolean = false, pendingDelete: Boolean = false) =
    PantryItemEntity(
        itemId = itemId ?: java.util.UUID.randomUUID().toString(),
        foodName = foodName,
        dateScanned = dateScanned,
        expiryDate = expiryDate,
        quantity = quantity,
        location = location,
        notes = notes,
        category = category,
        imageUrl = imageUrl,
        isSynced = isSynced,
        pendingDelete = pendingDelete
    )
