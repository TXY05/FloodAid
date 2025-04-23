package com.example.floodaid.viewmodel

import com.example.floodaid.roomDatabase.State
import com.example.floodaid.roomDatabase.District
import com.example.floodaid.roomDatabase.MapDao

class MapRepository(private val dao: MapDao) {
    suspend fun getAllStates() = dao.getAllStates()

    suspend fun getDistrictsByState(state: String) = dao.getDistrictsByState(state)

    suspend fun insertAllStates(states: List<State>) = dao.insertAllStates(states)

    suspend fun insertAllDistricts(districts: List<District>) = dao.insertAllDistricts(districts)

    suspend fun deleteAllStates() = dao.deleteAllStates()

    suspend fun deleteAllDistricts() = dao.deleteAllDistricts()
}