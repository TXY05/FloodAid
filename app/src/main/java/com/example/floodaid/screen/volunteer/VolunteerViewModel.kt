package com.example.floodaid.screen.volunteer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import com.example.floodaid.roomDatabase.Repository.VolunteerRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.String

class VolunteerViewModel(
    private val repository: VolunteerRepository,
    internal val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    val events: StateFlow<List<VolunteerEvent>> = repository.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _history = MutableStateFlow<List<VolunteerEventHistory>>(emptyList())
    val history: StateFlow<List<VolunteerEventHistory>> = _history

    private val _authState = MutableStateFlow(auth.currentUser != null)
    val authState: StateFlow<Boolean> = _authState

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser != null
            if (firebaseAuth.currentUser != null) {
                syncFirebaseEvents()
                getEventHistory(firebaseAuth.currentUser!!.uid)
            }
        }
    }

    internal fun syncFirebaseEvents() {
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

    fun getEvent(eventId: String): StateFlow<VolunteerEvent?> {
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

    // For room and firebase sync
    fun getEventHistory(userId: String) {
        viewModelScope.launch {
            repository.getEventHistory(userId).collect { historyList ->
                _history.value = historyList
            }
        }
    }
}