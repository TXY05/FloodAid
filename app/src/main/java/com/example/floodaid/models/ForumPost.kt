package com.example.floodaid.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ForumPost(
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val region : String = "",
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)








