package com.example.floodaid.roomDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [State::class, District::class], version = 1)
@TypeConverters(Converter::class)
abstract class MapDatabase : RoomDatabase() {

    abstract fun MapDao(): MapDao

    companion object {
        @Volatile
        private var INSTANCE: MapDatabase? = null

        fun getInstance(context: Context): MapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MapDatabase::class.java,
                    "flood_aid_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
