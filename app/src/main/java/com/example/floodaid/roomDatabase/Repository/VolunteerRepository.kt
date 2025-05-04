package com.example.floodaid.roomDatabase.Repository

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import com.example.floodaid.roomDatabase.Dao.VolunteerDao
import com.example.floodaid.roomDatabase.Dao.VolunteerEventHistoryDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class VolunteerRepository(
    private val volunteerDao: VolunteerDao,
    private val volunteerHistoryDao: VolunteerEventHistoryDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    suspend fun syncEventsFromFirebase() {
        if (user != null) {
            val result = firestore.collection("event").get().await()
            val events = result.toObjects(VolunteerEvent::class.java)
            volunteerDao.insertEvents(events)
        }else{

        }
    }

    suspend fun insertEvent(event: VolunteerEvent){
        if (user != null) {
            firestore.collection("event").document(event.id.toString()).set(event)
            volunteerDao.insert(event)
        }
    }

    suspend fun updateEvent(event: VolunteerEvent){
        volunteerDao.update(event)
    }

    suspend fun deleteEvent(event: VolunteerEvent){
        volunteerDao.delete(event)
    }

    fun getAllEvents(): Flow<List<VolunteerEvent>> =
        volunteerDao.getAllEvents()

    fun getEvent(eventId:Int): Flow<VolunteerEvent> =
        volunteerDao.getEvent(eventId)

    fun getFilteredEvent(date:String): Flow<VolunteerEvent> =
        volunteerDao.getFilteredEvent(date)

    suspend fun applyEvent(userId: String, eventId: String) {
        if (user != null) {
            val eventHistory = VolunteerEventHistory(userId = userId, eventId = eventId)
            firestore.collection("users").document(userId)
                .collection("event_history").document(eventId).set(eventHistory)
            volunteerHistoryDao.insertEventHistory(eventHistory)
        }
    }

    suspend fun deleteEventHistory(event: VolunteerEventHistory){
        volunteerHistoryDao.delete(event)
    }

    fun getEventHistory(userId: String): Flow<List<VolunteerEventHistory>> =
        volunteerHistoryDao.getEventHistory(userId)
}