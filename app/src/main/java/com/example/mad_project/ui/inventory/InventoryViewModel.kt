package com.example.mad_project.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_project.data.FirebaseWriteResult
import com.example.mad_project.data.InventoryItem
import com.example.mad_project.data.deletePantryItem
import com.example.mad_project.data.getPantry
import com.example.mad_project.data.toInventoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InventoryUiState(
    val items: List<InventoryItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val writeInProgress: Boolean = false
)

class InventoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getPantry().collect { pantryItems ->
                _uiState.value = _uiState.value.copy(
                    items = pantryItems.map { it.toInventoryItem() },
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    fun deleteItem(firebaseId: String) {
        viewModelScope.launch {
            deletePantryItem(firebaseId).collect { result ->
                when (result) {
                    FirebaseWriteResult.Loading -> {
                        _uiState.value = _uiState.value.copy(writeInProgress = true)
                    }
                    FirebaseWriteResult.Success -> {
                        _uiState.value = _uiState.value.copy(writeInProgress = false)
                    }
                    is FirebaseWriteResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            writeInProgress = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
}
