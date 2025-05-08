package com.example.floodaid.screen.forum

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import java.util.UUID


@Entity
data class ForumComment(
    @PrimaryKey
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorImageUrl: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),

    ) {
    constructor() : this(
        id = UUID.randomUUID().toString(),
        postId = "",
        authorId = "",
        authorName = "",
        authorImageUrl = "",
        content = "",
        timestamp = Timestamp.now()
    )
}