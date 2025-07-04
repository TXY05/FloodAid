package com.example.floodaid.roomDatabase.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant


data class Border(val coordinates: List<List<Double>>)
data class ImageURL(val url: List<String>)

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
    val imageUrlList: ImageURL? = null
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
    @PrimaryKey
    val id: String,
    val floodStatus: String, // e.g., "Low", "Medium", "High", "Critical"
    val districtId: Long,
    val latitude: Double,
    val longitude: Double,
    val expiryTime: Instant, // When this marker should expire
    val createdAt: Instant = Instant.now(), // When this marker was created
) {
    companion object {
        const val TEMP_ID = "Temp"
    }
}