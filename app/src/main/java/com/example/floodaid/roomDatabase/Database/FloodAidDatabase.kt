package com.example.floodaid.roomDatabase.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.floodaid.utils.Converter
import com.example.floodaid.roomDatabase.Dao.MapDao
import com.example.floodaid.roomDatabase.Entities.District
import com.example.floodaid.roomDatabase.Entities.FloodMarker
import com.example.floodaid.roomDatabase.Entities.Shelter
import com.example.floodaid.roomDatabase.Entities.State

@Database(
    entities = [State::class, District::class, Shelter::class, FloodMarker::class],
    version = 1
)
@TypeConverters(Converter::class)
abstract class FloodAidDatabase : RoomDatabase() {

    abstract fun MapDao(): MapDao

    companion object {
        @Volatile
        private var INSTANCE: FloodAidDatabase? = null

        fun getInstance(context: Context): FloodAidDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FloodAidDatabase::class.java,
                    "flood_aid_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}