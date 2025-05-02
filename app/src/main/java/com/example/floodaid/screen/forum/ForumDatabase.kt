package com.example.floodaid.screen.forum

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.floodaid.screen.forum.ForumDao
import com.example.floodaid.models.ForumPost


@Database(
    entities = [ForumPost::class],
    version = 1
)
abstract class ForumDatabase : RoomDatabase() {

    abstract val dao: ForumDao
}