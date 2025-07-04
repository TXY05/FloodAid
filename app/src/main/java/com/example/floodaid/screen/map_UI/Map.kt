package com.example.floodaid.screen.map_UI

import BottomBar
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.example.floodaid.R
import com.example.floodaid.utils.FloodClusterItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import com.example.floodaid.screen.map_UI.SOSViewModel

// Cluster and Maintain Marker ratio
//// Marker size and zoom limit constants
private const val SHELTER_MIN_ZOOM = 10f
private const val FLOOD_MIN_ZOOM = 9.5f
private const val FLOOD_CLUSTER_ZOOM = 10f
private const val SHELTER_MARKER_SIZE = 160f // Consistent size for shelter markers
private const val FLOOD_MARKER_SIZE = 120f   // Consistent size for flood markers

val CameraPositionSaver = run {
    val latLngSaver = Saver<LatLng, List<Double>>(
        save = { listOf(it.latitude, it.longitude) },
        restore = { LatLng(it[0], it[1]) }
    )

    Saver<CameraPosition, Map<String, Any>>(
        save = {
            mapOf(
                "target" to with(latLngSaver) { save(it.target)!! },
                "zoom" to it.zoom,
                "tilt" to it.tilt,
                "bearing" to it.bearing
            )
        },
        restore = {
            CameraPosition(
                with(latLngSaver) { restore(it["target"] as List<Double>)!! },
                it["zoom"] as Float,
                it["tilt"] as Float,
                it["bearing"] as Float
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "PotentialBehaviorOverride")
@Composable
fun Map(
    navController: NavHostController,
    viewModel: MapViewModel,
    sosViewModel: SOSViewModel? = null
) {
    // State and References
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isSOSActive = sosViewModel?.isSOSActive?.collectAsState()?.value ?: false

    var map by remember { mutableStateOf<GoogleMap?>(null) }
    var currentPolygon by remember { mutableStateOf<Polygon?>(null) }
    var sosCircle by remember { mutableStateOf<Circle?>(null) }
    var sosCircleAnimationJob by remember { mutableStateOf<Job?>(null) }

    var markerMap = remember { mutableStateMapOf<Long, Marker>() }
    var currentZoom by remember { mutableFloatStateOf(7f) } // Default zoom
    var clusterManager by remember { mutableStateOf<ClusterManager<FloodClusterItem>?>(null) }

    val showBottomSheet by viewModel.selectedShelter.collectAsState()
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    // Camera Position
    var savedCameraPosition by rememberSaveable(stateSaver = CameraPositionSaver) {
        mutableStateOf(CameraPosition(LatLng(4.2105, 101.9758), 7f, 0f, 0f))
    }

    var savedDistrict by rememberSaveable { mutableStateOf<Long?>(null) }
    var savedBorderCoordinates by rememberSaveable { mutableStateOf<List<List<Double>>?>(null) }
    var needsRestore by rememberSaveable { mutableStateOf(false) }

    // Add permission state tracking
    var locationPermissionGranted by rememberSaveable { mutableStateOf(
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    )}

    val mapViewSaver = run {
        Saver<MapView, Bundle>(
            save = { mapView ->
                Bundle().apply {
                    // Save minimal state (camera position is handled separately)
                    mapView.onSaveInstanceState(this)
                }
            },
            restore = { bundle ->
                MapView(context, GoogleMapOptions().mapId(context.getString(R.string.map_id))).apply {
                    onCreate(bundle)
                }
            }
        )
    }

// 2. Use rememberSaveable with the saver
    var mapView = rememberSaveable(saver = mapViewSaver) {
        MapView(context, GoogleMapOptions().mapId(context.getString(R.string.map_id))).apply {
            onCreate(null)
            Log.d("MapDebug", "Creating NEW MapView instance")
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            viewModel.startLocationUpdates()
            // We'll handle map updates in LaunchedEffect
        }
    }

    // Initialize Data
    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.startLocationUpdates()
        }

        // Load states but don't restore district yet (we'll do it when map is ready)
        if (savedDistrict == null) {
            viewModel.loadStates()
        }
    }

    // Apply location settings when both map is ready AND permission is granted
    LaunchedEffect(map, locationPermissionGranted) {
        if (map != null && locationPermissionGranted) {
            Log.d("MapDebug", "Map is ready and location permission granted - enabling location features")
            try {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } catch (e: SecurityException) {
                Log.e("MapDebug", "Security exception enabling location: ${e.message}")
            }
        }
    }

    // Specialized LaunchedEffect to handle district restoration ONLY after map is available
    LaunchedEffect(map) {
        if (map != null && savedDistrict != null) {
            Log.d("MapDebug", "Map is ready, now restoring saved district: $savedDistrict")
            needsRestore = true // Set flag to true when restoring from saved district
            viewModel.restoreDistrict(savedDistrict!!)
        }
    }

    // Collect district with border updates
    val districtWithBorder = viewModel.districtWithBorderFlow.collectAsState(initial = null)

    // Force redraw when we get border data update from Firestore
    LaunchedEffect(districtWithBorder.value, map) {
        districtWithBorder.value?.let { district ->
            if (district.borderCoordinates != null && map != null) {
                // Save coordinates for future use
                savedBorderCoordinates = district.borderCoordinates.coordinates

                // Only redraw if we need to restore
                if (needsRestore) {
                    Log.d("MapDebug", "Map is ready and we have border data, drawing polygon with ${district.borderCoordinates.coordinates.size} points")
                    // Redraw the polygon immediately
                    // Remove existing polygon if any
                    currentPolygon?.remove()

                    try {
                        val polygonOptions = PolygonOptions()
                            .addAll(district.borderCoordinates.coordinates.map { LatLng(it[0], it[1]) })
                            .strokeColor(getColor(context, R.color.marker_border))
                            .strokeWidth(5f)
                            .fillColor(getColor(context, R.color.marker_fill) and 0x1AFFFFFF)

                        currentPolygon = map?.addPolygon(polygonOptions)

                        // Apply dash pattern
                        currentPolygon?.strokePattern = listOf(
                            Dash(20f),
                            Gap(20f)
                        )

                        // Reset the flag after successful restoration
                        needsRestore = false
                        Log.d("MapDebug", "Successfully drew polygon for district ${district.id}")
                    } catch (e: Exception) {
                        Log.e("MapDebug", "Error in border update polygon drawing: ${e.message}", e)
                    }
                }
            }
        }
    }

    // Handle District Selection
    LaunchedEffect(uiState.selectedDistrict) {
        uiState.selectedDistrict?.let { district ->
            Log.d("MapDebug", "District selected: ${district.id}")

            // Always save the district ID when a district is selected
            savedDistrict = district.id

            // Reset any pending restore when manually selecting a district
            needsRestore = false

            // If this district has border coordinates, save them
            district.borderCoordinates?.let { borders ->
                savedBorderCoordinates = borders.coordinates
            }

            map?.let { googleMap ->
                // Clear previous markers and polygons
                markerMap.values.forEach { it.remove() }
                markerMap.clear()
                currentPolygon?.remove()
                clusterManager?.clearItems()

                // Move camera
                val districtLatLng = LatLng(district.latitude, district.longitude)

                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(districtLatLng, 10f),
                    object : GoogleMap.CancelableCallback {
                        override fun onFinish() {
                            // Draw border after camera movement
                            val borderCoordinates = district.borderCoordinates?.coordinates
                            val savedCoords = savedBorderCoordinates

                            val coordsToUse = borderCoordinates ?: savedCoords

                            if (coordsToUse != null) {
                                Log.d("MapDebug", "Drawing polygon with ${coordsToUse.size} points")
                                // Save border coordinates for future use
                                savedBorderCoordinates = coordsToUse

                                try {
                                    val polygonOptions = PolygonOptions()
                                        .addAll(coordsToUse.map { LatLng(it[0], it[1]) })
                                        .strokeColor(getColor(context, R.color.marker_border))
                                        .strokeWidth(5f) // Make the border slightly thicker
                                        .fillColor(getColor(context, R.color.marker_fill) and 0x1AFFFFFF) // Make fill very transparent (10% opacity)

                                    currentPolygon = googleMap.addPolygon(polygonOptions)

                                    // Apply dash pattern to the polygon
                                    currentPolygon?.let { polygon ->
                                        val pattern = listOf(
                                            Dash(20f),  // 20 pixels dash
                                            Gap(20f)   // 20 pixels gap
                                        )
                                        polygon.strokePattern = pattern
                                    }
                                } catch (e: Exception) {
                                    Log.e("MapDebug", "Error drawing polygon: ${e.message}", e)
                                }
                            } else {
                                Log.d("MapDebug", "No border coordinates available for district")
                            }
                            // Load marker data for this district
                            viewModel.loadDistrictData(district.id)
                        }

                        override fun onCancel() {
                            Log.d("MapDebug", "Camera animation cancelled")
                        }
                    }
                )
            } ?: Log.e("MapDebug", "Google Map is null")
        }
    }

    // Update Markers
    LaunchedEffect(uiState.currentMarkers, uiState.currentShelters, currentZoom) {
        map?.let { googleMap ->
            // Clear previous markers
            markerMap.values.forEach { it.remove() }
            markerMap.clear()
            clusterManager?.clearItems()

            // Initialize cluster manager if needed
            if (clusterManager == null) {
                val newClusterManager = ClusterManager<FloodClusterItem>(context, googleMap)
                newClusterManager.renderer = object : DefaultClusterRenderer<FloodClusterItem>(context, googleMap, newClusterManager) {
                    override fun onBeforeClusterItemRendered(item: FloodClusterItem, markerOptions: MarkerOptions) {
                        // Create teardrop marker based on flood status
                        val floodIcon = createTeardropMarker(
                            context,
                            if (item.getFloodStatus() == "flooded")
                                R.drawable.flood_icon // Use a default warning icon
                            else
                                R.drawable.safe_icon, // Use a default check circle icon
                            FLOOD_MARKER_SIZE.toInt(),
                            if (item.getFloodStatus() == "flooded")
                                Color.Blue.copy(alpha = 0.8f).toArgb() // Increased alpha for better visibility
                            else
                                Color.Green.copy(alpha = 0.8f).toArgb() // Increased alpha for better visibility
                        )
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(floodIcon))
                    }

                    override fun shouldRenderAsCluster(cluster: com.google.maps.android.clustering.Cluster<FloodClusterItem>): Boolean {
                        return currentZoom < FLOOD_CLUSTER_ZOOM
                    }
                }
                googleMap.setOnCameraIdleListener {
                    newClusterManager.onCameraIdle()
                    currentZoom = googleMap.cameraPosition.zoom
                }
                clusterManager = newClusterManager
            }

            // Add Flood Markers with clustering (only if within zoom range)
            if (currentZoom >= FLOOD_MIN_ZOOM) {
                uiState.currentMarkers.forEach { marker ->
                    val position = LatLng(marker.latitude, marker.longitude)
                    clusterManager?.addItem(FloodClusterItem(marker, position))
                }
                clusterManager?.cluster()
            }

            // Add Shelter Markers (only if within zoom range)
            if (currentZoom >= SHELTER_MIN_ZOOM) {
                uiState.currentShelters.forEach { shelter ->
                    val shelterIcon = createTeardropMarker(
                        context,
                        R.drawable.icon,
                        SHELTER_MARKER_SIZE.toInt(),
                        Color(0xFF4CAF50).toArgb(),
                        isShelter = true
                    )
                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(shelter.latitude, shelter.longitude))
                            .title(shelter.helpCenterName)
                            .snippet("Tap for details")
                            .icon(BitmapDescriptorFactory.fromBitmap(shelterIcon))
                    )
                    marker?.let {
                        it.tag = shelter.id
                        markerMap[shelter.id] = it
                    }
                }
            }

            // Set ONE global click listener for all markers
            googleMap.setOnMarkerClickListener { clickedMarker ->
                // First try to handle cluster marker click
                if (clusterManager?.onMarkerClick(clickedMarker) == true) {
                    return@setOnMarkerClickListener true
                }

                // Then handle shelter marker click
                val shelterId = clickedMarker.tag as? Long
                shelterId?.let { id ->
                    uiState.currentShelters.find { it.id == id }?.let { shelter ->
                        clickedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        viewModel.onMarkerSelected(id)
                        viewModel.onShelterSelected(shelter)
                    }
                }
                true
            }
        }
    }

    // Handle reset of marker appearance when bottom sheet is dismissed
    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet == null) {
            // Get the previously selected marker ID
            val markerId = viewModel.selectedMarkerId.value
            markerId?.let { id ->
                map?.let { googleMap ->
                    val shelterIcon = createTeardropMarker(
                        context,
                        R.drawable.icon,
                        SHELTER_MARKER_SIZE.toInt(),
                        Color(0xFF4CAF50).toArgb(),
                        isShelter = true
                    )
                    markerMap[id]?.setIcon(BitmapDescriptorFactory.fromBitmap(shelterIcon))
                }
            }
            viewModel.clearSelectedMarker()
        }
    }

    // Restore polygon when map becomes available
    LaunchedEffect(map, savedBorderCoordinates) {
        // Only restore polygon if map is ready and we have coordinates
        if (map != null && savedBorderCoordinates != null && needsRestore) {
            Log.d("MapDebug", "Map is ready, redrawing polygon with saved coordinates")
            try {
                // Remove any existing polygon
                currentPolygon?.remove()

                // Create a new polygon with saved coordinates
                val polygonOptions = PolygonOptions()
                    .addAll(savedBorderCoordinates!!.map { LatLng(it[0], it[1]) })
                    .strokeColor(getColor(context, R.color.marker_border))
                    .strokeWidth(5f)
                    .fillColor(getColor(context, R.color.marker_fill) and 0x1AFFFFFF)

                currentPolygon = map!!.addPolygon(polygonOptions)

                // Apply dash pattern
                currentPolygon?.strokePattern = listOf(
                    Dash(20f),
                    Gap(20f)
                )

                Log.d("MapDebug", "Successfully restored polygon with ${savedBorderCoordinates!!.size} points")

                // Reset the flag after successful restoration
                needsRestore = false
            } catch (e: Exception) {
                Log.e("MapDebug", "Error redrawing polygon: ${e.message}", e)
            }
        }
    }

    // SOS Circle Animation Effect
    LaunchedEffect(isSOSActive, map, currentLocation) {
        // Clean up existing animation if running
        sosCircleAnimationJob?.cancel()
        sosCircle?.remove()
        sosCircle = null

        // If SOS is active and we have map and location, show animation
        if (isSOSActive && map != null && currentLocation != null) {
            sosCircleAnimationJob = scope.launch {
                // Animation parameters
                val minRadius = 30.0  // meters
                val maxRadius = 150.0 // meters
                val animationDuration = 1500 // milliseconds

                while (isActive) {
                    // Create initial circle
                    sosCircle?.remove()
                    sosCircle = map?.addCircle(
                        CircleOptions()
                            .center(currentLocation!!)
                            .radius(minRadius)
                            .strokeWidth(4f)
                            .strokeColor(Color.Red.copy(alpha = 0.8f).toArgb())
                            .fillColor(Color.Red.copy(alpha = 0.3f).toArgb())
                    )

                    // Animate radius expansion
                    val startTime = System.currentTimeMillis()
                    while (System.currentTimeMillis() - startTime < animationDuration) {
                        val progress = (System.currentTimeMillis() - startTime).toFloat() / animationDuration
                        val currentRadius = minRadius + (maxRadius - minRadius) * progress
                        val currentAlpha = 0.8f * (1f - progress)
                        val currentFillAlpha = 0.3f * (1f - progress)

                        sosCircle?.radius = currentRadius
                        sosCircle?.strokeColor = Color.Red.copy(alpha = currentAlpha).toArgb()
                        sosCircle?.fillColor = Color.Red.copy(alpha = currentFillAlpha).toArgb()

                        delay(16) // roughly 60fps
                    }

                    // Remove circle at end of animation
                    sosCircle?.remove()
                    sosCircle = null
                    delay(100) // small pause between animations
                }
            }
        }
    }

    // Handle proper cleanup of map resources
    DisposableEffect(lifecycleOwner) {
        onDispose {
            Log.d("MapDebug", "Disposing map resources")
            // Don't clear the map - just destroy mapView
            sosCircleAnimationJob?.cancel()
            sosCircle?.remove()
            mapView?.onDestroy()
            viewModel.stopLocationUpdates()

            // Ensure we'll need to restore next time
            if (savedDistrict != null) {
                needsRestore = true
            }
        }
    }

