package com.example.floodaid.utils

import android.content.Context
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Suppress("DEPRECATION")
class GeocodingHelper(private val context: Context) {
    private val geocoder = Geocoder(context, Locale.getDefault())

    suspend fun getAddress(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()?.toAddressString()
            } else {
                geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()?.toAddressString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun android.location.Address.toAddressString(): String {
        return listOfNotNull(
            thoroughfare,  // Street
            subLocality,   // Neighborhood
            locality,      // City
            adminArea,     // State
            postalCode     // ZIP
        ).joinToString(", ")
    }
}