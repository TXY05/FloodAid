package com.example.floodaid.roomDatabase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.floodaid.models.UserLocation
import com.example.floodaid.models.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE uid = :uid")
    fun getProfile(uid: String): Flow<UserProfile?>

    @Query("DELETE FROM user_profile")
    suspend fun clear()

    @Query("SELECT * FROM user_profile WHERE uid = :uid")
    fun getProfileForLocation(uid: String): Flow<UserProfile?>

    @Update
    suspend fun updateForLocation(profile: UserProfile)

    // Simplified location tracking methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserLocation(location: UserLocation)

    @Query("SELECT * FROM user_location WHERE uid = :uid")
    suspend fun getUserLocation(uid: String): UserLocation?

    @Query("DELETE FROM user_location WHERE uid = :uid")
    suspend fun clearUserLocation(uid: String)
}