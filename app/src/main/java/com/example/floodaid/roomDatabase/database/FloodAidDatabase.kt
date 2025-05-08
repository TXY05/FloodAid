package com.example.floodaid.roomDatabase.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.floodaid.models.UserLocation
import com.example.floodaid.models.UserProfile
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import com.example.floodaid.models.VolunteerProfile
import com.example.floodaid.utils.Converter
import com.example.floodaid.roomDatabase.dao.MapDao
import com.example.floodaid.roomDatabase.dao.UserProfileDao
import com.example.floodaid.roomDatabase.dao.VolunteerDao
import com.example.floodaid.roomDatabase.dao.VolunteerEventHistoryDao
import com.example.floodaid.roomDatabase.dao.VolunteerProfileDao
import com.example.floodaid.roomDatabase.entities.District
import com.example.floodaid.roomDatabase.entities.FloodMarker
import com.example.floodaid.roomDatabase.entities.Shelter
import com.example.floodaid.roomDatabase.entities.State
import com.example.floodaid.screen.forum.ForumDao
import com.example.floodaid.screen.floodstatus.FloodHistoryEntity
import com.example.floodaid.screen.floodstatus.FloodStatusDao
import com.example.floodaid.screen.floodstatus.LocationStatusEntity
import com.example.floodaid.screen.forum.ForumPost

@Database(
    entities = [
        State::class,
        District::class,
        Shelter::class,
        FloodMarker::class,
        VolunteerEvent::class,
        VolunteerEventHistory::class,
        VolunteerProfile::class,
        LocationStatusEntity::class,
        FloodHistoryEntity::class,
        UserProfile::class,
        UserLocation::class,
        ForumPost::class
    ],
    version = 13, // Increment this number by 1
    exportSchema = true
)
@TypeConverters(Converter::class)
abstract class FloodAidDatabase : RoomDatabase() {

    abstract fun MapDao(): MapDao
    abstract fun volunteerDao(): VolunteerDao
    abstract fun volunteerEventHistoryDao(): VolunteerEventHistoryDao
    abstract fun volunteerProfileDao(): VolunteerProfileDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun forumDao(): ForumDao
    abstract fun floodStatusDao(): FloodStatusDao

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
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}