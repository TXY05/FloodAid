package com.example.floodaid.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodaid.models.UserProfile
import com.example.floodaid.roomDatabase.Repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    internal val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid)

    val profile: StateFlow<UserProfile?> = _currentUserId
        .flatMapLatest { uid ->
            if (uid != null) {
                repository.userProfileDao.getProfile(uid)
            } else {
                flowOf(null)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _authState = MutableStateFlow(auth.currentUser != null)
    val authState: StateFlow<Boolean> = _authState

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser != null
            if (firebaseAuth.currentUser != null) {
                syncProfile()
            }
        }
    }

    fun syncProfile() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _currentUserId.value = uid
            repository.syncProfileFromFirebase(uid)
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            try {
                repository.updateProfile(profile)
            } catch (e: Exception) {
                // Optionally handle update error
            }
        }
    }
}
