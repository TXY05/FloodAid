package com.example.floodaid.roomDatabase.Repository

import android.util.Log
import com.example.floodaid.models.UserProfile
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import com.example.floodaid.roomDatabase.dao.UserProfileDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ProfileRepository(
    internal val userProfileDao: UserProfileDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    internal val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    suspend fun syncProfileFromFirebase(userId: String) {
        try {
            userProfileDao.clear()
            val doc = firestore.collection("users").document(userId).get().await()
            val userProfile = doc.toObject(UserProfile::class.java)
            userProfile?.let { userProfileDao.insert(it) }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Sync failed", e)
        }
    }

    fun getProfile(uid: String): Flow<UserProfile?> {
        return userProfileDao.getProfile(uid)
    }

    suspend fun updateProfile(profile: UserProfile){
        return try {
            firestore.collection("users").document(profile.uid).set(profile).await()
            userProfileDao.update(profile)

        } catch (e: Exception) {
            Log.e("UserProfileRepo", "Update failed", e)
            throw e
        }
    }
}