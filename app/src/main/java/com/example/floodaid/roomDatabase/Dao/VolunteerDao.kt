package com.example.floodaid.roomDatabase.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface VolunteerDao {
    // For Volunteer Event
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: VolunteerEvent)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(event: VolunteerEvent)

    @Delete
    suspend fun delete(event: VolunteerEvent)

    @Query("SELECT * FROM event WHERE event_id =:eventId")
    fun getEvent(eventId:String):Flow<VolunteerEvent>

    @Query("SELECT * FROM event WHERE date =:filterDate")
    fun getFilteredEvent(filterDate:String):Flow<VolunteerEvent>

    // For room and firebase sync
    @Query("SELECT * FROM event")
    fun getAllEvents(): Flow<List<VolunteerEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<VolunteerEvent>)

    @Query("DELETE FROM event")
    suspend fun deleteAllEvent()
}

@Dao
interface VolunteerEventHistoryDao {
    // For Volunteer Event History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eventHistory: VolunteerEventHistory)

    @Delete
    suspend fun delete(eventHistory: VolunteerEventHistory)

    // For room and firebase sync
    @Query("""
        SELECT 
            eh.history_id as history_id,
            eh.userId as userId,
            eh.eventId as eventId,
            e.date as date,
            e.startTime as startTime,
            e.endTime as endTime,
            e.description as description,
            e.district as district
        FROM event_history eh
        INNER JOIN event e ON eh.eventId = e.event_id
        WHERE eh.userId = :userId
        ORDER BY e.date DESC
    """)
    fun getEventHistory(userId: String): Flow<List<VolunteerEventHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventHistory(eventHistory: List<VolunteerEventHistory>)

    @Query("DELETE FROM event_history")
    suspend fun deleteAllEventHistory()
}