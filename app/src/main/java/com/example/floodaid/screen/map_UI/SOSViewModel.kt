package com.example.floodaid.screen.map_UI

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodaid.models.UserLocation
import com.example.floodaid.roomDatabase.Repository.ProfileRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SOSViewModel(
    application: Application,
    private val profileRepository: ProfileRepository? = null
) : AndroidViewModel(application) {
    private val _isSOSActive = MutableStateFlow(false)
    val isSOSActive: StateFlow<Boolean> = _isSOSActive.asStateFlow()

    private val _needsLocationPermission = MutableStateFlow(false)
    val needsLocationPermission: StateFlow<Boolean> = _needsLocationPermission.asStateFlow()

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private var locationUpdateJob: Job? = null
    private var locationCallback: LocationCallback? = null

    // Flag to determine if we're using external location source
    private var usingExternalLocation = false

    // Reference to MapViewModel for starting its location updates
    private var mapViewModel: MapViewModel? = null

    // Location update interval (in milliseconds)
    private val LOCATION_UPDATE_INTERVAL = TimeUnit.MINUTES.toMillis(1)

    // Set the MapViewModel reference
    fun setMapViewModel(viewModel: MapViewModel) {
        this.mapViewModel = viewModel
    }

    // Call this to use location data from another ViewModel (e.g., MapViewModel)
    fun useExternalLocationSource(locationFlow: StateFlow<LatLng?>) {
        usingExternalLocation = true
        Log.d("SOSLocation", "Using external location source")

        // Cancel our own location updates if they're active
        stopOwnLocationUpdates()

        // Start collecting from external source
        viewModelScope.launch {
            try {
                locationFlow.collect { location ->
                    _currentLocation.value = location

                    // Log that we received location update
                    if (location != null) {
                        Log.d("SOSLocation", "Received external location: ${location.latitude}, ${location.longitude}")
                    }

                    // If SOS is active, save this externally-provided location
                    if (_isSOSActive.value && location != null) {
                        Log.d("SOSLocation", "SOS is active, saving external location...")
                        saveUserLocation(location.latitude, location.longitude)
                    }
                }
            } catch (e: Exception) {
                Log.e("SOSLocation", "Error collecting external location: ${e.message}", e)
                // If external location fails, start our own updates as fallback
                if (_isSOSActive.value && !usingExternalLocation) {
                    startLocationUpdates()
                }
            }
        }
    }

    private fun createLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    _currentLocation.value = latLng

                    // If SOS is active, save location
                    if (_isSOSActive.value) {
                        saveUserLocation(location.latitude, location.longitude)
                    }
                }
            }
        }
    }

    private fun saveUserLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            if (profileRepository == null) {
                Log.e("SOSLocation", "ERROR: ProfileRepository is null, can't save location")
                return@launch
            }

            currentUser?.uid?.let { uid ->
                val userLocation = UserLocation(
                    uid = uid,
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = System.currentTimeMillis(),
                    isSOSActive = true
                )

                try {
                    // Save to repository if available
                    profileRepository.updateUserLocation(userLocation)
                    Log.d("SOSLocation", "Location successfully updated: $latitude, $longitude")
                } catch (e: Exception) {
                    Log.e("SOSLocation", "Failed to update location: ${e.message}", e)
                }
            } ?: Log.e("SOSLocation", "ERROR: Current user is null, can't save location")
        }
    }

    fun checkLocationPermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            _needsLocationPermission.value = true
        }

        return hasPermission
    }

    fun clearPermissionRequest() {
        _needsLocationPermission.value = false
    }

    fun setSOSState(isActive: Boolean) {
        if (isActive) {
            activateSOS()
        } else {
            deactivateSOS()
        }
    }

    fun activateSOS() {
        Log.d("SOSLocation", "Activating SOS...")

        // Set active state
        _isSOSActive.value = true

        try {
            // Ensure the MapViewModel is starting location updates if we're using it
            if (usingExternalLocation) {
                Log.d("SOSLocation", "Using external location source for SOS")
                mapViewModel?.startLocationUpdates()

                // Check if we have a current location and save it immediately
                _currentLocation.value?.let { location ->
                    Log.d("SOSLocation", "Initial external location available, saving immediately")
                    saveUserLocation(location.latitude, location.longitude)
                }
            } else {
                // If we're not using an external location source, start our own updates
                Log.d("SOSLocation", "Using own location provider for SOS")
                startLocationUpdates()
            }

            // Always start periodic updates to ensure regular database updates
            startPeriodicLocationUpdates()

            Log.d("SOSLocation", "SOS activated successfully")
        } catch (e: Exception) {
            Log.e("SOSLocation", "Error activating SOS: ${e.message}", e)
        }
    }

    private fun deactivateSOS() {
        // Set inactive state
        _isSOSActive.value = false

        // If we're not using external location, stop our updates
        if (!usingExternalLocation) {
            stopOwnLocationUpdates()
        }

        // Always stop periodic updates
        locationUpdateJob?.cancel()
        locationUpdateJob = null

        // Clear location
        viewModelScope.launch {
            currentUser?.uid?.let { uid ->
                profileRepository?.clearSOSLocation(uid)
            }
        }
    }

    private fun startPeriodicLocationUpdates() {
        // Cancel any existing job
        locationUpdateJob?.cancel()

        Log.d("SOSLocation", "Starting periodic location updates every ${LOCATION_UPDATE_INTERVAL/1000} seconds")

        // Start new periodic update job
        locationUpdateJob = viewModelScope.launch {
            try {
                while (_isSOSActive.value && isActive) {
                    delay(LOCATION_UPDATE_INTERVAL)

                    // Update location periodically
                    _currentLocation.value?.let { location ->
                        Log.d("SOSLocation", "Periodic update - Location: ${location.latitude}, ${location.longitude}")
                        saveUserLocation(location.latitude, location.longitude)
                    } ?: Log.w("SOSLocation", "No location available for periodic update")
                }
            } catch (e: Exception) {
                Log.e("SOSLocation", "Error in periodic location updates: ${e.message}", e)
                // Try to restart if SOS is still active
                if (_isSOSActive.value) {
                    delay(5000) // Wait 5 seconds before attempting restart
                    startPeriodicLocationUpdates()
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_INTERVAL / 2  // Update slightly more frequently than our save interval
            ).apply {
                setWaitForAccurateLocation(true)
            }.build()

            try {
                // Create a new callback if needed
                if (locationCallback == null) {
                    locationCallback = createLocationCallback()
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback!!,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                Log.e("SOSLocation", "Location permission error", e)
            }
        }
    }

    private fun stopOwnLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    fun stopLocationUpdates() {
        stopOwnLocationUpdates()
        locationUpdateJob?.cancel()
        locationUpdateJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
