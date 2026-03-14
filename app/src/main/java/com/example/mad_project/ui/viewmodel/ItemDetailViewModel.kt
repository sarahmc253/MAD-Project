package com.example.mad_project.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mad_project.ShelfScanApplication
import com.example.mad_project.data.InventoryItem
import com.example.mad_project.data.PantryModel
import com.example.mad_project.data.PantryRepository
import com.example.mad_project.data.networkConnectivityFlow
import com.example.mad_project.data.toInventoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Item Detail screen. Loads item by id from Room via PantryRepository.
 */
data class ItemDetailUiState(
    val item: InventoryItem? = null,
    val isLoading: Boolean = true
)

class ItemDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val repository: PantryRepository
) : AndroidViewModel(application) {

    private val itemId: String = savedStateHandle["itemId"] ?: ""

    private val _uiState = MutableStateFlow(ItemDetailUiState())
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    private val isOnline: StateFlow<Boolean> = application.networkConnectivityFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            repository.pantryItems.collect { pantryItems ->
                val item = pantryItems.map { it.toInventoryItem() }.find { it.id == itemId }
                _uiState.update { it.copy(item = item, isLoading = false) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.syncPendingToFirebase()
        }
    }

    fun updateQuantity(newQuantity: String) {
        val currentItem = _uiState.value.item ?: return
        val updatedModel = PantryModel(
            itemId = currentItem.firebaseId,
            foodName = currentItem.name,
            expiryDate = currentItem.expiryDate,
            quantity = newQuantity.toIntOrNull()?.toFloat(),
            location = currentItem.location.name,
            notes = currentItem.notes,
            category = currentItem.category,
            imageUrl = currentItem.imageUrl
        )
        viewModelScope.launch {
            repository.upsertItem(updatedModel, isOnline.value).collect {}
        }
    }

    fun updateExpiryDate(newDate: String) {
        val currentItem = _uiState.value.item ?: return
        val updatedModel = PantryModel(
            itemId = currentItem.firebaseId,
            foodName = currentItem.name,
            expiryDate = newDate,
            quantity = currentItem.quantity.toFloatOrNull(),
            location = currentItem.location.name,
            notes = currentItem.notes,
            category = currentItem.category,
            imageUrl = currentItem.imageUrl
        )
        viewModelScope.launch {
            repository.upsertItem(updatedModel, isOnline.value).collect {}
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ShelfScanApplication
                ItemDetailViewModel(app, savedStateHandle, app.repository)
            }
        }
    }
}
