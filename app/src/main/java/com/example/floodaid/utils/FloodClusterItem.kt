package com.example.floodaid.utils

import com.example.floodaid.roomDatabase.Entities.FloodMarker
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class FloodClusterItem(
    private val marker: FloodMarker,
    private val position: LatLng
) : ClusterItem {
    override fun getPosition(): LatLng = position
    override fun getTitle(): String? = "Flood: ${marker.floodStatus}"
    override fun getSnippet(): String? = null

    // Add method to get flood status
    fun getFloodStatus(): String = marker.floodStatus
}