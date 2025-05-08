package com.example.floodaid.screen.forum


sealed interface ForumEvent {
    data class SaveForumPost(val forumPost: ForumPost) : ForumEvent
    data class EditForumPost(val forumPost: ForumPost) : ForumEvent

    data class SetContent(val content: String) : ForumEvent
    data class SetRegion(val region: String) : ForumEvent
    data class SetImages(val images: List<String>) : ForumEvent

    data class SortForumPost(val sortType: ForumSortType) : ForumEvent
    data class DeleteForumPost(val forumPost: ForumPost) : ForumEvent
}
