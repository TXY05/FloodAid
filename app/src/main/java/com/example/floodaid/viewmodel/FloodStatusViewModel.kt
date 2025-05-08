package com.example.floodaid.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.floodaid.repository.FirestoreRepository
import com.example.floodaid.screen.floodstatus.FloodHistoryEntity
import com.example.floodaid.screen.floodstatus.FloodStatusDao
import com.example.floodaid.screen.floodstatus.FloodStatusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LocationStatus(
    val location: String,
    val status: String
)

data class FloodStatusUiState(
    val floodData: List<LocationStatus> = emptyList(),
    val selectedLocation: String? = null,
    val showDialog: Boolean = false,
    val currentStatus: String
)

class FloodStatusViewModel(
    private val repository: FloodStatusRepository,
    private val dao: FloodStatusDao,
    private val firestoreRepository: FirestoreRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val locations = repository.getAllLocations().asLiveData()
    private val _uiState = MutableStateFlow(
        FloodStatusUiState(
            currentStatus = "Unknown",
            selectedLocation = savedStateHandle.get<String>("selectedLocation")
        )
    )
    val uiState: StateFlow<FloodStatusUiState> = _uiState

    private val _historyState = MutableStateFlow<List<FloodHistoryEntity>>(emptyList())
    val historyState: StateFlow<List<FloodHistoryEntity>> = _historyState

    init {
        observeFirestoreStatus()
        // Restore selected location if it exists
        savedStateHandle.get<String>("selectedLocation")?.let { location ->
            selectLocation(location)
        }
    }

    private fun observeFirestoreStatus() {
        viewModelScope.launch {
            firestoreRepository.listenToFloodStatus().collectLatest { statusList ->
                _uiState.value = _uiState.value.copy(
                    floodData = statusList.map { LocationStatus(it.location, it.status) }
                )

                // Also update local Room DB (optional syncing back to Room)
                statusList.forEach {
                    repository.insertOrUpdateLocation(it.location, it.status)
                }
            }
        }
    }

    fun selectLocation(location: String) {
        savedStateHandle["selectedLocation"] = location
        _uiState.value = _uiState.value.copy(selectedLocation = location)
        fetchFloodStatusForLocation(location)
    }

    private fun fetchFloodStatusForLocation(location: String) {
        viewModelScope.launch {
            val status = repository.getFloodStatusForLocation(location)
            _uiState.value = _uiState.value.copy(currentStatus = status)
        }
    }

    fun clearSelectedLocation() {
        savedStateHandle.remove<String>("selectedLocation")
        _uiState.value = _uiState.value.copy(selectedLocation = null)
    }

    fun showDialog() {
        _uiState.value = _uiState.value.copy(showDialog = true)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false)
    }

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun updateFloodStatus(location: String, status: String) {
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            try {
                repository.updateFloodStatus(location, status, currentDate, currentTime)
                showSnackbar("Status updated successfully!")
            } catch (e: Exception) {
                showSnackbar("Failed to update status: ${e.message}")
            }
        }
    }


    fun syncFromFirestore() {
        viewModelScope.launch {
            repository.syncFloodStatusFromFirestore()
        }
    }

    fun fetchFloodHistory(location: String) {
        viewModelScope.launch {
            firestoreRepository.listenToFloodHistory(location).collectLatest { historyList ->
                _historyState.value = historyList
            }
        }
    }

}