package com.example.floodaid.roomDatabase.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.Instant


data class Border(val coordinates: List<List<Double>>)

//         Entity
@Entity(tableName = "State")
data class State(
    @PrimaryKey
    val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "District",
    foreignKeys = [
        ForeignKey(
            entity = State::class,
            parentColumns = ["id"],
            childColumns = ["stateId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class District(
    @PrimaryKey
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val borderCoordinates: Border?,
    @ColumnInfo(index = true) val stateId: Long // Foreign key reference to State
)

@Entity(
    tableName = "Shelter",
    foreignKeys = [
        ForeignKey(
            entity = District::class,
            parentColumns = ["id"],
            childColumns = ["districtId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["districtId"])]
)
data class Shelter(
    @PrimaryKey
    val id: Long = 0,
    val helpCenterName: String,
    val descriptions: String,
    val latitude: Double,
    val longitude: Double,
    val districtId: Long, // Foreign key reference to District
    val address: String? = null,
//    val distance: Float? = null
)

@Entity(
    tableName = "FloodMarker",
    foreignKeys = [
        ForeignKey(
            entity = District::class,
            parentColumns = ["id"],
            childColumns = ["districtId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("districtId"), Index("expiryTime")]
)
data class FloodMarker(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val floodStatus: String, // e.g., "Low", "Medium", "High", "Critical"
    val districtId: Long,
    val latitude: Double,
    val longitude: Double,
    val expiryTime: Instant, // When this marker should expire
    val createdAt: Instant = Instant.now(), // When this marker was created
    val reporterId: String? = null // Who reported this (could be user ID or device ID)
) {
    fun isExpired(): Boolean {
        return Instant.now().isAfter(expiryTime)
    }

    fun remainingTime(): Duration {
        return Duration.between(Instant.now(), expiryTime)
    }

    fun isAboutToExpire(threshold: Duration = Duration.ofHours(1)): Boolean {
        return remainingTime() <= threshold && !isExpired()
    }
}