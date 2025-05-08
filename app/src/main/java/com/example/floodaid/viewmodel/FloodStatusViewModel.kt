package com.example.floodaid.viewmodel

import androidx.lifecycle.SavedStateHandle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.floodaid.roomDatabase.Entities.FloodMarker
import com.example.floodaid.roomDatabase.Repository.FirestoreRepository
import com.example.floodaid.screen.floodstatus.FloodHistoryEntity
import com.example.floodaid.screen.floodstatus.FloodStatusDao
import com.example.floodaid.screen.floodstatus.FloodStatusRepository
import com.example.floodaid.screen.map_UI.MapViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit

// Add a SaveState enum to track saving status
enum class SaveState {
    IDLE,
    SAVING,
    SUCCESS,
    ERROR
}

data class LocationStatus(
    val location: String,
    val status: String
)

data class FloodStatusUiState(
    val floodData: List<LocationStatus> = emptyList(),
    val selectedLocation: String? = null,
    val showDialog: Boolean = false,
    val currentStatus: String = "Unknown",
    val saveState: SaveState = SaveState.IDLE,
    val errorMessage: String = ""
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
        _uiState.value = _uiState.value.copy(showDialog = true, saveState = SaveState.IDLE, errorMessage = "")
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false, saveState = SaveState.IDLE, errorMessage = "")
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

    // Changed to use state management instead of callbacks
    fun saveFloodMarker(location: String, status: String, mapViewModel: MapViewModel) {
        // Update state to saving
        _uiState.value = _uiState.value.copy(saveState = SaveState.SAVING, errorMessage = "")

        viewModelScope.launch {
            try {
                Log.d("MapDebug", "Starting save process in ViewModel")
                // First update local status
                updateFloodStatus(location, status)

                // Get district data
                val district = mapViewModel.getDistrictsByName(location)
                Log.d("MapDebug", "Fetched district: $district")

                // Create flood marker with proper ID
                val reportMarker = FloodMarker(
                    id = FloodMarker.TEMP_ID,
                    floodStatus = status.lowercase(),
                    districtId = district.id,
                    latitude = 4.2105,
                    longitude = 101.9758,
//                    latitude = district.latitude,
//                    longitude = district.longitude,
                    expiryTime = Instant.now().plus(2, ChronoUnit.MINUTES)
                )

                // Let MapViewModel handle the saving to both databases
                mapViewModel.addNewFloodMarker(reportMarker)

                // Update state to success
                _uiState.value = _uiState.value.copy(saveState = SaveState.SUCCESS)

                // After successful save, close the dialog
                delay(200) // Small delay to ensure state is updated
                dismissDialog()
                showSnackbar("Flood status updated successfully!")

            } catch (e: Exception) {
                Log.e("MapDebug", "Error saving flood status", e)
            _uiState.value = _uiState.value.copy(
                saveState = SaveState.ERROR,
                errorMessage = "Error: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    // Reset the save state
    fun resetSaveState() {
        _uiState.value = _uiState.value.copy(saveState = SaveState.IDLE, errorMessage = "")
    }

    // Set error message
    fun setErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }
}