package com.example.mad_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mad_project.data.InventoryItem
import com.example.mad_project.data.sampleInventoryItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Item Detail screen. Loads item by id; ready to switch to Room when available.
 */
data class ItemDetailUiState(
    val item: InventoryItem? = null,
    val isLoading: Boolean = true
)

class ItemDetailViewModel(
    private val itemId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemDetailUiState())
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val item = sampleInventoryItems().find { it.id == itemId } ?: sampleInventoryItems().firstOrNull()
            _uiState.update { it.copy(item = item, isLoading = false) }
        }
    }
}

class ItemDetailViewModelFactory(private val itemId: Long) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ItemDetailViewModel(itemId) as T
}
