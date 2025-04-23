package com.example.floodaid.roomDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

data class Border(val coordinates: List<List<Double>>)

@Entity
data class State(
    @PrimaryKey val stateName: String
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = State::class,
        parentColumns = ["stateName"],
        childColumns = ["stateName"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class District(
    @PrimaryKey val districtName: String,
    val stateName: String,
    val latitude: Double,
    val longitude: Double,
    val borderCoordinates: Border?
//    val borderCoordinates: BorderCoordinates?
//    val borderCoordinates: List<List<Double>>?
//    val borderCoordinates: String? = null
)
