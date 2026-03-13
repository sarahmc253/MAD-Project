package com.example.mad_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * UI state for the home screen (tab selection and overlay visibility).
 */
data class HomeUiState(
    val currentTab: String = "Inventory",
    val addItemOverlayVisible: Boolean = false,
    val detailItemId: String? = null
)

/**
 * ViewModel for home screen navigation state (tabs, add overlay, detail overlay).
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun setCurrentTab(tab: String) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun showAddItemOverlay() {
        _uiState.update { it.copy(addItemOverlayVisible = true) }
    }

    fun hideAddItemOverlay() {
        _uiState.update { it.copy(addItemOverlayVisible = false) }
    }

    fun openItemDetail(itemId: String) {
        _uiState.update { it.copy(detailItemId = itemId) }
    }

    fun closeItemDetail() {
        _uiState.update { it.copy(detailItemId = null) }
    }
}
