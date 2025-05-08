package com.example.floodaid.roomDatabase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.floodaid.roomDatabase.entities.District
import com.example.floodaid.roomDatabase.entities.FloodMarker
import com.example.floodaid.roomDatabase.entities.Shelter
import com.example.floodaid.roomDatabase.entities.State
import com.example.floodaid.roomDatabase.relationships.DistrictWithShelters
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface MapDao {
    // State
    @Query("SELECT * FROM State")
    suspend fun getAllStates(): List<State>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertState(states: State): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStates(states: List<State>): List<Long>

    @Query("DELETE FROM State")
    suspend fun deleteAllStates()

    // District operations
    @Query("SELECT * FROM District")
    suspend fun getAllDistricts(): List<District>

    @Transaction
    @Query("SELECT * FROM District WHERE stateId = :stateId")
    suspend fun getDistrictsByState(stateId: Long): List<DistrictWithShelters>

    @Transaction
    @Query("SELECT * FROM District WHERE id = :districtId")
    suspend fun getDistrictsByID(districtId: Long): District

    @Transaction
    @Query("SELECT * FROM District WHERE name = :districtName")
    suspend fun getDistrictsByName(districtName: String): District

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDistricts(districts: List<District>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDistrict(districts: District)

    @Query("DELETE FROM District")
    suspend fun deleteAllDistricts()

    // Shelter operations
    @Query("SELECT * FROM Shelter")
    suspend fun getAllShelters(): List<Shelter>

    @Transaction
    @Query("SELECT * FROM Shelter WHERE districtId = :districtId")
    fun getSheltersByDistrict(districtId: Long): Flow<List<Shelter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllShelters(shelters: List<Shelter>)

    @Query("DELETE FROM Shelter")
    suspend fun deleteAllShelters()

    @Query("SELECT * FROM Shelter WHERE id = :shelterId")
    suspend fun getShelterById(shelterId: Long): Shelter?

    // FloodMarker operations
    @Query("SELECT * FROM FloodMarker")
    suspend fun getAllMarkers(): List<FloodMarker>

    @Query("SELECT * FROM FloodMarker WHERE id = :id")
    suspend fun getMarkerById(id: Long): FloodMarker?

    @Query("SELECT * FROM FloodMarker WHERE districtId = :districtId")
    fun getMarkersByDistrict(districtId: Long): Flow<List<FloodMarker>>

    @Query("SELECT * FROM FloodMarker WHERE districtId = :districtId AND expiryTime > :currentTime")
    fun getActiveMarkersByDistrict(
        districtId: Long,
        currentTime: Instant = Instant.now()
    ): Flow<List<FloodMarker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarkers(markers: FloodMarker)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMarkers(markers: List<FloodMarker>)

    @Update
    suspend fun updateMarker(marker: FloodMarker)

    @Query("DELETE FROM FloodMarker WHERE id = :id")
    suspend fun deleteMarker(id: Long)

    @Query("DELETE FROM FloodMarker WHERE expiryTime < :currentTime")
    suspend fun cleanupExpiredMarkers(currentTime: Instant = Instant.now())

    @Query("DELETE FROM FloodMarker")
    suspend fun deleteAllMarkers()

//    // Complex relationships
//    @Transaction
//    @Query("SELECT * FROM State WHERE id = :stateId")
//    suspend fun getStateWithDistricts(stateId: Long): StateWithDistricts
//
//    @Transaction
//    @Query("SELECT * FROM District WHERE id = :districtId")
//    suspend fun getDistrictWithMarkers(districtId: Long): DistrictWithMarkers
}