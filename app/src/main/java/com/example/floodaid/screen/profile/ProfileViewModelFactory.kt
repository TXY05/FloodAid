package com.example.floodaid.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.floodaid.roomDatabase.Repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth

class ProfileViewModelFactory(
    private val repository: ProfileRepository,
    private val auth: FirebaseAuth
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(repository, auth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}