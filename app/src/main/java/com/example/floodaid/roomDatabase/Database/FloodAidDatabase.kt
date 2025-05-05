package com.example.floodaid.roomDatabase.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.floodaid.models.ForumPost
import com.example.floodaid.models.UserProfile
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import com.example.floodaid.utils.Converter
import com.example.floodaid.roomDatabase.Dao.MapDao
import com.example.floodaid.roomDatabase.Dao.UserProfileDao
import com.example.floodaid.roomDatabase.Dao.VolunteerDao
import com.example.floodaid.roomDatabase.Dao.VolunteerEventHistoryDao
import com.example.floodaid.roomDatabase.Entities.District
import com.example.floodaid.roomDatabase.Entities.FloodMarker
import com.example.floodaid.roomDatabase.Entities.Shelter
import com.example.floodaid.roomDatabase.Entities.State
import com.example.floodaid.screen.forum.ForumDao
import com.example.floodaid.screen.floodstatus.FloodHistoryEntity
import com.example.floodaid.screen.floodstatus.FloodStatusDao
import com.example.floodaid.screen.floodstatus.LocationStatusEntity

@Database(
    entities = [
        State::class,
        District::class,
        Shelter::class,
        FloodMarker::class,
        VolunteerEvent::class,
        VolunteerEventHistory::class,
        LocationStatusEntity::class,
        FloodHistoryEntity::class,
        UserProfile::class,
        ForumPost::class
    ],
    version = 5, // Increment this number by 1
    exportSchema = true
)
@TypeConverters(Converter::class)
abstract class FloodAidDatabase : RoomDatabase() {

    abstract fun MapDao(): MapDao
    abstract fun volunteerDao(): VolunteerDao
    abstract fun volunteerEventHistoryDao(): VolunteerEventHistoryDao
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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}