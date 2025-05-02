package com.example.floodaid.screen.forum

import com.example.floodaid.models.ForumPost

data class ForumPostState(
    val forumPost: List<ForumPost> = emptyList(),
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val timestamp: Long = 0,
    val region : String = "",
    val isAddingPost: Boolean = false,
    val sortType: ForumSortType = ForumSortType.TIME_STAMP
)
