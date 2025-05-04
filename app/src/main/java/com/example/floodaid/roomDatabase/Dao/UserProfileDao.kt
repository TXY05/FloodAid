package com.example.floodaid.roomDatabase.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.floodaid.models.UserProfile

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE uid = :uid")
    suspend fun getProfile(uid: String): UserProfile?

    @Query("DELETE FROM user_profile")
    suspend fun clear()
}