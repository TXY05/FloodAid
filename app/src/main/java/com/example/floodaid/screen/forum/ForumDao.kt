package com.example.floodaid.screen.forum

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumDao {

    @Upsert
    suspend fun upsertForumPost(post: ForumPost)

    @Upsert
    suspend fun upsertForumPost(posts: List<ForumPost>)

    @Query("DELETE FROM ForumPost")
    suspend fun clearAllForumPosts()

    @Delete
    suspend fun deleteForumPost(forumPost: ForumPost)

    @Query("SELECT * FROM forumPost ORDER BY timestamp DESC")
    fun getForumPostOrderedByTimeStamp(): Flow<List<ForumPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<ForumPost>)


}