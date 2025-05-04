package com.example.floodaid.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "event_history")
data class VolunteerEventHistory(
    @ColumnInfo(name = "history_id")
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val eventId: String
){
    constructor() : this(0, "", "")
}