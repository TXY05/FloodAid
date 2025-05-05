package com.example.floodaid.repository

import com.example.floodaid.screen.floodstatus.FloodHistoryEntity
import com.example.floodaid.screen.floodstatus.LocationStatusEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.collections.remove

class FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val floodStatusCollection = firestore.collection("floodstatus")

    fun observeAllLocations(): Flow<List<LocationStatusEntity>> = callbackFlow {
        val listenerRegistration: ListenerRegistration = floodStatusCollection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                return@addSnapshotListener
            }

            val data = snapshot.documents.mapNotNull { doc ->
                val location = doc.id
                val status = doc.getString("status") ?: return@mapNotNull null
                LocationStatusEntity(location, status)
            }
            trySend(data)
        }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun updateFloodStatus(location: String, status: String, date: String) {
        val docRef = floodStatusCollection.document(location)

        // Update current status
        docRef.set(
            mapOf(
                "status" to status,
                "last_updated" to date
            )
        ).await()

        // Append to history
        docRef.collection("history").add(
            mapOf(
                "status" to status,
                "date" to date
            )
        ).await()
    }

    suspend fun pushStatusToFirestore(entity: LocationStatusEntity, date: String) {
        val docRef = floodStatusCollection.document(entity.location)

        // Update current status
        docRef.set(
            mapOf(
                "status" to entity.status,
                "last_updated" to date
            )
        ).await()

        // Append to history
        docRef.collection("history").add(
            mapOf(
                "status" to entity.status,
                "date" to date
            )
        ).await()
    }

    fun observeHistory(location: String): Flow<List<FloodHistoryEntity>> = callbackFlow {
        val historyRef = floodStatusCollection.document(location).collection("history")

        val listener = historyRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val history = snapshot.documents.mapNotNull { doc ->
                val status = doc.getString("status") ?: return@mapNotNull null
                val date = doc.getString("date") ?: return@mapNotNull null
                FloodHistoryEntity(location = location, status = status, date = date)
            }.sortedByDescending { it.date }

            trySend(history)
        }

        awaitClose { listener.remove() }
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
                    FloodHistoryEntity(location = location, status = status, date = date)
                }
                trySend(historyList)
            }
        awaitClose { listener.remove() }
    }
}