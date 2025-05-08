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
import java.time.temporal.ChronoUnit

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

    // FireStore Operations
//    fun syncData() {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//            try {
//                // Sync data sequentially with retries
//                withRetry(3) { repository.syncAllData() }
//
//                // Refresh current view after sync
//                uiState.value.selectedState?.let { state ->
//                    loadDistrictsForState(state.id)
//                }
//                uiState.value.selectedDistrict?.let { district ->
//                    loadDistrictData(district.id)
//                }
//            } catch (e: Exception) {
//                _uiState.update { it.copy(isLoading = false) }
//            } finally {
//                _uiState.update { it.copy(isLoading = false) }
//            }
//        }
//    }

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

//    private fun fetchLocation() {
//        if (hasLocationPermission()) {
//            try {
//                LocationServices.getFusedLocationProviderClient(getApplication())
//                    .lastLocation
//                    .addOnSuccessListener { location ->
//                        location?.let {
//                            _currentLocation.value = LatLng(it.latitude, it.longitude)
//                        }
//                    }
//            } catch (e: SecurityException) {
//                Log.e("Location", "Permission error", e)
//            }
//        }
//    }

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

    fun createSelangorFloodMarkers() {
        viewModelScope.launch {
            val markers = mutableListOf<FloodMarker>()

            // Gombak (ID: 1)
            markers.addAll(listOf(
                FloodMarker(
                    id = "gombak_1",
                    floodStatus = "flood",
                    districtId = 1,
                    latitude = 3.2550,
                    longitude = 101.5800,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "gombak_2",
                    floodStatus = "safe",
                    districtId = 1,
                    latitude = 3.2333,
                    longitude = 101.6833,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "gombak_3",
                    floodStatus = "flood",
                    districtId = 1,
                    latitude = 3.3000,
                    longitude = 101.5667,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                )
            ))

            // Hulu Langat (ID: 2)
            markers.addAll(listOf(
                FloodMarker(
                    id = "hulu_langat_1",
                    floodStatus = "flood",
                    districtId = 2,
                    latitude = 3.1400,
                    longitude = 101.8500,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "hulu_langat_2",
                    floodStatus = "safe",
                    districtId = 2,
                    latitude = 3.0833,
                    longitude = 101.7833,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "hulu_langat_3",
                    floodStatus = "flood",
                    districtId = 2,
                    latitude = 3.1500,
                    longitude = 101.8167,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                )
            ))

            // Hulu Selangor (ID: 3)
            markers.addAll(listOf(
                FloodMarker(
                    id = "hulu_selangor_1",
                    floodStatus = "safe",
                    districtId = 3,
                    latitude = 3.5058,
                    longitude = 101.6349,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "hulu_selangor_2",
                    floodStatus = "flood",
                    districtId = 3,
                    latitude = 3.6333,
                    longitude = 101.6167,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "hulu_selangor_3",
                    floodStatus = "safe",
                    districtId = 3,
                    latitude = 3.5500,
                    longitude = 101.6333,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                )
            ))

            // Klang (ID: 4)
            markers.addAll(listOf(
                FloodMarker(
                    id = "klang_1",
                    floodStatus = "flood",
                    districtId = 4,
                    latitude = 3.1500,
                    longitude = 101.4000,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "klang_2",
                    floodStatus = "safe",
                    districtId = 4,
                    latitude = 3.0833,
                    longitude = 101.4333,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "klang_3",
                    floodStatus = "flood",
                    districtId = 4,
                    latitude = 3.0167,
                    longitude = 101.4500,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                )
            ))

            // Kuala Langat (ID: 5)
            markers.addAll(listOf(
                FloodMarker(
                    id = "kuala_langat_1",
                    floodStatus = "safe",
                    districtId = 5,
                    latitude = 2.9000,
                    longitude = 101.4500,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "kuala_langat_2",
                    floodStatus = "flood",
                    districtId = 5,
                    latitude = 2.8333,
                    longitude = 101.4833,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "kuala_langat_3",
                    floodStatus = "safe",
                    districtId = 5,
                    latitude = 2.7500,
                    longitude = 101.4500,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                )
            ))

            // Kuala Selangor (ID: 6)
            markers.addAll(listOf(
                FloodMarker(
                    id = "kuala_selangor_1",
                    floodStatus = "flood",
                    districtId = 6,
                    latitude = 3.3667,
                    longitude = 101.2500,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "kuala_selangor_2",
                    floodStatus = "safe",
                    districtId = 6,
                    latitude = 3.3000,
                    longitude = 101.2333,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "kuala_selangor_3",
                    floodStatus = "flood",
                    districtId = 6,
                    latitude = 3.2500,
                    longitude = 101.1500,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                )
            ))

            // Petaling (ID: 7)
            markers.addAll(listOf(
                FloodMarker(
                    id = "petaling_1",
                    floodStatus = "safe",
                    districtId = 7,
                    latitude = 3.1833,
                    longitude = 101.6000,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "petaling_2",
                    floodStatus = "flood",
                    districtId = 7,
                    latitude = 3.1333,
                    longitude = 101.6167,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "petaling_3",
                    floodStatus = "safe",
                    districtId = 7,
                    latitude = 3.0333,
                    longitude = 101.6500,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                )
            ))

            // Sabak Bernam (ID: 8)
            markers.addAll(listOf(
                FloodMarker(
                    id = "sabak_bernam_1",
                    floodStatus = "flood",
                    districtId = 8,
                    latitude = 3.7333,
                    longitude = 101.0333,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "sabak_bernam_2",
                    floodStatus = "safe",
                    districtId = 8,
                    latitude = 3.6667,
                    longitude = 101.0833,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "sabak_bernam_3",
                    floodStatus = "flood",
                    districtId = 8,
                    latitude = 3.5833,
                    longitude = 101.1667,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                )
            ))

            // Sepang (ID: 9)
            markers.addAll(listOf(
                FloodMarker(
                    id = "sepang_1",
                    floodStatus = "safe",
                    districtId = 9,
                    latitude = 2.9000,
                    longitude = 101.7667,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "sepang_2",
                    floodStatus = "flood",
                    districtId = 9,
                    latitude = 2.8167,
                    longitude = 101.7500,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                ),
                FloodMarker(
                    id = "sepang_3",
                    floodStatus = "safe",
                    districtId = 9,
                    latitude = 2.6833,
                    longitude = 101.7000,
                    expiryTime = Instant.now().plus(4, ChronoUnit.DAYS)
                )
            ))

            // Insert all markers
            markers.forEach { marker ->
                try {
                    // Push to Firestore first
                    repository.pushFloodMarker(marker)
                    delay(100)
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error pushing marker ${marker.id}: ${e.message}")
                }
            }
        }
    }

    fun createAndPushSampleShelterMarkers() {
        viewModelScope.launch {
            val sampleShelters = listOf(
                // Gombak (districtId = 1)
                Shelter(
                    id = 1001L,
                    helpCenterName = "Gombak Relief Center 1",
                    descriptions = "Temporary shelter for flood victims in Gombak.",
                    districtId = 1,
                    latitude = 3.2500,
                    longitude = 101.6500,
                    imageUrlList = ImageURL(url = listOf("https://www.google.com.my/maps/place/Dewan+Beringin+Taman+Seri+Gombak/@3.2396447,101.6992416,3a,75y,90t/data=!3m8!1e2!3m6!1sCIHM0ogKEICAgICWlc6sJg!2e10!3e12!6shttps:%2F%2Flh3.googleusercontent.com%2Fgps-cs-s%2FAC9h4noA7ec5pZPOIiDRzKGtiSEPdDn6fH0aOB-aEsu0BJuCYCN1zEOkt8b_pErWCbVvA7SB3od6LPwAGGTLcIDcwHolfco8OA8FtG-3PwkdpCeomdFz3swQcXUNrYdgT2Jpr3Vooh5f%3Dw114-h86-k-no!7i4608!8i3456!4m7!3m6!1s0x31cc47645b719aa7:0xeb5c75d643471d9e!8m2!3d3.2396447!4d101.6992416!10e5!16s%2Fg%2F1tdxhc8k?entry=ttu&g_ep=EgoyMDI1MDUwMy4wIKXMDSoASAFQAw%3D%3D#", "https://www.google.com.my/maps/place/Dewan+Beringin+Taman+Seri+Gombak/@3.2396447,101.6992416,3a,75y,90t/data=!3m8!1e2!3m6!1sCIHM0ogKEICAgICWlc6sJg!2e10!3e12!6shttps:%2F%2Flh3.googleusercontent.com%2Fgps-cs-s%2FAC9h4noA7ec5pZPOIiDRzKGtiSEPdDn6fH0aOB-aEsu0BJuCYCN1zEOkt8b_pErWCbVvA7SB3od6LPwAGGTLcIDcwHolfco8OA8FtG-3PwkdpCeomdFz3swQcXUNrYdgT2Jpr3Vooh5f%3Dw114-h86-k-no!7i4608!8i3456!4m7!3m6!1s0x31cc47645b719aa7:0xeb5c75d643471d9e!8m2!3d3.2396447!4d101.6992416!10e5!16s%2Fg%2F1tdxhc8k?entry=ttu&g_ep=EgoyMDI1MDUwMy4wIKXMDSoASAFQAw%3D%3D#"))
                ),
                Shelter(
                    id = 1002L,
                    helpCenterName = "Gombak Relief Center 2",
                    descriptions = "Provides food and medical aid in Gombak.",
                    districtId = 1,
                    latitude = 3.2600,
                    longitude = 101.6700
                ),
                // Klang (districtId = 4)
                Shelter(
                    id = 2001L,
                    helpCenterName = "Klang Relief Center 1",
                    descriptions = "Main evacuation center for Klang area.",
                    districtId = 4,
                    latitude = 3.0500,
                    longitude = 101.4500
                ),
                Shelter(
                    id = 2002L,
                    helpCenterName = "Klang Relief Center 2",
                    descriptions = "Shelter with basic amenities in Klang.",
                    districtId = 4,
                    latitude = 3.0600,
                    longitude = 101.4600
                ),
                // Sepang (districtId = 9)
                Shelter(
                    id = 3001L,
                    helpCenterName = "Sepang Relief Center 1",
                    descriptions = "Flood relief and support in Sepang.",
                    districtId = 9,
                    latitude = 2.8000,
                    longitude = 101.7000
                ),
                Shelter(
                    id = 3002L,
                    helpCenterName = "Sepang Relief Center 2",
                    descriptions = "Temporary accommodation for Sepang residents.",
                    districtId = 9,
                    latitude = 2.8100,
                    longitude = 101.7100
                )
            )
            // Push to Firestore
            repository.pushShelters(sampleShelters)
            // Insert into Room database
            insertAllShelters(sampleShelters)
        }
    }
}