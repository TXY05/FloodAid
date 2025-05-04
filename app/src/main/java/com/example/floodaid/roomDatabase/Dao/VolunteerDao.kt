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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: VolunteerEvent)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(event: VolunteerEvent)

    @Delete
    suspend fun delete(event: VolunteerEvent)

    @Query("SELECT * FROM event")
    fun getAllEvents(): Flow<List<VolunteerEvent>>

    @Query("SELECT * FROM event WHERE event_id =:eventId")
    fun getEvent(eventId:Int):Flow<VolunteerEvent>

    @Query("SELECT * FROM event WHERE date =:filterDate")
    fun getFilteredEvent(filterDate:String):Flow<VolunteerEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<VolunteerEvent>)
}

@Dao
interface VolunteerEventHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventHistory(event: VolunteerEventHistory)

    @Delete
    suspend fun delete(event: VolunteerEventHistory)

    @Query("SELECT * FROM event_history WHERE userId = :userId")
    fun getEventHistory(userId: String): Flow<List<VolunteerEventHistory>>
}