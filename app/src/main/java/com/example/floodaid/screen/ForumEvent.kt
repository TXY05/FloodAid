package com.example.floodaid.screen

import com.example.floodaid.models.ForumPost

sealed interface ForumEvent {
    object SaveForumPost: ForumEvent
    data class SetTitle(val title: String): ForumEvent
    data class SetContent(val content: String): ForumEvent

    data class SortForumPost(val sortType: ForumSortType): ForumEvent
    data class DeleteForumPost(val forumPost: ForumPost): ForumEvent

}