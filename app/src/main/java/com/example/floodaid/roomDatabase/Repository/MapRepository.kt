package com.example.floodaid.roomDatabase.Repository

import com.example.floodaid.roomDatabase.Dao.MapDao
import com.example.floodaid.roomDatabase.Entities.District
import com.example.floodaid.roomDatabase.Entities.FloodMarker
import com.example.floodaid.roomDatabase.Entities.Shelter
import com.example.floodaid.roomDatabase.Entities.State

class MapRepository(private val dao: MapDao) {
    // State operations
    suspend fun getAllStates() = dao.getAllStates()
    suspend fun insertAllStates(states: List<State>) = dao.insertAllStates(states)
    suspend fun deleteAllStates() = dao.deleteAllStates()

    // District operations
    suspend fun getDistrictsByState(stateId: Long) = dao.getDistrictsByState(stateId)
    suspend fun insertAllDistricts(districts: List<District>) = dao.insertAllDistricts(districts)
    suspend fun deleteAllDistricts() = dao.deleteAllDistricts()

    // Shelter operations
    fun getSheltersByDistrict(districtId: Long) = dao.getSheltersByDistrict(districtId)
    suspend fun insertAllShelters(shelters: List<Shelter>) = dao.insertAllShelters(shelters)
    suspend fun deleteAllShelter() = dao.deleteAllShelters()
    suspend fun getShelterById(shelterId: Long) = dao.getShelterById(shelterId)

    // FloodMarker operations
    fun getMarkersByDistrict(districtId: Long) = dao.getMarkersByDistrict(districtId)
    fun getActiveMarkersByDistrict(districtId: Long) = dao.getActiveMarkersByDistrict(districtId)
    suspend fun insertAllMarkers(markers: List<FloodMarker>) = dao.insertAllMarkers(markers)
    suspend fun updateMarker(marker: FloodMarker) = dao.updateMarker(marker)
    suspend fun deleteMarker(id: Long) = dao.deleteMarker(id)
    suspend fun cleanupExpiredMarkers() = dao.cleanupExpiredMarkers()
    suspend fun getMarkerById(id: Long) = dao.getMarkerById(id)
    suspend fun deleteAllMarkers() = dao.deleteAllMarkers()

//    // Complex relationships
//    suspend fun getStateWithDistricts(stateId: Long) = dao.getStateWithDistricts(stateId)
//    suspend fun getDistrictWithMarkers(districtId: Long) = dao.getDistrictWithMarkers(districtId)
}