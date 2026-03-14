package com.example.mad_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
    private val itemId: String,
    private val repository: PantryRepository
) : ViewModel() {

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
        fun factory(itemId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repository =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ShelfScanApplication).repository
                ItemDetailViewModel(itemId, repository)
            }
        }
    }
}
