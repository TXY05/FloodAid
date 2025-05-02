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
import com.example.floodaid.roomDatabase.Entities.District
import com.example.floodaid.roomDatabase.Entities.FloodMarker
import com.example.floodaid.roomDatabase.Entities.Shelter
import com.example.floodaid.roomDatabase.Entities.State
import com.example.floodaid.roomDatabase.Repository.MapRepository
import com.example.floodaid.utils.DistanceCalculator
import com.example.floodaid.utils.GeocodingHelper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MapRepository(FloodAidDatabase.getInstance(application).MapDao())
    private val geocodingHelper = GeocodingHelper(getApplication())

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    private val _selectedShelter = MutableStateFlow<Shelter?>(null)
    val selectedShelter: StateFlow<Shelter?> = _selectedShelter.asStateFlow()

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
        fetchLocation()
        if (hasLocationPermission()) {
            startLocationUpdates()  // Begin updates immediately if permission exists
        }
//        if (BuildConfig.DEBUG) {
//            _currentLocation.value = LatLng(3.1390, 101.6869) // KL coordinates for debugging
//        }
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

    // Calculate distance (example)
    fun getDistanceToShelter(userLocation: LatLng, shelter: Shelter): Float {
        return DistanceCalculator.calculateDistance(
            userLocation,
            LatLng(shelter.latitude, shelter.longitude)
        )
    }

    fun clearSelectedShelter() {
        viewModelScope.launch {
            delay(100)
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
                    .addOnFailureListener { e ->
                        Log.e("Location", "Failed to get location", e)
                        // Fallback to default location if needed
                        _currentLocation.value = LatLng(3.1390, 101.6869) // KL coordinates
                    }
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

    fun isGpsEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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