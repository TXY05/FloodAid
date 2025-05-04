package com.example.floodaid.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "event")
data class VolunteerEvent(
    @ColumnInfo(name = "event_id")
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val district: String,
    val userid: String
){
    constructor() : this(0, "", "", "", "", "", "")
}
