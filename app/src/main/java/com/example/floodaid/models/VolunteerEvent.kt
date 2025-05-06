package com.example.floodaid.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.floodaid.models.VolunteerEventHistory

@Entity(tableName = "event")
data class VolunteerEvent(
    @ColumnInfo(name = "event_id")
    @PrimaryKey()
    val firestoreId: String = "",
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val district: String,
    val userId: String
){
    constructor() : this("", "", "", "", "", "", "")
}