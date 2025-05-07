package com.example.floodaid.screen.forum

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import java.util.UUID

@Entity
data class ForumPost(
    @PrimaryKey
    val id: String ,
    val imageUrls: List<String> = listOf(),
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val region: String = "",
    val authorImageUrl: String = "",
    val commentsCount: Int = 0,
)
 {
    constructor() : this(
        id = UUID.randomUUID().toString(),
        content = "",
        authorId = "",
        authorName = "",
        timestamp = Timestamp.now(),
        region = "",
        authorImageUrl = "",
        imageUrls = listOf(),
        commentsCount = 0,
    )
}
