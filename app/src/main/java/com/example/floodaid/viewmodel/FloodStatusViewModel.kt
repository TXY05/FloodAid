package com.example.floodaid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodaid.screen.floodstatus.FloodStatusRepository
import com.example.floodaid.screen.floodstatus.LocationStatusEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class LocationStatus(
    val location: String,
    val status: String
)

data class FloodStatusUiState(
    val floodData: List<LocationStatus> = emptyList(),
    val selectedLocation: String? = null,
    val showDialog: Boolean = false
)

class FloodStatusViewModel(private val repository: FloodStatusRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FloodStatusUiState())
    val uiState: StateFlow<FloodStatusUiState> = _uiState

    init {
        fetchLocations()
    }

    private fun fetchLocations() {
        viewModelScope.launch {
            repository.getAllLocations().collectLatest { locations ->
                _uiState.value = _uiState.value.copy(
                    floodData = locations.map { LocationStatus(it.location, it.status) }
                )
            }
        }
    }

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
        viewModelScope.launch {
            repository.updateStatus(location, status, date = "dd/MM/yyyy") // Replace with actual date
        }
    }
}