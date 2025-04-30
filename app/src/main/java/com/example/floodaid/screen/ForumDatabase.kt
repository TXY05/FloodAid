package com.example.floodaid.screen

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.floodaid.ForumDao
import com.example.floodaid.models.ForumPost


@Database(
    entities = [ForumPost::class],
    version = 1
)
abstract class ForumDatabase : RoomDatabase() {

    abstract val dao: ForumDao
}