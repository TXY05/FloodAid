package com.example.floodaid.repository

import com.example.floodaid.screen.floodstatus.FloodHistoryEntity
import com.example.floodaid.screen.floodstatus.LocationStatusEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.example.floodaid.roomDatabase.Entities.*
import java.time.Instant

class FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val floodStatusCollection = firestore.collection("floodstatus")

    private val statesCollection = firestore.collection("states")
    private val districtsCollection = firestore.collection("districts")
    private val sheltersCollection = firestore.collection("shelters")
    private val floodMarkersCollection = firestore.collection("flood_markers")

    // Flood Status
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

    // Map
    // States
    suspend fun fetchAllStates(): List<State> {
        val snapshot = statesCollection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            State(
                id = doc.getLong("id") ?: 0L,
                name = doc.getString("name") ?: return@mapNotNull null
            )
        }
    }

    fun listenToStates(): Flow<List<State>> = callbackFlow {
        val listener = statesCollection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val states = snapshot.documents.mapNotNull { doc ->
                State(
                    id = doc.getLong("id") ?: 0L,
                    name = doc.getString("name") ?: return@mapNotNull null
                )
            }
            trySend(states)
        }
        awaitClose { listener.remove() }
    }

    // Districts
    suspend fun fetchAllDistricts(): List<District> {
        val snapshot = districtsCollection.get().await()
        val districts = mutableListOf<District>()

        for (doc in snapshot.documents) {
            val borderCoordinates = mutableListOf<List<Double>>()
            val borderSnapshot = doc.reference.collection("borderCoordinates").get().await()

            for (borderDoc in borderSnapshot.documents) {
                val lat = borderDoc.getDouble("lat") ?: continue
                val lon = borderDoc.getDouble("lon") ?: continue
                borderCoordinates.add(listOf(lat, lon))
            }

            districts.add(
                District(
                    id = doc.getLong("id") ?: 0L,
                    name = doc.getString("name") ?: continue,
                    latitude = doc.getDouble("latitude") ?: continue,
                    longitude = doc.getDouble("longitude") ?: continue,
                    borderCoordinates = if (borderCoordinates.isNotEmpty()) Border(borderCoordinates) else null,
                    stateId = doc.getLong("stateId") ?: continue
                )
            )
        }
        return districts
    }

    fun listenToDistricts(): Flow<List<District>> = callbackFlow {
        val listener = districtsCollection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val districts = mutableListOf<District>()
            for (doc in snapshot.documents) {
                val borderCoordinates = mutableListOf<List<Double>>()
                doc.reference.collection("borderCoordinates").get()
                    .addOnSuccessListener { borderSnapshot ->
                        for (borderDoc in borderSnapshot.documents) {
                            val lat = borderDoc.getDouble("lat") ?: return@addOnSuccessListener
                            val lon = borderDoc.getDouble("lon") ?: return@addOnSuccessListener
                            borderCoordinates.add(listOf(lat, lon))
                        }

                        districts.add(
                            District(
                                id = doc.getLong("id") ?: 0L,
                                name = doc.getString("name") ?: return@addOnSuccessListener,
                                latitude = doc.getDouble("latitude") ?: return@addOnSuccessListener,
                                longitude = doc.getDouble("longitude") ?: return@addOnSuccessListener,
                                borderCoordinates = if (borderCoordinates.isNotEmpty()) Border(borderCoordinates) else null,
                                stateId = doc.getLong("stateId") ?: return@addOnSuccessListener
                            )
                        )
                        trySend(districts)
                    }
            }
        }
        awaitClose { listener.remove() }
    }

    // Add function to push districts to Firestore
    suspend fun pushDistricts(districts: List<District>) {
        val batch = firestore.batch()

        for (district in districts) {
            // Create main district document
            val districtRef = districtsCollection.document()
            val districtData = hashMapOf(
                "id" to district.id,
                "name" to district.name,
                "latitude" to district.latitude,
                "longitude" to district.longitude,
                "stateId" to district.stateId
            )
            batch.set(districtRef, districtData)

            // Add border coordinates if they exist
            district.borderCoordinates?.let { border ->
                // Create ordered document IDs for border coordinates
                border.coordinates.forEachIndexed { index, coordinate ->
                    // Create a document ID that will sort alphabetically (e.g., "A001", "A002", etc.)
                    val docId = String.format("A%03d", index + 1)
                    val borderRef = districtRef.collection("borderCoordinates").document(docId)
                    val borderData = hashMapOf(
                        "lat" to coordinate[0],
                        "lon" to coordinate[1],
                        "order" to index // Add an order field for additional sorting
                    )
                    batch.set(borderRef, borderData)
                }
            }
        }

        // Commit the batch
        batch.commit().await()
    }

    // Shelters
    suspend fun fetchAllShelters(): List<Shelter> {
        val snapshot = sheltersCollection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            Shelter(
                id = doc.getLong("id") ?: 0L,
                helpCenterName = doc.getString("helpCenterName") ?: return@mapNotNull null,
                descriptions = doc.getString("descriptions") ?: "",
                latitude = doc.getDouble("latitude") ?: return@mapNotNull null,
                longitude = doc.getDouble("longitude") ?: return@mapNotNull null,
                districtId = doc.getLong("districtId") ?: return@mapNotNull null,
                address = doc.getString("address")
            )
        }
    }

    fun listenToShelters(): Flow<List<Shelter>> = callbackFlow {
        val listener = sheltersCollection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val shelters = snapshot.documents.mapNotNull { doc ->
                Shelter(
                    id = doc.getLong("id") ?: 0L,
                    helpCenterName = doc.getString("helpCenterName") ?: return@mapNotNull null,
                    descriptions = doc.getString("descriptions") ?: "",
                    latitude = doc.getDouble("latitude") ?: return@mapNotNull null,
                    longitude = doc.getDouble("longitude") ?: return@mapNotNull null,
                    districtId = doc.getLong("districtId") ?: return@mapNotNull null,
                    address = doc.getString("address")
                )
            }
            trySend(shelters)
        }
        awaitClose { listener.remove() }
    }

    // Flood Markers
    suspend fun fetchAllFloodMarkers(): List<FloodMarker> {
        val currentTime = Instant.now().toEpochMilli()
        val snapshot = floodMarkersCollection
            .whereGreaterThan("expiryTime", currentTime)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            FloodMarker(
                id = doc.id, // Use document ID directly
                floodStatus = doc.getString("floodStatus") ?: return@mapNotNull null,
                districtId = doc.getLong("districtId") ?: return@mapNotNull null,
                latitude = doc.getDouble("latitude") ?: return@mapNotNull null,
                longitude = doc.getDouble("longitude") ?: return@mapNotNull null,
                expiryTime = Instant.ofEpochMilli(doc.getLong("expiryTime") ?: return@mapNotNull null),
                reporterId = doc.getString("reporterId")
            )
        }
    }

    suspend fun pushFloodMarker(marker: FloodMarker) {
        val markerData = mapOf(
            "floodStatus" to marker.floodStatus,
            "districtId" to marker.districtId,
            "latitude" to marker.latitude,
            "longitude" to marker.longitude,
            "expiryTime" to marker.expiryTime.toEpochMilli(),
            "reporterId" to marker.reporterId
        )

        // Let FireStore generate the ID
        val docRef = floodMarkersCollection.document()
        docRef.set(markerData).await()

        // Return the marker with the FireStore ID
        marker.copy(id = docRef.id)
    }

    fun listenToFloodMarkers(): Flow<List<FloodMarker>> = callbackFlow {
        val listener = floodMarkersCollection
            .whereGreaterThan("expiryTime", Instant.now().toEpochMilli())
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val markers = snapshot.documents.mapNotNull { doc ->
                    FloodMarker(
                        id = doc.id, // Use document ID directly
                        floodStatus = doc.getString("floodStatus") ?: return@mapNotNull null,
                        districtId = doc.getLong("districtId") ?: return@mapNotNull null,
                        latitude = doc.getDouble("latitude") ?: return@mapNotNull null,
                        longitude = doc.getDouble("longitude") ?: return@mapNotNull null,
                        expiryTime = Instant.ofEpochMilli(doc.getLong("expiryTime") ?: return@mapNotNull null),
                        reporterId = doc.getString("reporterId")
                    )
                }
                trySend(markers)
            }
        awaitClose { listener.remove() }
    }

    suspend fun cleanupExpiredMarkers() {
        val currentTime = Instant.now().toEpochMilli()
        val expiredMarkers = floodMarkersCollection
            .whereLessThanOrEqualTo("expiryTime", currentTime)
            .get()
            .await()

        expiredMarkers.documents.forEach { doc ->
            doc.reference.delete().await()
        }
    }
}
