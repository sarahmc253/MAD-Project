package com.example.mad_project.data

import android.util.Log
import com.example.mad_project.data.room.PantryItemDao
import com.example.mad_project.data.room.PantryItemEntity
import com.example.mad_project.data.room.toEntity
import com.example.mad_project.data.room.toPantryModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private const val REPO_TAG = "PantryRepository"

/**
 * AI-generated: Offline-first repository that keeps Room as the single source of truth
 * for the UI and Firebase as the remote store.
 *
 * Write strategy:
 *   Online  → write to Room (isSynced = false), write to Firebase, mark synced on success.
 *   Offline → write to Room (isSynced = false); Firebase write is deferred.
 *   Delete online  → delete from Firebase, then hard-delete from Room.
 *   Delete offline → mark pendingDelete = true in Room; deferred until reconnect.
 *
 * Sync strategy (called by the ViewModel on reconnect):
 *   1. Push all unsynced Room rows to Firebase.
 *   2. Execute all pending-delete rows against Firebase, then remove from Room.
 *   3. Apply the fresh Firebase snapshot to Room, preserving any local pending edits.
 *
 * Prompt: "Design an offline-first Room + Firebase repository in Kotlin with Flow-based
 * reactive queries, offline write queuing, and sync-on-reconnect logic."
 *
 * Then I had to make sure that this used the correct functions I made from db.kt, and I also
 * modified this file to be a bit more clean.
 */
class PantryRepository(private val dao: PantryItemDao) {

    /** The application always observes Room — never Firebase directly. */
    val pantryItems: Flow<List<PantryModel>> = dao.getAllItems()
        .map { entities -> entities.map { it.toPantryModel() } }

    fun firebaseItems(): Flow<List<PantryModel>> = getPantry()

    // ── CRUD ──────────────────────────────────────────────────────────────────

    fun generateItemID(): String {
        return java.util.UUID.randomUUID().toString()
    }

    fun updateItem(item: PantryModel, online: Boolean): Flow<FirebaseWriteResult> {
        val itemWithId = if (item.itemId != null) item else item.copy(itemId = generateItemID())
        val itemId = itemWithId.itemId!!

        return if (online) {
            writePantryItem(itemId, itemWithId)
                .onEach { result ->
                    when (result) {
                        FirebaseWriteResult.Loading ->
                            dao.upsertItem(itemWithId.toEntity(isSynced = false))
                        FirebaseWriteResult.Success ->
                            dao.upsertItem(itemWithId.toEntity(isSynced = true))
                        is FirebaseWriteResult.Error ->
                            Log.e(REPO_TAG, "updateItem Firebase error: ${result.message}")
                    }
                }
        } else {
            flow {
                emit(FirebaseWriteResult.Loading)
                dao.upsertItem(itemWithId.toEntity(isSynced = false))
                emit(FirebaseWriteResult.Success)
            }
        }
    }

    fun deleteItem(itemId: String, online: Boolean): Flow<FirebaseWriteResult> {
        return if (online) {
            deletePantryItem(itemId)
                .onEach { result ->
                    if (result is FirebaseWriteResult.Success) {
                        dao.deleteItem(itemId)
                    }
                }
        } else {
            flow {
                emit(FirebaseWriteResult.Loading)
                dao.markPendingDelete(itemId)
                emit(FirebaseWriteResult.Success)
            }
        }
    }

    // ── Sync ──────────────────────────────────────────────────────────────────
    suspend fun syncPendingItems(entity: PantryItemEntity){
        try {
            writePantryItem(entity.itemId, entity.toPantryModel()).collect { result ->
                if (result is FirebaseWriteResult.Success) dao.markSynced(entity.itemId)
            }
            Log.d(REPO_TAG, "Synced item ${entity.itemId}")
        } catch (e: Exception) {
            Log.e(REPO_TAG, "Failed to sync item ${entity.itemId}", e)
        }
    }

    suspend fun deletePendingItems(entity: PantryItemEntity){
        try {
            deletePantryItem(entity.itemId).collect { result ->
                if (result is FirebaseWriteResult.Success) dao.deleteItem(entity.itemId)
            }
            Log.d(REPO_TAG, "Deleted item ${entity.itemId}")
        } catch (e: Exception) {
            Log.e(REPO_TAG, "Failed to delete item ${entity.itemId}", e)
        }
    }
    suspend fun syncPendingToFirebase() {
        for (entity in dao.getUnsyncedItems()) {
            syncPendingItems(entity)
        }

        for (entity in dao.getPendingDeleteItems()) {
            deletePendingItems(entity)
        }
    }
    suspend fun updateFromFirebase(firebaseItems: List<PantryModel>) {
        val firebaseIds = firebaseItems.mapNotNull { it.itemId }.toSet()

        val toUpsert = firebaseItems.mapNotNull { model ->
            val id = model.itemId ?: return@mapNotNull null
            // Preserve any local edit that hasn't been pushed yet.
            if (dao.isSynced(id) == false) return@mapNotNull null
            model.toEntity(isSynced = true)
        }
        dao.upsertItems(toUpsert)

        // Remove rows that no longer exist in Firebase.
        val staleIds = dao.getSyncedItemIds().filter { it !in firebaseIds }
        if (staleIds.isNotEmpty()) {
            dao.deleteItems(staleIds)
        }
    }
}
