package com.example.floodaid.roomDatabase.Repository

import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import com.example.floodaid.roomDatabase.Dao.VolunteerDao
import com.example.floodaid.roomDatabase.Dao.VolunteerEventHistoryDao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class VolunteerRepository(
    private val volunteerDao: VolunteerDao,
    private val volunteerHistoryDao: VolunteerEventHistoryDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getLocalEvents(): Flow<List<VolunteerEvent>> = volunteerDao.getAllEvents()

    suspend fun syncEventsFromFirebase() {
        val result = firestore.collection("event").get().await() // use kotlinx-coroutines-play-services
        val events = result.toObjects(VolunteerEvent::class.java)
        volunteerDao.insertEvents(events)
    }

    suspend fun createEvent(event: VolunteerEvent) {
        firestore.collection("event").document(event.id).set(event)
        volunteerDao.insertEvents(listOf(event))
    }

    suspend fun applyEvent(userId: String, eventId: String) {
        val eventHistory = VolunteerEventHistory(userId = userId, eventId = eventId)
        firestore.collection("users").document(userId)
            .collection("event_history").document(eventId).set(eventHistory)
        volunteerHistoryDao.insertEventHistory(eventHistory)
    }

    fun getEventHistory(userId: String): Flow<List<VolunteerEventHistory>> =
        volunteerHistoryDao.getEventHistory(userId)
}