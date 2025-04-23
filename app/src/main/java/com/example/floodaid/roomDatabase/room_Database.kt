package com.example.floodaid.roomDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [State::class, District::class], version = 1)
@TypeConverters(Converter::class)
abstract class room_Database : RoomDatabase() {

    abstract fun MapDao(): MapDao

    companion object {
        @Volatile
        private var INSTANCE: room_Database? = null

        fun getInstance(context: Context): room_Database {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    room_Database::class.java,
                    "flood_aid_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
