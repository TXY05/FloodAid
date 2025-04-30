package com.example.floodaid

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.floodaid.models.ForumPost
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumDao {


    @Upsert
    suspend fun upsertForumPost(forumPost: ForumPost)


    @Delete
    suspend fun deleteForumPost(forumPost: ForumPost)

    @Query("SELECT * FROM forumPost WHERE region = :region")
    fun getForumPostFromState(region: String): Flow<List<ForumPost>>

    @Query("SELECT * FROM forumPost ORDER BY timestamp")
    fun getForumPostOrderedByTimeStamp(): Flow<List<ForumPost>>



}