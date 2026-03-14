package com.example.mad_project.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mad_project.data.InventoryItem
import com.example.mad_project.data.PantryRepository
import com.example.mad_project.data.room.ShelfScanDatabase
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
    application: Application
) : AndroidViewModel(application) {

    private val repository: PantryRepository = PantryRepository(
        ShelfScanDatabase.getInstance(application).pantryItemDao()
    )

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
}

class ItemDetailViewModelFactory(
    private val itemId: String,
    private val application: Application
) : ViewModelProvider.Factory {

    /**
     * AI-generated. ViewModelProvider.Factory is required when a ViewModel has constructor
     * parameters; the generic create(modelClass): T and unchecked cast are boilerplate the
     * compiler cannot verify.
     *
     * Sources: https://developer.android.com/topic/libraries/architecture/viewmodel#viewmodel-with-parameters
     *
     * Prompt: Factory so ItemDetailViewModel can be created with itemId in Compose.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ItemDetailViewModel(itemId, application) as T
}
