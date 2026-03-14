package com.example.mad_project.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mad_project.ShelfScanApplication
import com.example.mad_project.data.InventoryItem
import com.example.mad_project.data.PantryRepository
import com.example.mad_project.data.toInventoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    savedStateHandle: SavedStateHandle,
    private val repository: PantryRepository
) : ViewModel() {

    private val itemId: String = savedStateHandle["itemId"] ?: ""

    private val _uiState = MutableStateFlow(ItemDetailUiState())
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.pantryItems.collect { pantryItems ->
                val item = pantryItems.map { it.toInventoryItem() }.find { it.id == itemId }
                _uiState.update { it.copy(item = item, isLoading = false) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val repository =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ShelfScanApplication).repository
                ItemDetailViewModel(savedStateHandle, repository)
            }
        }
    }
}
