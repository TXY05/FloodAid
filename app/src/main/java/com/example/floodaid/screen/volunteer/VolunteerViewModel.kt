package com.example.floodaid.screen.volunteer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import com.example.floodaid.roomDatabase.Repository.VolunteerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class VolunteerViewModel(
    private val repository: VolunteerRepository
) : ViewModel() {
    val events: StateFlow<List<VolunteerEvent>> = repository.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        syncFirebaseEvents()
    }

    private fun syncFirebaseEvents() {
        viewModelScope.launch {
            repository.syncEventsFromFirebase()
        }
    }

    fun insertEvent(event: VolunteerEvent) {
        viewModelScope.launch {
            repository.insertEvent(event)
        }
    }

    fun updateEvent(event: VolunteerEvent) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun deleteEvent(event: VolunteerEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun getEvent(eventId: Int): StateFlow<VolunteerEvent?> {
        return repository.getEvent(eventId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun getFilteredEvent(date: String): StateFlow<VolunteerEvent?> {
        return repository.getFilteredEvent(date)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun applyEvent(userId: String, eventId: String) {
        viewModelScope.launch {
            repository.applyEvent(userId, eventId)
        }
    }

    fun deleteEventHistory(event: VolunteerEventHistory) {
        viewModelScope.launch {
            repository.deleteEventHistory(event)
        }
    }

    fun getEventHistory(userId: String): LiveData<List<VolunteerEventHistory>> =
        repository.getEventHistory(userId).asLiveData()
}