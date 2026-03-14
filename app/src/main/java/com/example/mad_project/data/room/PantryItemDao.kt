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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: PantryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<PantryItemEntity>)

    @Query("DELETE FROM pantry_items WHERE itemId = :itemId")
    suspend fun deleteItem(itemId: String)

    @Query("DELETE FROM pantry_items WHERE itemId IN (:ids)")
    suspend fun deleteItems(ids: List<String>)

    @Query("UPDATE pantry_items SET pending_delete = 1 WHERE itemId = :itemId")
    suspend fun markPendingDelete(itemId: String)

    @Query("UPDATE pantry_items SET is_synced = 1 WHERE itemId = :itemId")
    suspend fun markSynced(itemId: String)

    @Query("SELECT * FROM pantry_items WHERE is_synced = 0 AND pending_delete = 0")
    suspend fun getUnsyncedItems(): List<PantryItemEntity>

    @Query("SELECT * FROM pantry_items WHERE pending_delete = 1")
    suspend fun getPendingDeleteItems(): List<PantryItemEntity>

    @Query("SELECT itemId FROM pantry_items WHERE is_synced = 1 AND pending_delete = 0")
    suspend fun getSyncedItemIds(): List<String>

    @Query("SELECT is_synced FROM pantry_items WHERE itemId = :itemId")
    suspend fun isSynced(itemId: String): Boolean?
}
