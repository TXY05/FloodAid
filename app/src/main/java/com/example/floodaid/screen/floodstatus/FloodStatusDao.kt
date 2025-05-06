package com.example.floodaid.screen.floodstatus

import androidx.room.*
import com.example.floodaid.screen.floodstatus.FloodHistoryEntity
import com.example.floodaid.screen.floodstatus.LocationStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FloodStatusDao {

    // For LocationStatus
    @Query("SELECT * FROM location_status")
    fun getAllLocations(): Flow<List<LocationStatusEntity>>

    @Query("SELECT * FROM location_status")
    suspend fun getAllLocationsOnce(): List<LocationStatusEntity>

    @Update
    suspend fun updateLocationStatus(location: LocationStatusEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialLocations(locations: List<LocationStatusEntity>)

//    @Query("UPDATE location_status SET status = ''")
//    suspend fun clearAllStatuses()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationStatus(location: LocationStatusEntity)

    @Query("SELECT * FROM location_status")
    fun getAllLocationStatuses(): Flow<List<LocationStatusEntity>>

    @Query("SELECT status FROM location_status WHERE location = :location LIMIT 1")
    suspend fun getFloodStatus(location: String): String

    // For FloodHistory
    @Query("SELECT * FROM flood_history WHERE location = :location ORDER BY id DESC LIMIT 7")
    fun getHistoryForLocation(location: String): Flow<List<FloodHistoryEntity>>

    @Insert
    suspend fun insertFloodHistory(history: FloodHistoryEntity)

//    @Query("DELETE FROM flood_history")
//    suspend fun clearAllHistories()

    @Query("SELECT * FROM flood_history")
    fun getAllFloodHistory(): Flow<List<FloodHistoryEntity>>


}