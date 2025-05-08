package com.example.floodaid.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.floodaid.models.VolunteerEventHistory

@Entity(tableName = "volunteer_profile")
data class VolunteerProfile(
    @ColumnInfo(name = "volunteer_id")
    @PrimaryKey
    val userId: String,
    val phoneNum: String,
    val emgContact: String,
    val emgNum: String
){
    constructor() : this("", "", "", "")
}