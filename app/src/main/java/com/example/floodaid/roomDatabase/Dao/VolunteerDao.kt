package com.example.floodaid.roomDatabase.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface VolunteerDao {
    @Query("SELECT * FROM event")
    fun getAllEvents(): Flow<List<VolunteerEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<VolunteerEvent>)
}

@Dao
interface VolunteerEventHistoryDao {
    @Query("SELECT * FROM event_history WHERE userId = :userId")
    fun getEventHistory(userId: String): Flow<List<VolunteerEventHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventHistory(event: VolunteerEventHistory)
}