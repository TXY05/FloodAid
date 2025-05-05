package com.example.floodaid.screen.floodstatus

import androidx.room.*

@Entity(tableName = "location_status")
data class LocationStatusEntity(
    @PrimaryKey val location: String,
    val status: String
)

@Entity(tableName = "flood_history")
data class FloodHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val location: String,
    val status: String,
    val date: String,    // e.g., "05/05/2025"
    val time: String     // e.g., "14:30"
)