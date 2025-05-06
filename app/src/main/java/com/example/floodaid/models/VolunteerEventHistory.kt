package com.example.floodaid.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.floodaid.models.VolunteerEvent

@Entity(tableName = "event_history")
data class VolunteerEventHistory(
    @ColumnInfo(name = "history_id")
    @PrimaryKey()
    val firestoreId: String = "",
    val userId: String,
    val eventId: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val district: String
){
//    constructor() : this("", "", "")
}