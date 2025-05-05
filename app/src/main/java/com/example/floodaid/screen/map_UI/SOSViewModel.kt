package com.example.floodaid.screen.map_UI

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SOSViewModel : ViewModel() {
    private val _isSOSActive = MutableStateFlow(false)
    val isSOSActive: StateFlow<Boolean> = _isSOSActive.asStateFlow()

    fun setSOSState(isActive: Boolean) {
        _isSOSActive.value = isActive
    }
}