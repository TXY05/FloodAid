package com.example.floodaid.screen.forum

import androidx.room.PrimaryKey
import java.util.UUID

data class ForumState(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val timestamp: Long = 0L,
    val region: String = "",
    val imageUrls: List<String> = emptyList(),
    val forumPosts: List<ForumPost> = emptyList(),
    val sortType: ForumSortType = ForumSortType.TIME_STAMP
)


