package com.example.floodaid.screen.volunteer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import com.example.floodaid.roomDatabase.Repository.VolunteerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VolunteerViewModel(
    private val repository: VolunteerRepository
) : ViewModel() {
    val events: StateFlow<List<VolunteerEvent>> = repository.getLocalEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        syncFirebaseEvents()
    }

    fun syncFirebaseEvents() {
        viewModelScope.launch {
            repository.syncEventsFromFirebase()
        }
    }

    fun createEvent(event: VolunteerEvent) {
        viewModelScope.launch {
            repository.createEvent(event)
        }
    }

    fun applyEvent(userId: String, eventId: String) {
        viewModelScope.launch {
            repository.applyEvent(userId, eventId)
        }
    }

    fun getEventHistory(userId: String): LiveData<List<VolunteerEventHistory>> =
        repository.getEventHistory(userId).asLiveData()
}