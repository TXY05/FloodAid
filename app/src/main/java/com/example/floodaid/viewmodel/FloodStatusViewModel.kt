package com.example.floodaid.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class LocationStatus(
    val location: String,
    val status: String
)

data class FloodStatusUiState(
    val floodData: List<LocationStatus> = emptyList(),
    val selectedLocation: String? = null,
    val showDialog: Boolean = false
)

class FloodStatusViewModel : ViewModel() {

    private val initialLocations = listOf(
        "Gombak",
        "Hulu Langat",
        "Hulu Selangor",
        "Klang",
        "Kuala Langat",
        "Petaling",
        "Sabak Bernam",
        "Sepang"
    ).map {
        when (it) {
            "Sabak Bernam", "Sepang" -> LocationStatus(it, "Flooded")
            else -> LocationStatus(it, "Safe")
        }
    }

    private val _uiState = MutableStateFlow(
        FloodStatusUiState(floodData = initialLocations)
    )
    val uiState: StateFlow<FloodStatusUiState> = _uiState

    fun selectLocation(location: String) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
    }

    fun clearSelectedLocation() {
        _uiState.value = _uiState.value.copy(selectedLocation = null)
    }

    fun showDialog() {
        _uiState.value = _uiState.value.copy(showDialog = true)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false)
    }

    fun updateFloodStatus(location: String, status: String) {
        val updatedList = _uiState.value.floodData.map {
            if (it.location == location) it.copy(status = status) else it
        }
        _uiState.value = _uiState.value.copy(floodData = updatedList, showDialog = false)
    }
}
