package com.example.floodaid.screen.forum

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class ForumPost(
    val content: String = "",
    val authorId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val region: String = "",
    val authorImageBase64: String = "",
    val imageUrls: List<String> = listOf(),
    @PrimaryKey()
    val id: String = UUID.randomUUID().toString(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,

    ) {
    constructor() : this(
        id = UUID.randomUUID().toString(),
        content = "",
        authorId = "",
        timestamp = System.currentTimeMillis(),
        region = "",
        authorImageBase64 = "",
        imageUrls = listOf(),
        likesCount = 0,
        commentsCount = 0
    )
}
