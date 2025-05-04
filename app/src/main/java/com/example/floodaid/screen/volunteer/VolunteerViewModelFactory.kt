package com.example.floodaid.screen.volunteer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.floodaid.roomDatabase.Repository.VolunteerRepository

class VolunteerViewModelFactory(
    private val repository: VolunteerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VolunteerViewModel::class.java)) {
            return VolunteerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}