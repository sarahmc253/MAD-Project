package com.example.mad_project.ui.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_project.data.FirebaseWriteResult
import com.example.mad_project.data.InventoryItem
import com.example.mad_project.data.PantryRepository
import com.example.mad_project.data.networkConnectivityFlow
import com.example.mad_project.data.room.NuggetDatabase
import com.example.mad_project.data.toInventoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InventoryUiState(
    val items: List<InventoryItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val writeInProgress: Boolean = false
)

// AI-generated: Updated to use PantryRepository so all reads come from Room
// and deletes go through the offline-first layer instead of calling Firebase directly.
class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PantryRepository = PantryRepository(
        NuggetDatabase.getInstance(application).pantryItemDao()
    )

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    private val isOnline: StateFlow<Boolean> = application.networkConnectivityFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            repository.pantryItems
                .map { pantryItems -> pantryItems.map { it.toInventoryItem() } }
                .collect { inventoryItems ->
                    _uiState.update {
                        it.copy(items = inventoryItems, isLoading = false, errorMessage = null)
                    }
                }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId, isOnline.value).collect { result ->
                when (result) {
                    FirebaseWriteResult.Loading ->
                        _uiState.update { it.copy(writeInProgress = true) }
                    FirebaseWriteResult.Success ->
                        _uiState.update { it.copy(writeInProgress = false) }
                    is FirebaseWriteResult.Error ->
                        _uiState.update { it.copy(writeInProgress = false, errorMessage = result.message) }
                }
            }
        }
    }
}
