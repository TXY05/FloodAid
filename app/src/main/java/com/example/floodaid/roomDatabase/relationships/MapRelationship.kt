package com.example.floodaid.roomDatabase.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.floodaid.roomDatabase.entities.District
import com.example.floodaid.roomDatabase.entities.Shelter

data class DistrictWithShelters(
    @Embedded val district: District,
    @Relation(
        parentColumn = "id",
        entityColumn = "districtId"
    )
    val shelters: List<Shelter>
)
