package com.example.floodaid.screen.floodstatus

import android.util.Log
import kotlinx.coroutines.flow.Flow
import com.example.floodaid.screen.floodstatus.FloodStatusDao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FloodStatusRepository(private val dao: FloodStatusDao) {
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

                val locationStatus = LocationStatusEntity(location, status)
                val history = FloodHistoryEntity(location = location, status = status, date = date)

                dao.insertLocationStatus(locationStatus)
                dao.insertFloodHistory(history)
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error syncing from Firestore: ${e.message}", e)
        }
    }

    fun getAllLocations(): Flow<List<LocationStatusEntity>> = dao.getAllLocations()

    fun getFloodHistory(location: String): Flow<List<FloodHistoryEntity>> {
        return dao.getHistoryForLocation(location)
    }

    suspend fun updateStatus(location: String, status: String, date: String) {
        dao.updateLocationStatus(LocationStatusEntity(location, status))
        dao.insertFloodHistory(FloodHistoryEntity(location = location, status = status, date = date))
    }

    suspend fun insertInitial(locations: List<LocationStatusEntity>) {
        dao.insertInitialLocations(locations)
    }

    suspend fun initializePredefinedDistricts() {
        val existingDistricts = dao.getAllLocationsOnce() // Add a method to fetch all locations synchronously
        if (existingDistricts.isEmpty()) {
            dao.insertInitialLocations(predefinedDistricts)
        }
    }
    suspend fun clearAllData() {
        dao.clearAllStatuses()
        dao.clearAllHistories()
    }

    suspend fun insertOrUpdateLocation(location: String, status: String) {
        dao.insertLocationStatus(LocationStatusEntity(location, status))
    }
}
