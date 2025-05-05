package com.example.floodaid.screen.map_UI

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodaid.roomDatabase.Database.FloodAidDatabase
import com.example.floodaid.roomDatabase.Entities.*
import com.example.floodaid.roomDatabase.Repository.MapRepository
import com.example.floodaid.utils.DistanceCalculator
import com.example.floodaid.utils.GeocodingHelper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.example.floodaid.repository.FirestoreRepository
import java.time.Instant
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

//class MapViewModel(application: Application) : AndroidViewModel(application) {
class MapViewModel(
    application: Application,
    private val repository: MapRepository
) : AndroidViewModel(application) {

    private val geocodingHelper = GeocodingHelper(getApplication())

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    private val _selectedShelter = MutableStateFlow<Shelter?>(null)
    val selectedShelter: StateFlow<Shelter?> = _selectedShelter.asStateFlow()

    private val _selectedMarkerId = MutableStateFlow<Long?>(null)
    val selectedMarkerId: StateFlow<Long?> = _selectedMarkerId.asStateFlow()

    private val locationClient = LocationServices.getFusedLocationProviderClient(application)
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
                withRetry(3) { repository.syncAllData() }

            } catch (e: Exception) {
            }
        }

        fetchLocation()
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

    // FireStore Operations
    fun syncData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Sync data sequentially with retries
                withRetry(3) { repository.syncAllData() }

                // Refresh current view after sync
                uiState.value.selectedState?.let { state ->
                    loadDistrictsForState(state.id)
                }
                uiState.value.selectedDistrict?.let { district ->
                    loadDistrictData(district.id)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun observeFireStoreUpdates() {
        viewModelScope.launch {
            // Observe states updates first
            repository.listenToStatesUpdates().collect { states ->
                try {
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
                // Optionally update local database
                repository.insertAllMarkers(listOf(marker))
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
                        currentShelters = emptyList(), // Clear previous shelters
                        currentMarkers = emptyList(),   // Clear previous markers
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
                // Handle error
            }
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
                // Consider adding error logging/handling here
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

    private fun fetchLocation() {
        if (hasLocationPermission()) {
            try {
                LocationServices.getFusedLocationProviderClient(getApplication())
                    .lastLocation
                    .addOnSuccessListener { location ->
                        location?.let {
                            _currentLocation.value = LatLng(it.latitude, it.longitude)
                        }
                    }
//                    .addOnFailureListener { e ->
//                        Log.e("Location", "Failed to get location", e)
//                        // Fallback to default location if needed
//                        _currentLocation.value = LatLng(3.1390, 101.6869) // KL coordinates
//                    }
            } catch (e: SecurityException) {
                Log.e("Location", "Permission error", e)
            }
        }
    }

    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).apply {
            setMinUpdateDistanceMeters(10f) // Minimum change to trigger update
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

    fun cleanupExpiredMarkers() {
        viewModelScope.launch {
            repository.cleanupExpiredMarkers()
            uiState.value.selectedDistrict?.let {
                loadDistrictData(it.id)
            }
        }
    }
}