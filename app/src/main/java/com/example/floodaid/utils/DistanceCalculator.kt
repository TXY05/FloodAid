package com.example.floodaid.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng

object DistanceCalculator {

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

    fun calculateDistance(start: LatLng, end: LatLng): Float {
        return calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude)
    }
}