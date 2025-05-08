package com.example.floodaid.screen.volunteer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import com.example.floodaid.models.VolunteerProfile
import com.example.floodaid.roomDatabase.repository.VolunteerRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.String

class VolunteerViewModel(
    private val repository: VolunteerRepository,
    internal val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    val events: StateFlow<List<VolunteerEvent>> = repository.getAllEvents()
        .map { events ->
            events.sortedBy { event ->
                try {
                    dateFormat.parse(event.date)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _history = MutableStateFlow<List<VolunteerEventHistory>>(emptyList())
    val history: StateFlow<List<VolunteerEventHistory>> = _history

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate

    private val _volunteer = MutableStateFlow(VolunteerProfile())
    val volunteer: StateFlow<VolunteerProfile> = _volunteer

    private val _authState = MutableStateFlow(auth.currentUser != null)
    val authState: StateFlow<Boolean> = _authState

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser != null
            if (firebaseAuth.currentUser != null) {
                syncFirebaseEvents()
                getEventHistory(firebaseAuth.currentUser!!.uid)
                syncVolunteerProfile()
            }
        }
    }

    internal fun syncFirebaseEvents() {
        viewModelScope.launch {
            repository.syncEventsFromFirebase()
        }
    }

    private fun syncVolunteerProfile() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            repository.syncVolunteerProfile(userId)
            repository.getVolunteerProfile(userId).collect { profile ->
                profile?.let { _volunteer.value = it }
            }
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

    fun getEvent(eventId: String): StateFlow<VolunteerEvent?> {
        return repository.getEvent(eventId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    val filteredEvents: StateFlow<List<VolunteerEvent>> = combine(
        events,
        selectedDate
    ) { sortedEvents, date ->
        if (date.isNullOrEmpty()) {
            sortedEvents
        } else {
            sortedEvents.filter { it.date == date }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedDate(dateString: String) {
        _selectedDate.value = dateString
    }

    fun clearDateFilter() {
        _selectedDate.value = null
    }

    fun applyEvent(userId: String, eventId: String) {
        viewModelScope.launch {
            repository.applyEvent(userId, eventId)
        }
    }

    fun deleteEventAndHistory(event: VolunteerEvent, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEvent(event)
            repository.deleteEventHistory(event.firestoreId)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun getVolunteerProfile(userId: String) {
        viewModelScope.launch {
            repository.getVolunteerProfile(userId).collect { profile ->
                profile?.let { _volunteer.value = it }
            }
        }
    }

    fun updatePhoneNumber(phone: String) {
        _volunteer.value = _volunteer.value.copy(phoneNum = phone)
    }

    fun updateEmergencyContact(name: String) {
        _volunteer.value = _volunteer.value.copy(emgContact = name)
    }

    fun updateEmergencyPhone(phone: String) {
        _volunteer.value = _volunteer.value.copy(emgNum = phone)
    }

    fun submitRegistration(userId: String) {
        viewModelScope.launch {
            try {
                val completeRegistration = _volunteer.value.copy(userId = userId)
                repository.saveVolunteerProfile(completeRegistration)
            } catch (e: Exception) {
                Log.e("VolunteerRegister", "Failed to submit registration", e)
            }
        }
    }

    // Validate
    fun validatePhoneNumber(phone: String): Boolean {
        return phone.length >= 10 && phone.all { it.isDigit() }
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