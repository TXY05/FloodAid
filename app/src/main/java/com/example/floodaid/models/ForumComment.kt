package com.example.floodaid.models

import com.google.firebase.Timestamp

data class ForumComment(
    val content: String = "",
    val authorId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
)