package com.example.mad_project.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_project.data.FirebaseWriteResult
import com.example.mad_project.data.InventoryItem
import com.example.mad_project.data.PantryModel
import com.example.mad_project.ShelfScanApplication
import com.example.mad_project.data.PantryRepository
import com.example.mad_project.data.networkConnectivityFlow
import com.example.mad_project.data.toInventoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sources:
 * https://developer.android.com/topic/libraries/architecture/viewmodel
 *
 * ViewModel for pantry/shopping list data (Firebase Realtime Database).
 *
 * AI-generated additions:
 *   - Repository wiring (Room + Firebase offline-first layer)
 *   - Network connectivity StateFlow
 *   - Firebase listener that feeds Room on reconnect
 *   - syncPendingToFirebase call on reconnect
 *
 * Prompt: "Update an existing Android ViewModel to use an offline-first repository,
 * monitor network connectivity with a Flow, and sync local Room data to Firebase
 * when the device comes back online."
 */

data class InventoryUiState(
    val items: List<InventoryItem> = emptyList(),
    val pantryItems: List<PantryModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val writeInProgress: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val statusFilter: String = "All",
    val viewMode: String = "LIST"
)

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PantryRepository = (application as ShelfScanApplication).repository

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    val isOnline: StateFlow<Boolean> = application.networkConnectivityFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            repository.pantryItems.collect { pantryItems ->
                _uiState.update {
                    it.copy(
                        items = pantryItems.map { p -> p.toInventoryItem() },
                        pantryItems = pantryItems,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }

        // When the device comes online: flush pending local changes, then listen to Firebase.
        var firebaseListenerJob: Job? = null
        viewModelScope.launch {
            isOnline.collect { online ->
                firebaseListenerJob?.cancel()
                if (online) {
                    launch(Dispatchers.IO) { repository.syncPendingToFirebase() }
                    firebaseListenerJob = launch {
                        repository.firebaseItems().collect { firebaseItems ->
                            launch(Dispatchers.IO) {
                                repository.updateFromFirebase(firebaseItems)
                            }
                        }
                    }
                }
            }
        }
    }

    fun upsertItem(item: PantryModel) {
        viewModelScope.launch {
            repository.upsertItem(item, isOnline.value).collect { result ->
                handleWriteResult(result)
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId, isOnline.value).collect { result ->
                handleWriteResult(result)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setStatusFilter(filter: String) {
        _uiState.update { it.copy(statusFilter = filter) }
    }

    fun setViewMode(mode: String) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    private fun handleWriteResult(result: FirebaseWriteResult) {
        _uiState.update {
            when (result) {
                FirebaseWriteResult.Loading -> it.copy(writeInProgress = true)
                FirebaseWriteResult.Success -> it.copy(writeInProgress = false)
                is FirebaseWriteResult.Error -> it.copy(writeInProgress = false, errorMessage = result.message)
            }
        }
    }
}
