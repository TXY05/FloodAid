package com.example.floodaid.screen.map_UI

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.floodaid.roomDatabase.Repository.ProfileRepository

class SOSViewModelFactory(
    private val application: Application,
    private val profileRepository: ProfileRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SOSViewModel::class.java)) {
            return SOSViewModel(application, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}