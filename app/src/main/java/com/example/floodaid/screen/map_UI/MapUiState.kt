package com.example.floodaid.screen.map_UI

import com.example.floodaid.roomDatabase.Entities.District
import com.example.floodaid.roomDatabase.Entities.FloodMarker
import com.example.floodaid.roomDatabase.Entities.Shelter
import com.example.floodaid.roomDatabase.Entities.State

// UI State Class
data class MapUiState(
    val states: List<State> = emptyList(),
    val districts: List<District> = emptyList(),
    val selectedState: State? = null,
    val selectedDistrict: District? = null,
    val currentShelters: List<Shelter> = emptyList(),
    val currentMarkers: List<FloodMarker> = emptyList(),
    val isLoading: Boolean = false
)