package com.example.floodaid.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "event_history")
data class VolunteerEventHistory(
    @ColumnInfo(name = "history_id")
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val eventId: String
)