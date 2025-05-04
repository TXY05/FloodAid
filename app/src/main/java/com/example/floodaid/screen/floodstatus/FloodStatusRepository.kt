package com.example.floodaid.screen.floodstatus

import kotlinx.coroutines.flow.Flow
import com.example.floodaid.screen.floodstatus.FloodStatusDao

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

    fun getAllLocations(): Flow<List<LocationStatusEntity>> = dao.getAllLocations()

    fun getHistory(location: String): Flow<List<FloodHistoryEntity>> =
        dao.getHistoryForLocation(location)

    suspend fun updateStatus(location: String, status: String, date: String) {
        dao.updateLocationStatus(LocationStatusEntity(location, status))
        dao.insertHistory(FloodHistoryEntity(location = location, status = status, date = date))
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
}
