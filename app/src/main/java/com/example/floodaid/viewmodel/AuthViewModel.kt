package com.example.floodaid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodaid.screen.login.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        // Check if the user is logged in or not (this can also be moved to the repository)
        val isAuthenticated = authRepository.auth.currentUser != null
        _authState.value = if (isAuthenticated) AuthState.Authenticated else AuthState.Unauthenticated
    }

    fun loginFunction(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or Password Can't Be Empty")
            return
        }

        _authState.value = AuthState.Loading
        authRepository.signInWithEmail(email, password) { authState ->
            _authState.value = authState
        }
    }

    fun signupFunction(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or Password Can't Be Empty")
            return
        }

        _authState.value = AuthState.Loading
        authRepository.signUpWithEmail(email, password) { authState ->
            _authState.value = authState
        }
    }

    fun signoutFunction() {
        authRepository.auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun signInWithGoogle() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.signInWithGoogle { authState ->
                _authState.value = authState
            }
        }
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}