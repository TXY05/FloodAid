package com.example.floodaid.roomDatabase.Repository

import android.util.Log
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
import kotlin.String

class VolunteerRepository(
    private val volunteerDao: VolunteerDao,
    private val volunteerHistoryDao: VolunteerEventHistoryDao,
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
        volunteerDao.update(event)
    }

    suspend fun deleteEvent(event: VolunteerEvent){
        volunteerDao.delete(event)
    }

    fun getEvent(eventId:String): Flow<VolunteerEvent> =
        volunteerDao.getEvent(eventId)

    fun getFilteredEvent(date:String): Flow<VolunteerEvent> =
        volunteerDao.getFilteredEvent(date)

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

    suspend fun deleteEventHistory(event: VolunteerEventHistory){
        volunteerHistoryDao.delete(event)
    }

    // For room and firebase sync
    fun getAllEvents(): Flow<List<VolunteerEvent>> =
        volunteerDao.getAllEvents()

    fun getEventHistory(userId: String): Flow<List<VolunteerEventHistory>> =
        volunteerHistoryDao.getEventHistory(userId)
}