// Drawer Content
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
                Text(
                    text = if (uiState.selectedState == null) "Select State"
                    else "Districts in ${uiState.selectedState?.name}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )

                if (uiState.selectedState == null) {
                    // State List
                    LazyColumn {
                        items(uiState.states.distinctBy { it.id }) { state ->
                            TextButton(
                                onClick = {
                                    viewModel.onStateSelected(state)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(state.name)
                            }
                            HorizontalDivider()
                        }
                    }
                } else {
                    // District List
                    LazyColumn {
                        items(uiState.districts.distinctBy { it.id }) { district ->
                            val isSelected = uiState.selectedDistrict?.id == district.id

                            TextButton(
                                onClick = {
                                    if (!isProcessing && !isSelected) {
                                        isProcessing = true
                                        viewModel.onDistrictSelected(district)
                                        scope.launch {
                                            drawerState.close()
                                            delay(200)
                                            isProcessing = false
                                        }
                                        Log.d("MapDebug", "Try To save data")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                                        else Color.Transparent
                                    )
                            ) {
                                Text(district.name)
                            }
                            HorizontalDivider()
                        }

                        item {
                            TextButton(
                                onClick = { viewModel.clearSelectedState() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("← Back to States")
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = { BottomBar(navController = navController) },
            topBar = {
                TopAppBar(
                    title = { Text("FloodAid Map") },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        MapView(
                            context,
                            GoogleMapOptions().mapId(context.getString(R.string.map_id))
                        ).apply {
                            onCreate(null)
                            getMapAsync { googleMap ->
                                map = googleMap
                                with(googleMap.uiSettings) {
                                    isZoomControlsEnabled = true
                                    isMyLocationButtonEnabled = true
                                }

                                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(savedCameraPosition))

                                // Set up camera idle listener to save position
                                googleMap.setOnCameraMoveListener {
                                    savedCameraPosition = googleMap.cameraPosition
                                }
                            }
                        }.also { mapView = it }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    update = { view ->
                        when (lifecycleOwner.lifecycle.currentState) {
                            Lifecycle.State.CREATED -> view.onCreate(null)
                            Lifecycle.State.STARTED -> view.onStart()
                            Lifecycle.State.RESUMED -> view.onResume()
                            Lifecycle.State.DESTROYED -> view.onDestroy()
                            else -> {}
                        }
                    }
                )
                if (showBottomSheet != null) {
                    ShelterDetailsBottomSheet(
                        shelter = showBottomSheet,
                        currentLocation = currentLocation,
                        onDismiss = {
                            viewModel.onShelterSelected(null)
                            viewModel.clearSelectedShelter()
                        },
                    )
                }
            }
        }
    }
}

// Add this function to create teardrop-shaped markers
private fun createTeardropMarker(context: Context, drawableId: Int, size: Int, backgroundColor: Int, isShelter: Boolean = false): Bitmap {
    val pointerHeight = size * 0.22f // Height of the pointer triangle
    val circleRadius = (size / 2f) - pointerHeight / 2f
    val centerX = size / 2f
    val centerY = size / 2f - pointerHeight / 4f
    val borderWidth = size * 0.03f

    val originalBitmap = try {
        BitmapFactory.decodeResource(context.resources, drawableId)
    } catch (e: Exception) {
        Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            val paint = android.graphics.Paint().apply {
                color = Color.White.toArgb()
                isAntiAlias = true
            }
            canvas.drawCircle(centerX, centerY, circleRadius, paint)
        }
    }
    val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, (circleRadius * 2).toInt(), (circleRadius * 2).toInt(), false)

    val outputBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outputBitmap)

    // Draw the main circle
    val paint = android.graphics.Paint().apply {
        color = backgroundColor
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
    }

    // Draw circle border
    val borderPaint = android.graphics.Paint().apply {
        color = Color.White.toArgb() // White border
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = borderWidth
    }

    // Draw the pointer triangle
    val trianglePath = Path().apply {
        moveTo(centerX - circleRadius * 0.5f, centerY + circleRadius * 0.7f) // left base
        lineTo(centerX + circleRadius * 0.5f, centerY + circleRadius * 0.7f) // right base
        lineTo(centerX, size.toFloat()) // tip
        close()
    }

    // Draw Pointer Triangle
    canvas.drawPath(trianglePath, paint)
    // Draw triangle border
    canvas.drawPath(trianglePath, borderPaint)
    // Draw Main Circle
    canvas.drawCircle(centerX, centerY, circleRadius, paint)
    // Draw Circle Border
    canvas.drawCircle(centerX, centerY, circleRadius, borderPaint)

    // Draw the icon bitmap in the center of the circle
    val iconLeft = (centerX - circleRadius)
    val iconTop = (centerY - circleRadius)
    val iconRect = RectF(iconLeft, iconTop, iconLeft + circleRadius * 2, iconTop + circleRadius * 2)
    val iconPaint = android.graphics.Paint().apply { isAntiAlias = true }
    canvas.drawBitmap(scaledBitmap, null, iconRect, iconPaint)

    return outputBitmap
}
