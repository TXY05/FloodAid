package com.example.floodaid.roomDatabase.Repository

import android.util.Log
import com.example.floodaid.models.UserLocation
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

    // Add methods for location tracking
    // Simplified location tracking
    suspend fun updateUserLocation(userLocation: UserLocation) {
        try {
            // Update Room database
            userProfileDao.insertUserLocation(userLocation)

            // Update Firestore
            firestore.collection("user_locations")
                .document(userLocation.uid)
                .set(userLocation)
                .await()

            Log.d("LocationUpdate", "Updated location for user ${userLocation.uid}: " +
                    "lat=${userLocation.latitude}, lng=${userLocation.longitude}")
        } catch (e: Exception) {
            Log.e("LocationUpdate", "Failed to update location", e)
        }
    }

    suspend fun getUserLocation(uid: String): UserLocation? {
        return try {
            userProfileDao.getUserLocation(uid)
        } catch (e: Exception) {
            Log.e("LocationUpdate", "Failed to get location", e)
            null
        }
    }

    suspend fun clearSOSLocation(uid: String) {
        try {
            // Clear from Room
            userProfileDao.clearUserLocation(uid)

            // Clear from Firestore
            firestore.collection("user_locations")
                .document(uid)
                .delete()
                .await()

            Log.d("LocationUpdate", "Cleared SOS location for user $uid")
        } catch (e: Exception) {
            Log.e("LocationUpdate", "Failed to clear location", e)
        }
    }
}