package com.example.floodaid.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodaid.roomDatabase.MapDatabase
import com.example.floodaid.roomDatabase.District
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.example.floodaid.roomDatabase.State as StateEntity
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MapDatabase.getInstance(application)
    private val repository = MapRepository(database.MapDao())

    var states = mutableStateOf(listOf<StateEntity>())
    var districts = mutableStateOf(listOf<District>())
    var selectedState = mutableStateOf<String?>(null)
    var selectedDistrict = mutableStateOf<District?>(null)

    var mapView: MapView? = null
    var lastCameraPosition: CameraPosition? = null

    init {
        loadStates() // Make sure this is called
    }

    fun loadStates() {
        viewModelScope.launch {
            states.value = repository.getAllStates()
        }
    }

    fun loadDistrictsForState(state: String) {
        viewModelScope.launch {
            districts.value = repository.getDistrictsByState(state)
        }
    }

    fun onStateSelected(state: String) {
        selectedState.value = state
        loadDistrictsForState(state)
    }

    fun onDistrictSelected(district: District) {
        selectedDistrict.value = district
    }

    fun insertAllStates(states: List<StateEntity>) {
        viewModelScope.launch {
            repository.insertAllStates(states)
        }
    }

    fun insertAllCities(districts: List<District>) {
        viewModelScope.launch {
            repository.insertAllDistricts(districts)
        }
    }

    fun deleteAllStates() {
        viewModelScope.launch {
            repository.deleteAllStates()
        }
    }

    fun deleteAllDistricts() {
        viewModelScope.launch {
            repository.deleteAllDistricts()
        }
    }
}