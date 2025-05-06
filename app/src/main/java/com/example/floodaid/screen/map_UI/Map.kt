package com.example.floodaid.screen.map_UI

import BottomBar
import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Shader
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import com.example.floodaid.ui.theme.Red
import com.example.floodaid.screen.map_UI.MapUiState
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.delay
import com.example.floodaid.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.floodaid.roomDatabase.Entities.District
import com.example.floodaid.roomDatabase.Entities.Shelter
import com.example.floodaid.utils.DistanceCalculator
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CircleOptions
import com.example.floodaid.utils.vectorToBitmap
import com.example.floodaid.utils.BitmapParameters
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.AdvancedMarker
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.clustering.ClusterItem
import com.example.floodaid.roomDatabase.Entities.FloodMarker

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.lifecycle.LifecycleOwner
import com.example.floodaid.repository.FirestoreRepository
import com.example.floodaid.roomDatabase.Database.FloodAidDatabase
import com.example.floodaid.roomDatabase.Repository.MapRepository
import com.example.floodaid.utils.FloodClusterItem

// Cluster and Maintain Marker ratio
//// Marker size and zoom limit constants
private const val SHELTER_MIN_ZOOM = 10f
private const val SHELTER_MAX_ZOOM = 22f
private const val FLOOD_MIN_ZOOM = 8f
private const val FLOOD_MAX_ZOOM = 22f
private const val FLOOD_CLUSTER_ZOOM = 12f
private const val SHELTER_MARKER_SIZE = 150f // Consistent size for shelter markers
private const val FLOOD_MARKER_SIZE = 100f   // Consistent size for flood markers

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
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Map(
    navController: NavHostController,
    viewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(
            application = LocalContext.current.applicationContext as Application,
            repository = MapRepository(
                dao = FloodAidDatabase.getInstance(LocalContext.current.applicationContext as Application).MapDao(),
                FirestoreRepository = FirestoreRepository()
            )
        )
    )
) {
    // State and References
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentLocation by viewModel.currentLocation.collectAsState()
    var map by remember { mutableStateOf<GoogleMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var currentPolygon by remember { mutableStateOf<Polygon?>(null) }
    val markerMap = remember { mutableStateMapOf<Long, Marker>() }
    var currentZoom by remember { mutableFloatStateOf(7f) } // Default zoom
    var clusterManager by remember { mutableStateOf<ClusterManager<FloodClusterItem>?>(null) }

    val sheetState = rememberModalBottomSheetState()
    val showBottomSheet by viewModel.selectedShelter.collectAsState()

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    // Camera Position
    var savedCameraPosition by rememberSaveable(stateSaver = CameraPositionSaver) {
        mutableStateOf(CameraPosition(LatLng(4.2105, 101.9758), 7f, 0f, 0f))
    }

    // Add a state to track if we need to show markers
    var shouldShowMarkers by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startLocationUpdates()
            // Refresh map to enable location layer
            mapView?.getMapAsync { googleMap ->
                googleMap.isMyLocationEnabled = true
            }
        }
    }

    // Initialize Data
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

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.startLocationUpdates()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadStates()
    }

    // Handle District Selection
    LaunchedEffect(uiState.selectedDistrict) {
        uiState.selectedDistrict?.let { district ->
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
                            district.borderCoordinates?.let { border ->
                                currentPolygon = googleMap.addPolygon(
                                    PolygonOptions()
                                        .addAll(border.coordinates.map { LatLng(it[0], it[1]) })
                                        .strokeColor(Color.Red.toArgb())
                                        .fillColor(Color.Red.copy(alpha = 0.2f).toArgb())
                                )
                            }
                            viewModel.loadDistrictData(district.id)
                        }

                        override fun onCancel() {}
                    }
                )
            }
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
                            if (item.getFloodStatus() == "flood")
                                R.drawable.icon // Use a default warning icon
                            else
                                R.drawable.icon, // Use a default check circle icon
                            FLOOD_MARKER_SIZE.toInt(),
                            if (item.getFloodStatus() == "flood")
                                Color.Red.copy(alpha = 0.8f).toArgb() // Increased alpha for better visibility
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

    // Add camera change listener to update currentZoom
//    LaunchedEffect(Unit) {
//        map?.setOnCameraMoveListener {
//            currentZoom = map?.cameraPosition?.zoom ?: 7f
//        }
//    }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDestroy()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationUpdates()
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
                                    if (uiState.selectedState?.id != state.id) {
                                        viewModel.onStateSelected(state)
                                    }
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
                            onClick = { scope.launch { drawerState.open() }; viewModel.syncData() }
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
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    googleMap.isMyLocationEnabled = true
                                    googleMap.uiSettings.isMyLocationButtonEnabled = true
                                }
                                map = googleMap
                                with(googleMap.uiSettings) {
                                    isZoomControlsEnabled = true
                                    isMyLocationButtonEnabled = true
                                }

                                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(savedCameraPosition))

                                // Set up camera idle listener to save position
                                googleMap.setOnCameraIdleListener {
                                    savedCameraPosition = googleMap.cameraPosition
                                }

                                // After initial position is set, check for user location
                                if (currentLocation != null) {
                                    // Move to User Location
                                    currentLocation?.let { loc ->
                                        googleMap.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(loc.latitude, loc.longitude),
                                                12f
                                            )
                                        )
                                    }
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
                        sheetState = sheetState
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
    canvas.drawCircle(centerX, centerY, circleRadius, paint)

    // Draw the pointer triangle
    val trianglePath = Path().apply {
        moveTo(centerX - circleRadius * 0.5f, centerY + circleRadius * 0.7f) // left base
        lineTo(centerX + circleRadius * 0.5f, centerY + circleRadius * 0.7f) // right base
        lineTo(centerX, size.toFloat()) // tip
        close()
    }
    canvas.drawPath(trianglePath, paint)

    // Draw the icon bitmap in the center of the circle
    val iconLeft = (centerX - circleRadius)
    val iconTop = (centerY - circleRadius)
    val iconRect = RectF(iconLeft, iconTop, iconLeft + circleRadius * 2, iconTop + circleRadius * 2)
    val iconPaint = android.graphics.Paint().apply { isAntiAlias = true }
    canvas.drawBitmap(scaledBitmap, null, iconRect, iconPaint)

    return outputBitmap
}
