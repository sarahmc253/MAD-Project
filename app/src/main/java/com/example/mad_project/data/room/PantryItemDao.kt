package com.example.mad_project.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * AI-generated: DAO for pantry item CRUD operations.
 * getAllItems() returns a Flow so the UI reacts automatically to any local change.
 * All write operations are suspend functions for safe coroutine usage.
 *
 * Prompt: "Write a Room DAO with Flow-based reactive queries and suspend write functions
 * for an offline-first pantry tracking app."
 */
@Dao
interface PantryItemDao {

    /** Emits the full item list whenever the table changes. Excludes pending-delete rows. */
    @Query("SELECT * FROM pantry_items WHERE pending_delete = 0")
    fun getAllItems(): Flow<List<PantryItemEntity>>

    /** Insert or overwrite a single item. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: PantryItemEntity)

    /** Bulk insert / overwrite — used when applying a Firebase snapshot to Room. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<PantryItemEntity>)

    /** Hard-delete a row (called after a successful Firebase delete). */
    @Query("DELETE FROM pantry_items WHERE itemId = :itemId")
    suspend fun deleteItem(itemId: String)

    /** Hard-delete multiple rows by ID (used when Firebase reports items were removed). */
    @Query("DELETE FROM pantry_items WHERE itemId IN (:ids)")
    suspend fun deleteItems(ids: List<String>)

    /** Flag a row for deletion while offline; keeps it hidden from the UI. */
    @Query("UPDATE pantry_items SET pending_delete = 1 WHERE itemId = :itemId")
    suspend fun markPendingDelete(itemId: String)

    /** Mark a row as successfully written to Firebase. */
    @Query("UPDATE pantry_items SET is_synced = 1 WHERE itemId = :itemId")
    suspend fun markSynced(itemId: String)

    /** Returns items that need to be pushed to Firebase (created/edited offline). */
    @Query("SELECT * FROM pantry_items WHERE is_synced = 0 AND pending_delete = 0")
    suspend fun getUnsyncedItems(): List<PantryItemEntity>

    /** Returns items that need to be deleted from Firebase (deleted offline). */
    @Query("SELECT * FROM pantry_items WHERE pending_delete = 1")
    suspend fun getPendingDeleteItems(): List<PantryItemEntity>

    /** Returns the IDs of all rows that are confirmed synced with Firebase. */
    @Query("SELECT itemId FROM pantry_items WHERE is_synced = 1 AND pending_delete = 0")
    suspend fun getSyncedItemIds(): List<String>

    /** Returns null if the item doesn't exist locally, otherwise its isSynced value. */
    @Query("SELECT is_synced FROM pantry_items WHERE itemId = :itemId")
    suspend fun isSynced(itemId: String): Boolean?
}
