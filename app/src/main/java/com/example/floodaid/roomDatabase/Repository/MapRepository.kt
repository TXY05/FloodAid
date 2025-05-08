package com.example.floodaid.roomDatabase.Repository

import com.example.floodaid.roomDatabase.Repository.FirestoreRepository
import com.example.floodaid.roomDatabase.Dao.MapDao
import com.example.floodaid.roomDatabase.Entities.District
import com.example.floodaid.roomDatabase.Entities.FloodMarker
import com.example.floodaid.roomDatabase.Entities.Shelter
import com.example.floodaid.roomDatabase.Entities.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import android.util.Log

class MapRepository(
    private val dao: MapDao,
    private val FirestoreRepository: FirestoreRepository
) {
    // State operations
    suspend fun getAllStates(): List<State> {
        return dao.getAllStates().distinctBy { it.id } // or distinctBy { it.name }
    }
    suspend fun insertState(states: State) = dao.insertState(states)
    suspend fun insertAllStates(states: List<State>) = dao.insertAllStates(states)
    suspend fun deleteAllStates() = dao.deleteAllStates()

    // District operations
//    suspend fun getAllDistricts(): List<District> = dao.getAllDistricts()
    suspend fun getAllDistricts(): List<District> {
        return dao.getAllDistricts()
    }
    suspend fun getDistrictsByState(stateId: Long) = dao.getDistrictsByState(stateId)
    suspend fun getDistrictsByID(districtId: Long) = dao.getDistrictsByID(districtId)
    suspend fun getDistrictsByName(districtName: String): District {
        return dao.getDistrictsByName(districtName)
    }
    suspend fun insertAllDistricts(districts: List<District>) = dao.insertAllDistricts(districts)
    suspend fun deleteAllDistricts() = dao.deleteAllDistricts()

    // Shelter operations
    fun getSheltersByDistrict(districtId: Long) = dao.getSheltersByDistrict(districtId)
    suspend fun insertAllShelters(shelters: List<Shelter>) = dao.insertAllShelters(shelters)
    suspend fun deleteAllShelter() = dao.deleteAllShelters()
    suspend fun getShelterById(shelterId: Long) = dao.getShelterById(shelterId)

    // FloodMarker operations
    fun getActiveMarkersByDistrict(districtId: Long) = dao.getActiveMarkersByDistrict(districtId)
    suspend fun insertAllMarkers(markers: List<FloodMarker>) = dao.insertAllMarkers(markers)
    suspend fun updateMarker(marker: FloodMarker) = dao.updateMarker(marker)
    suspend fun deleteMarker(id: Long) = dao.deleteMarker(id)
    suspend fun cleanupRoomMarkers() = dao.cleanupExpiredMarkers()
    suspend fun getMarkerById(id: Long) = dao.getMarkerById(id)
    suspend fun deleteAllMarkers() = dao.deleteAllMarkers()

    // FireStore Operations
    suspend fun syncAllData() {
        try {
            syncStates()
            syncDistricts()
            syncShelters()
            syncFloodMarkers()

        } catch (e: Exception) {
            Log.e("MapRepository", "Error during sync: ${e.message}")
        }
    }

    // Separate sync functions for individual tables
    suspend fun syncStates() {
        try {
            val states = FirestoreRepository.fetchAllStates()
            insertAllStates(states)
        } catch (e: Exception) {
            Log.e("MapRepository", "Error syncing states: ${e.message}")
        }
    }

    suspend fun syncDistricts() {
        try {
            val districts = FirestoreRepository.fetchAllDistricts()
            insertAllDistricts(districts)
        } catch (e: Exception) {
            Log.e("MapRepository", "Error syncing districts: ${e.message}")
        }
    }

    suspend fun syncShelters() {
        try {
            val shelters = FirestoreRepository.fetchAllShelters()
            insertAllShelters(shelters)
        } catch (e: Exception) {
            Log.e("MapRepository", "Error syncing shelters: ${e.message}")
        }
    }

    suspend fun syncFloodMarkers() {
        try {
            val markers = FirestoreRepository.fetchAllFloodMarkers()
            insertAllMarkers(markers)
        } catch (e: Exception) {
            Log.e("MapRepository", "Error syncing flood markers: ${e.message}")
        }
    }

    // Listen to FireStore updates
    fun listenToStatesUpdates(): Flow<List<State>> = FirestoreRepository.listenToStates()
    fun listenToDistrictsUpdates(): Flow<List<District>> = FirestoreRepository.listenToDistricts()
    fun listenToSheltersUpdates(): Flow<List<Shelter>> = FirestoreRepository.listenToShelters()
    fun listenToFloodMarkersUpdates(): Flow<List<FloodMarker>> = FirestoreRepository.listenToFloodMarkers()

    suspend fun pushDistricts(district: List<District>) {
        FirestoreRepository.pushDistricts(district)
    }

    suspend fun pushShelters(shelters: List<Shelter>) {
        FirestoreRepository.pushShelters(shelters)
    }

    // Add new flood marker to FireStore
    suspend fun pushFloodMarker(marker: FloodMarker) {
        FirestoreRepository.pushFloodMarker(marker)
    }

    suspend fun cleanupFireStoreMarkers(){
        FirestoreRepository.cleanupExpiredMarkers()
    }
}