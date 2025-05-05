package com.example.floodaid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.floodaid.repository.FirestoreRepository
import com.example.floodaid.screen.floodstatus.FloodHistoryEntity
import com.example.floodaid.screen.floodstatus.FloodStatusDao
import com.example.floodaid.screen.floodstatus.FloodStatusRepository
import com.example.floodaid.screen.floodstatus.LocationStatusEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class LocationStatus(
    val location: String,
    val status: String
)

data class FloodStatusUiState(
    val floodData: List<LocationStatus> = emptyList(),
    val selectedLocation: String? = null,
    val showDialog: Boolean = false
)

class FloodStatusViewModel(
    private val repository: FloodStatusRepository,
    private val dao: FloodStatusDao,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    val locations = repository.getAllLocations().asLiveData() // Collecting the Flow as LiveData
    private val _uiState = MutableStateFlow(FloodStatusUiState())
    val uiState: StateFlow<FloodStatusUiState> = _uiState

    private val _historyState = MutableStateFlow<List<FloodHistoryEntity>>(emptyList())
    val historyState: StateFlow<List<FloodHistoryEntity>> = _historyState

    init {
        observeFirestoreStatus()
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
            val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            firestoreRepository.updateFloodStatus(location, status, date)
            repository.insertOrUpdateLocation(location, status)
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

    fun clearAllData() {
        // Caution: Firestore does not support bulk deletes client-side easily.
        // So you may either skip this or implement Firestore recursive delete via admin scripts.
        viewModelScope.launch {
            repository.clearAllData()
        }
    }


}