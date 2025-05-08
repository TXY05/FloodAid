package com.example.floodaid.roomDatabase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
}