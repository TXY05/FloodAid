package com.example.floodaid.screen.floodstatus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.floodaid.repository.FirestoreRepository
import com.example.floodaid.viewmodel.FloodStatusViewModel

class FloodStatusViewModelFactory(
    private val roomRepository: FloodStatusRepository,
    private val dao: FloodStatusDao,
    private val firestoreRepository: FirestoreRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FloodStatusViewModel::class.java)) {
            return FloodStatusViewModel(roomRepository, dao, firestoreRepository, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}