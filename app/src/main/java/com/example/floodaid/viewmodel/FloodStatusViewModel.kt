package com.example.floodaid.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.floodaid.roomDatabase.entities.FloodMarker
import com.example.floodaid.roomDatabase.Repository.FirestoreRepository
import com.example.floodaid.screen.floodstatus.FloodHistoryEntity
import com.example.floodaid.screen.floodstatus.FloodStatusDao
import com.example.floodaid.screen.floodstatus.FloodStatusRepository
import com.example.floodaid.screen.map_UI.MapViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.random.Random

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
    val errorMessage: String = "",
    val validationMessage: String = ""
)

class FloodStatusViewModel(
    private val repository: FloodStatusRepository,
    private val dao: FloodStatusDao,
    private val firestoreRepository: FirestoreRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Add map to track last update times for each location
    private val lastUpdateTimes = mutableMapOf<String, Long>()

    // Function to get last update time for a location
    fun getLastUpdateTime(location: String): Long {
        return lastUpdateTimes[location] ?: 0L
    }

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
        _uiState.value = _uiState.value.copy(showDialog = true, saveState = SaveState.IDLE, errorMessage = "", validationMessage = "")
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            showDialog = false,
            saveState = SaveState.IDLE,
            errorMessage = "",
            validationMessage = ""
        )
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
        val currentTime = System.currentTimeMillis()
        val lastUpdateTime = lastUpdateTimes[location] ?: 0L
        val timeSinceLastUpdate = currentTime - lastUpdateTime
        val twoMinutesInMillis = 2 * 60 * 1000 // 2 minutes in milliseconds

        if (timeSinceLastUpdate < twoMinutesInMillis) {
            val remainingSeconds = (twoMinutesInMillis - timeSinceLastUpdate) / 1000
            _uiState.value = _uiState.value.copy(
                validationMessage = "Please wait ${remainingSeconds} seconds before updating status again",
                saveState = SaveState.ERROR
            )
            return
        }

        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            try {
                repository.updateFloodStatus(location, status, currentDate, currentTimeStr)
                lastUpdateTimes[location] = currentTime
                _uiState.value = _uiState.value.copy(
                    saveState = SaveState.SUCCESS,
                    validationMessage = ""
                )
                showSnackbar("Status updated successfully!")
                // Close dialog after successful update
                delay(500) // Small delay to ensure snackbar is visible
                dismissDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    saveState = SaveState.ERROR,
                    validationMessage = "Failed to update status: ${e.message}"
                )
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

    // Function to generate random coordinates based on district ID
    private fun generateRandomCoordinates(districtId: Int): Pair<Double, Double> {
        // Define coordinate ranges for each district
        // These ranges should be adjusted based on your specific district boundaries
        return when (districtId) {
            1 -> { // For district ID 1
                val lat = Random.nextDouble(3.236296, 3.328371) // Example range for latitude
                val lon = Random.nextDouble(101.543151, 101.697791) // Example range for longitude
                Pair(lat, lon)

            }
            2 -> { // For district ID 2
                val lat = Random.nextDouble(2.968620, 3.078295)
                val lon = Random.nextDouble(101.766041, 101.823200)
                Pair(lat, lon)
            }
            3 -> { // For district ID 3
                val lat = Random.nextDouble(3.391313, 3.595826)
                val lon = Random.nextDouble(101.505285, 101.636378)
                Pair(lat, lon)
            }
            4 -> { // For district ID 4
                val lat = Random.nextDouble(2.994287, 3.108926)
                val lon = Random.nextDouble(101.359604, 101.537993)
                Pair(lat, lon)
            }
            5 -> { // For district ID 5
                val lat = Random.nextDouble(2.733001, 2.926007)
                val lon = Random.nextDouble(101.420031, 101.670565)
                Pair(lat, lon)
            }
            6 -> { // For district ID 6
                val lat = Random.nextDouble(3.376203, 3.511508)
                val lon = Random.nextDouble(101.173302, 101.379076)
                Pair(lat, lon)
            }
            7 -> { // For district ID 7
                val lat = Random.nextDouble(3.048491, 3.162642)
                val lon = Random.nextDouble(101.514357, 101.603362)
                Pair(lat, lon)
            }
            8 -> { // For district ID 8
                val lat = Random.nextDouble(3.645089, 3.776877)
                val lon = Random.nextDouble(101.016436, 101.161125)
                Pair(lat, lon)
            }
            9 -> { // For district ID 9
                val lat = Random.nextDouble(2.664606, 2.782942)
                val lon = Random.nextDouble(101.681773, 101.725471)
                Pair(lat, lon)
            }
            // Add more cases for other district IDs
            else -> { // Default case if district ID is not recognized
                val lat = Random.nextDouble(3.0, 3.5) // Wider default range
                val lon = Random.nextDouble(101.5, 102.0)
                Pair(lat, lon)
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

                // Generate random coordinates based on district ID
                val (randomLat, randomLon) = generateRandomCoordinates(district.id.toInt())

                // Create flood marker with proper ID
                val reportMarker = FloodMarker(
                    id = FloodMarker.TEMP_ID,
                    floodStatus = if (status.lowercase() == "flood") "flooded" else "safe",
                    districtId = district.id,
                    latitude = randomLat,
                    longitude = randomLon,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
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