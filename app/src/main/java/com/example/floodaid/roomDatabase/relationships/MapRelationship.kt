package com.example.floodaid.roomDatabase.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.floodaid.roomDatabase.entities.District
import com.example.floodaid.roomDatabase.entities.FloodMarker
import com.example.floodaid.roomDatabase.entities.Shelter
import com.example.floodaid.roomDatabase.entities.State

// Might Delete
data class StateWithDistricts(
    @Embedded val state: State,
    @Relation(
        parentColumn = "id",
        entityColumn = "stateId"
    )
    val districts: List<District>
)

data class DistrictWithMarkers(
    @Embedded val district: District,
    @Relation(
        parentColumn = "id",
        entityColumn = "districtId"
    )
    val markers: List<FloodMarker>
)

data class DistrictWithShelters(
    @Embedded val district: District,
    @Relation(
        parentColumn = "id",
        entityColumn = "districtId"
    )
    val shelters: List<Shelter>
)
