package com.example.floodaid.models

data class ForumComment(
    val content: String = "",
    val authorId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)