package com.example.floodaid.repository

import com.example.floodaid.screen.floodstatus.FloodHistoryEntity
import com.example.floodaid.screen.floodstatus.LocationStatusEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val floodStatusCollection = firestore.collection("floodstatus")

    suspend fun updateFloodStatus(location: String, status: String, date: String, time: String) {
        val docRef = firestore.collection("floodstatus").document(location)

        // Update current status with timestamp
        docRef.set(
            mapOf(
                "status" to status,
                "last_updated_date" to date,
                "last_updated_time" to time
            )
        ).await()

        // Add to history with full timestamp
        docRef.collection("history").add(
            mapOf(
                "status" to status,
                "date" to date,
                "time" to time,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
        ).await()
    }
    fun listenToFloodStatus(): Flow<List<LocationStatusEntity>> = callbackFlow {
        val listener = firestore.collection("floodstatus").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val statusList = snapshot.documents.mapNotNull { doc ->
                val location = doc.id // Document ID is the location
                val status = doc.getString("status") ?: return@mapNotNull null
                LocationStatusEntity(location, status)
            }
            trySend(statusList)
        }
        awaitClose { listener.remove() }
    }

    fun listenToFloodHistory(location: String): Flow<List<FloodHistoryEntity>> = callbackFlow {
        val listener = firestore.collection("floodstatus").document(location)
            .collection("history")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val historyList = snapshot.documents.mapNotNull { doc ->
                    val status = doc.getString("status") ?: return@mapNotNull null
                    val date = doc.getString("date") ?: return@mapNotNull null
                    val time = doc.getString("time") ?: "00:00"
                    FloodHistoryEntity(location = location, status = status, date = date, time = time)
                }
                trySend(historyList)
            }
        awaitClose { listener.remove() }
    }

    fun observeHistory(location: String): Flow<List<FloodHistoryEntity>> = callbackFlow {
        val historyRef = firestore.collection("floodstatus")
            .document(location)
            .collection("history")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = historyRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val history = snapshot.documents.mapNotNull { doc ->
                val status = doc.getString("status") ?: return@mapNotNull null
                val date = doc.getString("date") ?: return@mapNotNull null
                val time = doc.getString("time") ?: "00:00" // Default if time not set
                FloodHistoryEntity(location = location, status = status, date = date, time = time)
            }
            trySend(history)
        }

        awaitClose { listener.remove() }
    }
}