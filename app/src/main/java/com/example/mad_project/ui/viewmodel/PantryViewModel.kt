package com.example.mad_project.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_project.data.FirebaseWriteResult
import com.example.mad_project.data.PantryModel
import com.example.mad_project.data.PantryRepository
import com.example.mad_project.data.networkConnectivityFlow
import com.example.mad_project.data.room.NuggetDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Sources:
 * https://developer.android.com/topic/libraries/architecture/viewmodel
 *
 * ViewModel for pantry/shopping list data (Firebase Realtime Database).
 *
 * AI-generated additions:
 *   - Repository wiring (Room + Firebase offline-first layer)
 *   - Network connectivity StateFlow
 *   - Firebase listener that feeds Room on reconnect
 *   - syncPendingToFirebase call on reconnect
 *
 * Prompt: "Update an existing Android ViewModel to use an offline-first repository,
 * monitor network connectivity with a Flow, and sync local Room data to Firebase
 * when the device comes back online."
 */
data class PantryUiState(
    val items: List<PantryModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val writeInProgress: Boolean = false
)

class PantryViewModel(application: Application) : AndroidViewModel(application) {

    private val localRepository: PantryRepository = PantryRepository(
        NuggetDatabase.getInstance(application).pantryItemDao()
    )

    private val _uiState = MutableStateFlow(PantryUiState())
    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()

    val isOnline: StateFlow<Boolean> = application.networkConnectivityFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        // UI reads from Room — always available, online or offline.
        viewModelScope.launch {
            localRepository.pantryItems.collect { items ->
                _uiState.update {
                    it.copy(items = items, isLoading = false, errorMessage = null)
                }
            }
        }

        // When the device comes online: flush pending local changes, then listen to Firebase.
        var firebaseListenerJob: Job? = null
        viewModelScope.launch {
            isOnline.collect { online ->
                firebaseListenerJob?.cancel()
                if (online) {
                    // Sync any offline writes/deletes first, then start the live listener.
                    launch(Dispatchers.IO) { localRepository.syncPendingToFirebase() }

                    firebaseListenerJob = launch {
                        localRepository.firebaseItems().collect { firebaseItems ->
                            launch(Dispatchers.IO) {
                                localRepository.updateFromFirebase(firebaseItems)
                            }
                        }
                    }
                }
            }
        }
    }

    fun addItem(item: PantryModel) {
        viewModelScope.launch {
            localRepository.addItem(item, isOnline.value).collect { result ->
                handleWriteResult(result)
            }
        }
    }

    fun updateItem(item: PantryModel) {
        viewModelScope.launch {
            localRepository.updateItem(item, isOnline.value).collect { result ->
                handleWriteResult(result)
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            localRepository.deleteItem(itemId, isOnline.value).collect { result ->
                handleWriteResult(result)
            }
        }
    }

    private fun handleWriteResult(result: FirebaseWriteResult) {
        _uiState.update {
            when (result) {
                FirebaseWriteResult.Loading -> it.copy(writeInProgress = true)
                FirebaseWriteResult.Success -> it.copy(writeInProgress = false)
                is FirebaseWriteResult.Error -> it.copy(writeInProgress = false, errorMessage = result.message)
            }
        }
    }

    fun clearError() {
        _uiState.update { state ->
            state.copy(errorMessage = null)
        }
    }
}
