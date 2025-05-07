package com.example.floodaid.screen.forum

import com.google.firebase.Timestamp
import java.util.UUID

data class ForumState(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val region: String = "",
    val authorImageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val forumPosts: List<ForumPost> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sortType: ForumSortType = ForumSortType.TIME_STAMP
)


