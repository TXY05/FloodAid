package com.example.floodaid.screen.login

import android.app.Application
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.floodaid.R
import com.example.floodaid.viewmodel.AuthState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.security.MessageDigest
import java.util.UUID

class AuthRepository(private val application: Application,val auth: FirebaseAuth) {


    // Email and password login
    fun signInWithEmail(email: String, password: String, onComplete: (AuthState) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(AuthState.Authenticated)
                } else {
                    onComplete(AuthState.Error(task.exception?.message ?: "Login Failed"))
                }
            }
    }

    // Email and password signup
    fun signUpWithEmail(email: String, password: String, onComplete: (AuthState) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(AuthState.Authenticated)
                } else {
                    onComplete(AuthState.Error(task.exception?.message ?: "Signup Failed"))
                }
            }
    }
    // Helper function to create a nonce for Google Sign-In
    fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold("") { str, it ->
            str + "%02x".format(it)
        }
    }

    // Function to handle Google Sign-In, requires Context
    suspend fun signInWithGoogle(onAuthStateUpdated: (AuthState) -> Unit) {
        // Google ID sign-in logic here
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(application.getString(R.string.web_client_id))
            .setAutoSelectEnabled(false)
            .setNonce(createNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(application)

        try {
            credentialManager.getCredential(application, request).apply {
                val credential = credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    val firebaseCredential = GoogleAuthProvider.getCredential(
                        googleIdTokenCredential.idToken,
                        null
                    )

                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onAuthStateUpdated(AuthState.Authenticated)
                            } else {
                                onAuthStateUpdated(AuthState.Error(task.exception?.message ?: "Login Failed"))
                            }
                        }
                }
            }
        } catch (e: Exception) {
            onAuthStateUpdated(AuthState.Error(e.message ?: "Something went wrong"))
        }
    }
}
