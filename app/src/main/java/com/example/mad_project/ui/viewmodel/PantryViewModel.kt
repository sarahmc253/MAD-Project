package com.example.mad_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_project.data.FirebaseWriteResult
import com.example.mad_project.data.PantryModel
import com.example.mad_project.data.addPantryItem
import com.example.mad_project.data.deletePantryItem
import com.example.mad_project.data.getPantry
import com.example.mad_project.data.writePantryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for pantry/shopping list data (Firebase Realtime Database).
 * Follows Android app architecture: UI state in ViewModel, data operations via repository layer.
 */
data class PantryUiState(
    val items: List<PantryModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val writeInProgress: Boolean = false
)

class PantryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PantryUiState())
    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getPantry().collect { items ->
                _uiState.value = _uiState.value.copy(
                    items = items,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    fun addItem(item: PantryModel) {
        viewModelScope.launch {
            addPantryItem(item).collect { result ->
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

    fun updateItem(item: PantryModel) {
        val itemId = item.itemId ?: return
        viewModelScope.launch {
            writePantryItem(itemId, item).collect { result ->
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

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            deletePantryItem(itemId).collect { result ->
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

    fun clearError() {
        _uiState.update { state ->
            state.copy(errorMessage = null)
        }
    }
}
