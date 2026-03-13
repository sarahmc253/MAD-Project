package com.example.mad_project.ui.components

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
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

/**
 * Sources:
 * https://developer.android.com/topic/libraries/architecture/viewmodel
 *
 * We chose to use a ViewModel because it looked like a good and simple way to manage
 * coroutines for realtime firebase operations.
 *
 * prompt: Use this guide https://developer.android.com/topic/libraries/architecture/viewmodel to
 * implement a simple ViewModel that performs crud operations on a firebase database. The attached files
 * show the relevant functions and models to use.
 */
data class PantryUiState(
    val items: List<PantryModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val writeInProgress: Boolean = false,
    val alertItems: List<PantryModel> = emptyList(),
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0,
)

class PantryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PantryUiState())
    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getPantry().collect { items ->
                val (alertItems, expiringSoonCount, expiredCount) = computeAlerts(items)
                _uiState.value = _uiState.value.copy(
                    items = items,
                    isLoading = false,
                    errorMessage = null,
                    alertItems = alertItems,
                    expiringSoonCount = expiringSoonCount,
                    expiredCount = expiredCount,
                )
            }
        }
    }

    // Items expiring within 48 hours of now (expiringSoon) or already past expiry (expired).
    private fun computeAlerts(items: List<PantryModel>): Triple<List<PantryModel>, Int, Int> {
        val now = System.currentTimeMillis()
        val cutoffMillis = now + 48L * 60 * 60 * 1000
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val expiringSoon = mutableListOf<PantryModel>()
        val expired = mutableListOf<PantryModel>()

        items.forEach { item ->
            val expiryTime = item.expiryDate?.let {
                runCatching { fmt.parse(it)?.time }.getOrNull()
            } ?: return@forEach

            when {
                expiryTime < now -> expired.add(item)
                expiryTime <= cutoffMillis -> expiringSoon.add(item)
            }
        }

        return Triple(expiringSoon + expired, expiringSoon.size, expired.size)
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
}