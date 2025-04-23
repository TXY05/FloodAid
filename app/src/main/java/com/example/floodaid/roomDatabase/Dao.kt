package com.example.floodaid.roomDatabase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MapDao {
    @Query("SELECT * FROM State")
    suspend fun getAllStates(): List<State>

    @Query("SELECT * FROM District WHERE stateName = :state")
    suspend fun getDistrictsByState(state: String): List<District>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStates(states: List<State>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDistricts(districts: List<District>)

    @Query("DELETE FROM State")
    suspend fun deleteAllStates()

    @Query("DELETE FROM District")
    suspend fun deleteAllDistricts()
}
