package com.example.mad_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_project.data.InventoryItem
import com.example.mad_project.data.ExpiryStatus
import com.example.mad_project.data.sampleInventoryItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Inventory list screen.
 * Ready to be backed by Room flow when local persistence is added.
 */
data class InventoryListUiState(
    val items: List<InventoryItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val statusFilter: String = "All",
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0
)

/**
 * ViewModel for Inventory list: search, filters, and derived counts.
 */
class InventoryListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryListUiState())
    val uiState: StateFlow<InventoryListUiState> = _uiState.asStateFlow()

    private fun computeCounts(items: List<InventoryItem>): Pair<Int, Int> {
        val expiringSoon = items.count { it.expiryStatus == ExpiryStatus.EXPIRES_SOON }
        val expired = items.count { it.expiryStatus == ExpiryStatus.EXPIRED }
        return expiringSoon to expired
    }

    init {
        viewModelScope.launch {
            val all = sampleInventoryItems()
            val (expiringSoon, expired) = computeCounts(all)

            _uiState.update { state ->
                state.copy(
                    items = all,
                    expiringSoonCount = expiringSoon,
                    expiredCount = expired
                )
            }
        }
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
}
