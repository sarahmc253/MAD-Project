package com.example.mad_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_project.data.ItemLocation
import com.example.mad_project.data.OpenFoodFactsProduct
import com.example.mad_project.data.fetchProductByBarcode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI state for the Add Item (scanner) screen.
 */
data class AddItemUiState(
    val hasScannedItem: Boolean = false,
    val scannedBarcode: String? = null,
    val productInfo: OpenFoodFactsProduct? = null,
    val productLoading: Boolean = false,
    val showEditDetailsDialog: Boolean = false,
    val openedFromManualEntry: Boolean = false,
    val dialogName: String = "",
    val dialogExpiry: String = "",
    val dialogQuantity: String = "1",
    val dialogLocation: ItemLocation = ItemLocation.PANTRY
)

/**
 * ViewModel for Add Item screen: barcode scan state, Open Food Facts lookup, and add-dialog state.
 * Keeps UI logic and coroutine work out of the composable.
 */
class AddItemViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AddItemUiState())
    val uiState: StateFlow<AddItemUiState> = _uiState.asStateFlow()

    /**
     * AI-generated. viewModelScope.launch + _uiState.update + withContext(Dispatchers.IO) for
     * fetchProductByBarcode keeps network work off the main thread and updates state atomically.
     *
     * Prompt: Handle barcode scan and Open Food Facts lookup in ViewModel.
     */
    fun onBarcodeScanned(barcode: String) {
        if (barcode.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    hasScannedItem = true,
                    scannedBarcode = barcode,
                    productInfo = null,
                    productLoading = true
                )
            }
            val product = withContext(Dispatchers.IO) { fetchProductByBarcode(barcode) }
            _uiState.update {
                it.copy(productInfo = product, productLoading = false)
            }
        }
    }

    fun clearScan() {
        _uiState.update {
            it.copy(
                hasScannedItem = false,
                scannedBarcode = null,
                productInfo = null,
                productLoading = false
            )
        }
    }

    fun openAddDialog(manualEntry: Boolean) {
        _uiState.update {
            it.copy(
                showEditDetailsDialog = true,
                openedFromManualEntry = manualEntry,
                dialogName = if (manualEntry) "" else (it.productInfo?.displayName ?: it.scannedBarcode ?: ""),
                dialogExpiry = "",
                dialogQuantity = "1",
                dialogLocation = ItemLocation.PANTRY
            )
        }
    }

    fun setDialogName(name: String) {
        _uiState.update { it.copy(dialogName = name) }
    }

    fun setDialogExpiry(expiry: String) {
        _uiState.update { it.copy(dialogExpiry = expiry) }
    }

    fun setDialogQuantity(quantity: String) {
        _uiState.update { it.copy(dialogQuantity = quantity.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun setDialogLocation(location: ItemLocation) {
        _uiState.update { it.copy(dialogLocation = location) }
    }

    fun dismissDialog() {
        _uiState.update {
            it.copy(showEditDetailsDialog = false, openedFromManualEntry = false)
        }
    }

    /**
     * AI-generated. Returns (name, expiryOrNull, quantity, locationName) for the add callback.
     * locationName is the ItemLocation enum name (e.g. "FRIDGE") for Firebase.
     *
     * Prompt: Read dialog state and return values for add.
     */
    fun getDialogValuesForAdd(): AddDialogValues {
        val s = _uiState.value
        val name = s.dialogName.trim().ifBlank { "Manual item" }
        val expiry = s.dialogExpiry.trim().takeIf { it.isNotBlank() }
        val quantity = s.dialogQuantity.toFloatOrNull() ?: 1f
        val locationName = s.dialogLocation.name
        return AddDialogValues(name, expiry, quantity, locationName)
    }
}

data class AddDialogValues(
    val name: String,
    val expiryDate: String?,
    val quantity: Float,
    val locationName: String
)
