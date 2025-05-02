package com.example.floodaid.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng

object DistanceCalculator {
    /**
     * Calculate distance between two coordinates in kilometers
     * @return Distance in km (Float)
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000f  // Convert meters to kilometers
    }

    /**
     * Extension function for LatLng objects
     */
    fun calculateDistance(start: LatLng, end: LatLng): Float {
        return calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude)
    }
}