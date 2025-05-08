package com.example.floodaid.screen.floodstatus

import android.util.Log
import com.example.floodaid.roomDatabase.Repository.FirestoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FloodStatusRepository(private val dao: FloodStatusDao, private val firestoreRepository: FirestoreRepository) {
    val predefinedDistricts = listOf(
        LocationStatusEntity(location = "Gombak", status = ""),
        LocationStatusEntity(location = "Hulu Langat", status = ""),
        LocationStatusEntity(location = "Hulu Selangor", status = ""),
        LocationStatusEntity(location = "Klang", status = ""),
        LocationStatusEntity(location = "Kuala Selangor", status = ""),
        LocationStatusEntity(location = "Petaling", status = ""),
        LocationStatusEntity(location = "Sabak Bernam", status = ""),
        LocationStatusEntity(location = "Sepang", status = "")
    )

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun syncFloodStatusFromFirestore() = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("floodstatus").get().await()
            for (doc in snapshot.documents) {
                val location = doc.id // Use document ID as location name
                val status = doc.getString("status") ?: continue
                val date = doc.getString("last_updated") ?: continue
                val time = doc.getString("last_updated_time") ?: "00:00"

                val locationStatus = LocationStatusEntity(location, status)
                val history = FloodHistoryEntity(location = location, status = status, date = date, time = time)

                dao.insertLocationStatus(locationStatus)
                dao.insertFloodHistory(history)
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error syncing from Firestore: ${e.message}", e)
        }
    }

    fun getAllLocations(): Flow<List<LocationStatusEntity>> = dao.getAllLocations()

    suspend fun initializePredefinedDistricts() {
        val existingDistricts = dao.getAllLocationsOnce() // Add a method to fetch all locations synchronously
        if (existingDistricts.isEmpty()) {
            dao.insertInitialLocations(predefinedDistricts)
        }
    }

    suspend fun insertOrUpdateLocation(location: String, status: String) {
        dao.insertLocationStatus(LocationStatusEntity(location, status))
    }

    suspend fun updateFloodStatus(location: String, status: String, date: String, time: String) {
        firestoreRepository.updateFloodStatus(location, status, date, time)
    }

    suspend fun getFloodStatusForLocation(location: String): String {
        return withContext(Dispatchers.IO) {
            dao.getFloodStatus(location) // Fetch from Room database
        }
    }
//    // Clear all data from both Firestore and Room
//    suspend fun clearAllData() {
//        dao.clearAllStatuses()
//        dao.clearAllHistories()
//    }
}
