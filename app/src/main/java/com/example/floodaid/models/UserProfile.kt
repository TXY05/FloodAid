package com.example.floodaid.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val uid: String = "",
    val fullName: String = "",
    val userName: String = "",
    val email: String = "",
    val myKadOrPassport: String = "",
    val gender: String = "",
    val birthOfDate: String = "",
    val location: String = "",
    val profilePictureUrl: String = "",
)

@Entity(tableName = "user_location")
data class UserLocation(
    @PrimaryKey
    val uid: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val isSOSActive: Boolean = true
)
