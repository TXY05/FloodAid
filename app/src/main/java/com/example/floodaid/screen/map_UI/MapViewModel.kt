package com.example.floodaid.screen.map_UI

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodaid.roomDatabase.Database.FloodAidDatabase
import com.example.floodaid.roomDatabase.Entities.District
import com.example.floodaid.roomDatabase.Entities.State
import com.example.floodaid.roomDatabase.Repository.MapRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MapRepository(FloodAidDatabase.getInstance(application).MapDao())

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    private val locationClient = LocationServices.getFusedLocationProviderClient(application)

    init {
        fetchLocation()
    }

    fun loadStates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val states = repository.getAllStates()
                _uiState.update { it.copy(states = states, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                // Handle error
            }
        }
    }

    fun onStateSelected(state: State) {
        _uiState.update { it.copy(selectedState = state, selectedDistrict = null) }
        loadDistrictsForState(state.id)
    }

//    fun clearSelectedState() {
//        _uiState.update { it.copy(selectedState = null, districts = emptyList()) }
//    }

    fun loadDistrictsForState(stateId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val districtWithShelters = repository.getDistrictsByState(stateId)
                val districts = districtWithShelters.map { it.district }
                _uiState.update {
                    it.copy(
                        districts = districts,
//                        currentShelters = emptyList(),
//                        currentMarkers = emptyList(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onDistrictSelected(district: District) {
        _uiState.update { it.copy(selectedDistrict = district) }
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
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun fetchLocation() {
        if (hasLocationPermission()) {
            try {
                locationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        _currentLocation.value = LatLng(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Handle permission error
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        val context = getApplication<Application>().applicationContext
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}