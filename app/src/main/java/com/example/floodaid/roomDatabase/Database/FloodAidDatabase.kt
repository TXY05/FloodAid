package com.example.floodaid.roomDatabase.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import com.example.floodaid.utils.Converter
import com.example.floodaid.roomDatabase.Dao.MapDao
import com.example.floodaid.roomDatabase.Dao.VolunteerDao
import com.example.floodaid.roomDatabase.Dao.VolunteerEventHistoryDao
import com.example.floodaid.roomDatabase.Entities.District
import com.example.floodaid.roomDatabase.Entities.FloodMarker
import com.example.floodaid.roomDatabase.Entities.Shelter
import com.example.floodaid.roomDatabase.Entities.State

@Database(
    entities = [
        State::class,
        District::class,
        Shelter::class,
        FloodMarker::class,
        VolunteerEvent::class,
        VolunteerEventHistory::class
    ],
    version = 2, // Increment this number by 1
    exportSchema = true
)
@TypeConverters(Converter::class)
abstract class FloodAidDatabase : RoomDatabase() {

    abstract fun MapDao(): MapDao
    abstract fun volunteerDao(): VolunteerDao
    abstract fun volunteerEventHistoryDao(): VolunteerEventHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: FloodAidDatabase? = null

        fun getInstance(context: Context): FloodAidDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FloodAidDatabase::class.java,
                    "flood_aid_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}