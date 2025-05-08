package com.example.floodaid.screen.map_UI

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodaid.roomDatabase.entities.*
import com.example.floodaid.roomDatabase.repository.MapRepository
import com.example.floodaid.utils.GeocodingHelper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.temporal.ChronoUnit

class MapViewModel(
    application: Application,
    private val repository: MapRepository
) : AndroidViewModel(application) {

    private val geocodingHelper = GeocodingHelper(getApplication())

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    private val _districtWithBorderFlow = MutableStateFlow<District?>(null)
    val districtWithBorderFlow: StateFlow<District?> = _districtWithBorderFlow.asStateFlow()

    private val _selectedShelter = MutableStateFlow<Shelter?>(null)
    val selectedShelter: StateFlow<Shelter?> = _selectedShelter.asStateFlow()

    private val _selectedMarkerId = MutableStateFlow<Long?>(null)
    val selectedMarkerId: StateFlow<Long?> = _selectedMarkerId.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                Log.d("LOCATION", "New location: ${location.latitude}, ${location.longitude}")
                _currentLocation.value = LatLng(location.latitude, location.longitude)
            } ?: Log.e("LOCATION", "Received null location")
        }
    }


    init {
//      Initial data sync
        viewModelScope.launch {
            try {
                observeFireStoreUpdates()
                cleanupDatabases()
                withRetry(3) { repository.syncAllData() }

            } catch (e: Exception) {
            }
        }

        startPeriodicCleanup()

        if (hasLocationPermission()) {
            startLocationUpdates()
        }
    }

    // Helper function for retrying operations
    private suspend fun <T> withRetry(maxRetries: Int, block: suspend () -> T): T {
        var lastException: Exception? = null
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    delay(1000L * (attempt + 1)) // Exponential backoff
                }
            }
        }
        throw lastException ?: Exception("Unknown error occurred")
    }

    // Add new function to handle both database cleanups
    private suspend fun cleanupDatabases() {
        try {
            // Clean up FireStore first
            repository.cleanupRoomMarkers()
            // Then clean up Room database
            repository.cleanupFireStoreMarkers()

            viewModelScope.launch {
                uiState.value.selectedDistrict?.let {
                    loadDistrictData(it.id)
                }
            }
        } catch (e: Exception) {
            Log.e("MapViewModel", "Error during database cleanup: ${e.message}")
        }
    }

    private fun startPeriodicCleanup() {
        viewModelScope.launch {
            while (true) {
                delay(5 * 60 * 1000) // Clean up every 5 minutes
                cleanupDatabases()
            }
        }
    }

    fun observeFireStoreUpdates() {
        viewModelScope.launch {
            // Observe states updates first
            repository.listenToStatesUpdates().collect { states ->
                try {
                    repository.deleteAllStates()
                    repository.insertAllStates(states)
                    _uiState.update { it.copy(states = states) }
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error updating states: ${e.message}")
                }
            }
        }

        viewModelScope.launch {
            // Observe districts updates after states
            repository.listenToDistrictsUpdates().collect { districts ->
                try {
                    repository.deleteAllDistricts()
                    repository.insertAllDistricts(districts)
                    _uiState.update { it.copy(districts = districts) }
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error updating districts: ${e.message}")
                }
            }
        }

        viewModelScope.launch {
            // Observe shelters updates after districts
            repository.listenToSheltersUpdates().collect { shelters ->
                try {
                    if (shelters.isNotEmpty()) {
                        repository.deleteAllShelter()
                        repository.insertAllShelters(shelters)
                        uiState.value.selectedDistrict?.let { district ->
                            // Filter shelters for the current district
                            val districtShelters = shelters.filter { it.districtId == district.id }
                            _uiState.update { it.copy(currentShelters = districtShelters) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error updating shelters: ${e.message}")
                }
            }
        }

        viewModelScope.launch {
            // Observe flood markers updates after districts
            repository.listenToFloodMarkersUpdates().collect { markers ->
                try {
                    if (markers.isNotEmpty()) {
                        repository.deleteAllMarkers()
                        repository.insertAllMarkers(markers)
                        uiState.value.selectedDistrict?.let { district ->
                            // Filter markers for the current district
                            val districtMarkers = markers.filter { it.districtId == district.id }
                            _uiState.update { it.copy(currentMarkers = districtMarkers) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error updating flood markers: ${e.message}")
                }
            }
        }
    }

    fun addNewFloodMarker(marker: FloodMarker) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.pushFloodMarker(marker)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                // Handle error appropriately
            }
        }
    }

    // For States
    fun loadStates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val states = repository.getAllStates()
                    .distinctBy { it.id } // Double protection
                    .sortedBy { it.name } // Optional: sort alphabetically

                _uiState.update {
                    it.copy(
                        states = states,
                        districts = emptyList(), // Clear districts when states reload
                        selectedState = null,    // Reset selection
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        states = emptyList()
                    )
                }
            }
        }
    }

    fun onStateSelected(state: State) {
        _uiState.update { it.copy(selectedState = state, selectedDistrict = null) }
        loadDistrictsForState(state.id)
    }

    fun clearSelectedState() {
        _uiState.update { it.copy(selectedState = null, districts = emptyList()) }
    }

    // For Districts
    fun loadDistrictsForState(stateId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val districtWithShelters = repository.getDistrictsByState(stateId)
                val districts = districtWithShelters.map { it.district }.distinctBy { it.id } // Added distinct
                _uiState.update {
                    it.copy(
                        districts = districts,
//                        currentShelters = emptyList(), // Clear previous shelters
//                        currentMarkers = emptyList(),   // Clear previous markers
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        districts = emptyList() // Clear on error
                    )
                }
            }
        }
    }

    suspend fun restoreDistrict(districtId: Long) {
        try {
            Log.d("MapDebug", "Fetching district data for restoration")
            val district = repository.getDistrictsByID(districtId)

            // Load the district data first
            onDistrictSelected(district)

            // Then check if we need to fetch border data from Firestore
            if (district.borderCoordinates == null) {
                Log.d("MapDebug", "District has no border coordinates, fetching from Firestore")
                viewModelScope.launch {
                    try {
                        // Try to get border data from Firestore
                        val allDistricts = repository.fetchAllDistricts()
                        val freshDistrict = allDistricts.find { it.id == districtId }

                        if (freshDistrict != null && freshDistrict.borderCoordinates != null) {
                            Log.d("MapDebug", "Found district with border data in Firestore")
                            // Update the district in Room database
                            repository.insertAllDistricts(listOf(freshDistrict))

                            // Update the UI
                            _uiState.update { current ->
                                current.copy(selectedDistrict = freshDistrict)
                            }

                            // Update border flow for direct polygon drawing
                            _districtWithBorderFlow.value = freshDistrict
                        } else {
                            Log.d("MapDebug", "No border data found in Firestore for district $districtId")
                        }
                    } catch (e: Exception) {
                        Log.e("MapDebug", "Error fetching district border data: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MapDebug", "Error restoring district: ${e.message}")
            loadStates() // Fallback to loading states if restoration fails
        }
    }

    fun onDistrictSelected(district: District) {
        _uiState.update { current ->
            current.copy(
                selectedDistrict = district,
                currentShelters = emptyList(),  // Clear previous shelters
                currentMarkers = emptyList()   // Clear previous markers
            )
        }
        loadDistrictData(district.id)
    }

    fun loadDistrictData(districtId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val shelters = repository.getSheltersByDistrict(districtId).first()
                val markers = repository.getActiveMarkersByDistrict(districtId).first()
                _uiState.update {
                    it.copy(
                        currentShelters = shelters,
                        currentMarkers = markers,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentShelters = emptyList(),  // Clear on error
                        currentMarkers = emptyList()    // Clear on error
                    )
                }
            }
        }
    }

    // Shelter
    fun onShelterSelected(shelter: Shelter?) {
        viewModelScope.launch {
            shelter?.let { nonNullShelter ->
                val updatedShelter = if (nonNullShelter.address.isNullOrEmpty()) {
                    val address = geocodingHelper.getAddress(
                        nonNullShelter.latitude,
                        nonNullShelter.longitude
                    )
                    nonNullShelter.copy(address = address)
                } else {
                    nonNullShelter
                }
                _selectedShelter.value = updatedShelter
            }
        }
    }

    fun clearSelectedShelter() {
        viewModelScope.launch {
//            delay(100)
            _selectedShelter.value = null
        }
    }

    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).apply {
            setMinUpdateDistanceMeters(5f) // Minimum change to trigger update
            setWaitForAccurateLocation(true)
        }.build()

        try {
            LocationServices.getFusedLocationProviderClient(getApplication())
                .requestLocationUpdates(
                    locationRequest,
                    object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            result.lastLocation?.let { location ->
                                _currentLocation.value = LatLng(location.latitude, location.longitude)
                            }
                        }
                    },
                    Looper.getMainLooper()
                )
        } catch (e: SecurityException) {
            Log.e("Location", "Permission error", e)
        }
    }

    fun stopLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(getApplication())
            .removeLocationUpdates(locationCallback)
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    getApplication(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun onMarkerSelected(markerId: Long) {
        _selectedMarkerId.value = markerId
    }

    fun clearSelectedMarker() {
        _selectedMarkerId.value = null
    }

    // State operations
    fun getAllStates() {
        viewModelScope.launch {
            repository.getAllStates()
            loadStates() // Optional: Refresh state list after insert
        }
    }

    fun insertState(state: State) {
        viewModelScope.launch {
            repository.insertState(state)
            loadStates() // Optional: Refresh state list after insert
        }
    }

    fun insertAllStates(states: List<State>) {
        viewModelScope.launch {
            repository.insertAllStates(states)
            loadStates()
        }
    }

    fun deleteAllStates() {
        viewModelScope.launch {
            repository.deleteAllStates()
            _uiState.update { it.copy(states = emptyList()) }
        }
    }

    // District operations
    fun insertAllDistricts(districts: List<District>) {
        viewModelScope.launch {
            repository.insertAllDistricts(districts)
            uiState.value.selectedState?.let {
                loadDistrictsForState(it.id)
            }
        }
    }

    suspend fun getDistrictsByName(districtName: String): District {
        return withContext(Dispatchers.IO) { // Run in background
            repository.getDistrictsByName(districtName)
        }
    }

    fun deleteAllDistricts() {
        viewModelScope.launch {
            repository.deleteAllDistricts()
            _uiState.update { it.copy(districts = emptyList()) }
        }
    }

    // Shelter operations
    fun insertAllShelters(shelters: List<Shelter>) {
        viewModelScope.launch {
            repository.insertAllShelters(shelters)
            uiState.value.selectedDistrict?.let {
                loadDistrictData(it.id)
            }
        }
    }

    fun deleteAllShelters() {
        viewModelScope.launch {
            repository.deleteAllShelter()
            _uiState.update { it.copy(currentShelters = emptyList()) }
        }
    }

    fun loadShelterById(shelterId: Long, onResult: (Shelter?) -> Unit) {
        viewModelScope.launch {
            val shelter = repository.getShelterById(shelterId)
            onResult(shelter)
        }
    }

    // FloodMarker operations
    fun insertAllFloodMarkers(markers: List<FloodMarker>) {
        viewModelScope.launch {
            repository.insertAllMarkers(markers)
            uiState.value.selectedDistrict?.let {
                loadDistrictData(it.id)
            }
        }
    }

    fun updateFloodMarker(marker: FloodMarker) {
        viewModelScope.launch {
            repository.updateMarker(marker)
            uiState.value.selectedDistrict?.let {
                loadDistrictData(it.id)
            }
        }
    }

    fun deleteFloodMarker(id: Long) {
        viewModelScope.launch {
            repository.deleteMarker(id)
            uiState.value.selectedDistrict?.let {
                loadDistrictData(it.id)
            }
        }
    }

    fun deleteAllFloodMarkers() {
        viewModelScope.launch {
            repository.deleteAllMarkers()
            _uiState.update { it.copy(currentMarkers = emptyList()) }
        }
    }

    fun loadFloodMarkerById(id: Long, onResult: (FloodMarker?) -> Unit) {
        viewModelScope.launch {
            val marker = repository.getMarkerById(id)
            onResult(marker)
        }
    }
}