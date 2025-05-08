package com.example.floodaid.roomDatabase.repository

import android.util.Log
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import com.example.floodaid.models.VolunteerProfile
import com.example.floodaid.roomDatabase.dao.VolunteerDao
import com.example.floodaid.roomDatabase.dao.VolunteerEventHistoryDao
import com.example.floodaid.roomDatabase.dao.VolunteerProfileDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.String

class VolunteerRepository(
    private val volunteerDao: VolunteerDao,
    private val volunteerHistoryDao: VolunteerEventHistoryDao,
    private val volunteerProfileDao: VolunteerProfileDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    suspend fun syncEventsFromFirebase() {
        try {
            val result = firestore.collection("event").get().await()
            val events = result.documents.mapNotNull { doc ->
                try {
                    doc.toObject(VolunteerEvent::class.java)?.copy(
                        firestoreId = doc.id
                    )
                } catch (e: Exception) {
                    Log.e("VolunteerRepo", "Error parsing doc ${doc.id}", e)
                    null
                }
            }

            val historyResult = firestore.collection("event_history").get().await()
            val histories = historyResult.documents.mapNotNull { doc ->
                try {
                    doc.toObject(VolunteerEventHistory::class.java)?.copy(
                        firestoreId = doc.id
                    )
                } catch (e: Exception) {
                    Log.e("VolunteerHistoryRepo", "Error parsing doc ${doc.id}", e)
                    null
                }
            }
            volunteerDao.deleteAllEvent()
            volunteerDao.insertEvents(events)
            volunteerHistoryDao.deleteAllEventHistory()
            volunteerHistoryDao.insertEventHistory(histories)
        } catch (e: Exception) {
            Log.e("VolunteerRepo", "Sync failed", e)
            throw e
        }
    }

    suspend fun insertEvent(event: VolunteerEvent): String{
        return try {
            val docRef = firestore.collection("event").document()
            val eventWithId = event.copy(firestoreId = docRef.id)
            docRef.set(eventWithId).await()
            volunteerDao.insert(eventWithId)

            docRef.id
        } catch (e: Exception) {
            Log.e("VolunteerRepo", "Insert failed", e)
            throw e
        }
    }

    suspend fun updateEvent(event: VolunteerEvent){
        return try {
            val docRef = firestore.collection("event").document(event.firestoreId)
            docRef.set(event).await()
            volunteerDao.update(event)

        } catch (e: Exception) {
            Log.e("VolunteerRepo", "Insert failed", e)
            throw e
        }
    }

    suspend fun deleteEvent(event: VolunteerEvent){
        try {
            firestore.collection("event").document(event.firestoreId).delete().await()
            volunteerDao.delete(event)
        } catch (e: Exception) {
            Log.e("VolunteerRepo", "Delete event failed", e)
            throw e
        }
    }

    fun getEvent(eventId:String): Flow<VolunteerEvent> =
        volunteerDao.getEvent(eventId)

    suspend fun applyEvent(userId: String, eventId: String): String {
        return try {
            val eventHistory = VolunteerEventHistory(
                userId = userId,
                eventId = eventId,
                date = "",
                startTime = "",
                endTime = "",
                description = "",
                district = "")

            val docRef = firestore.collection("event_history").document()
            val historyWithId = eventHistory.copy(firestoreId = docRef.id)
            docRef.set(historyWithId).await()
            volunteerHistoryDao.insert(historyWithId)

            docRef.id
        } catch (e: Exception) {
            Log.e("VolunteerHistory", "Insert failed", e)
            throw e
        }
    }

    suspend fun deleteEventHistory(eventId: String){
        try {
            val querySnapshot = firestore.collection("event_history")
                .whereEqualTo("eventId", eventId)
                .get()
                .await()
            for (doc in querySnapshot.documents) {
                firestore.collection("event_history").document(doc.id).delete().await()
            }
            volunteerHistoryDao.deleteEventHistory(eventId)

        } catch (e: Exception) {
            Log.e("VolunteerRepo", "Delete event history failed", e)
            throw e
        }
    }

    suspend fun syncVolunteerProfile(userId: String) {
        try {
            volunteerProfileDao.deleteAllVolunteerProfile()
            val document = firestore.collection("volunteer_profile").document(userId).get().await()
            val volunteer = document.toObject(VolunteerProfile::class.java)
            volunteer?.let {
                volunteerProfileDao.insert(it)
            }
        } catch (e: Exception) {
            Log.e("VolunteerRepo", "Failed to sync volunteer profile", e)
        }
    }

    suspend fun saveVolunteerProfile(volunteer: VolunteerProfile) {
        return try {
            firestore.collection("volunteer_profile").document(volunteer.userId).set(volunteer).await()
            volunteerProfileDao.insert(volunteer)

        } catch (e: Exception) {
            Log.e("VolunteerProfileRepo", "Insert failed", e)
            throw e
        }
    }

    suspend fun updateVolunteerProfile(volunteer: VolunteerProfile){
        volunteerProfileDao.update(volunteer)
    }

    suspend fun deleteVolunteerProfile(volunteer: VolunteerProfile){
        volunteerProfileDao.delete(volunteer)
    }

    fun getVolunteerProfile(userId: String): Flow<VolunteerProfile?> {
        return volunteerProfileDao.getVolunteerProfile(userId)
    }

    // For room and firebase sync
    fun getAllEvents(): Flow<List<VolunteerEvent>> =
        volunteerDao.getAllEvents()

    fun getEventHistory(userId: String): Flow<List<VolunteerEventHistory>> {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return volunteerHistoryDao.getEventHistory(userId).map { historyList ->
            historyList.sortedBy { historyItem ->
                try {
                    dateFormat.parse(historyItem.date)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
        }
    }
}