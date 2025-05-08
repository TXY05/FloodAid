package com.example.floodaid.screen.volunteer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.floodaid.roomDatabase.repository.VolunteerRepository
import com.google.firebase.auth.FirebaseAuth

class VolunteerViewModelFactory(
    private val repository: VolunteerRepository,
    private val auth: FirebaseAuth
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VolunteerViewModel::class.java)) {
            return VolunteerViewModel(repository, auth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}