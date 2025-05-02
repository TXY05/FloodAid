package com.example.floodaid.screen.map_UI

import BottomBar
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.floodaid.roomDatabase.Entities.District
import com.example.floodaid.roomDatabase.Entities.Shelter
import com.example.floodaid.utils.DistanceCalculator
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CircleOptions

//val CameraPositionSaver = run {
//    val latLngSaver = Saver<LatLng, List<Double>>(
//        save = { listOf(it.latitude, it.longitude) },
//        restore = { LatLng(it[0], it[1]) }
//    )
//
//    Saver<CameraPosition, Map<String, Any>>(
//        save = {
//            mapOf(
//                "target" to with(latLngSaver) { save(it.target)!! },
//                "zoom" to it.zoom,
//                "tilt" to it.tilt,
//                "bearing" to it.bearing
//            )
//        },
//        restore = {
//            CameraPosition(
//                with(latLngSaver) { restore(it["target"] as List<Double>)!! },
//                it["zoom"] as Float,
//                it["tilt"] as Float,
//                it["bearing"] as Float
//            )
//        }
//    )
//}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Map(
    navController: NavHostController,
    viewModel: MapViewModel = viewModel()
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

    val sheetState = rememberModalBottomSheetState()
    val showBottomSheet by viewModel.selectedShelter.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

//    // Camera Position
//    var savedCameraPosition by rememberSaveable(stateSaver = CameraPositionSaver) {
//        mutableStateOf(CameraPosition(LatLng(4.2105, 101.9758), 7f, 0f, 0f))
//    }

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
        if (showBottomSheet == null && sheetState.isVisible) {
            sheetState.hide() // Ensure sheet closes when shelter is null
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
    LaunchedEffect(uiState.currentMarkers, uiState.currentShelters) {
        map?.let { googleMap ->
            markerMap.values.forEach { it.remove() }
            markerMap.clear()

            if (googleMap.cameraPosition.zoom > 0) {
                // Add Flood Markers
                uiState.currentMarkers.forEach { marker ->
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(marker.latitude, marker.longitude))
                            .title("Flood: ${marker.floodStatus}")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )?.let { markerMap[marker.id] = it }
                }

                // Add Shelter Markers
                uiState.currentShelters.forEach { shelter ->
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(shelter.latitude, shelter.longitude))
                            .title(shelter.helpCenterName)
                            .snippet("Tap for details")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )?.let { marker ->
                        marker.tag = shelter.id // Store shelter ID in marker
                        markerMap[shelter.id] = marker
                    }
                }

                // Set ONE global click listener
                googleMap.setOnMarkerClickListener { clickedMarker ->
                    val shelterId = clickedMarker.tag as? Long
                    shelterId?.let { id ->
                        uiState.currentShelters.find { it.id == id }?.let { shelter ->
                            viewModel.onShelterSelected(shelter)
                        }
                    }
                    true // Always consume the event
                }
            }
        }
    }

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
            ModalDrawerSheet {
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
                                            delay(300)
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
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    googleMap.isMyLocationEnabled = true // This shows the blue dot
                                    googleMap.uiSettings.isMyLocationButtonEnabled = true // Shows the "locate me" button
                                }
                                map = googleMap
                                with(googleMap.uiSettings) {
                                    isZoomControlsEnabled = true
                                    isMyLocationButtonEnabled = true
                                }
                                //Move to save location
//                                googleMap.setOnCameraIdleListener {
//                                    savedCameraPosition = googleMap.cameraPosition
//                                }

                                //Move to User Location
                                currentLocation?.let { loc ->
                                    googleMap.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(loc.latitude, loc.longitude),
                                            12f
                                        )
                                    )
                                }
                            }
                        }.also { mapView = it }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    update = { view ->
                        // Handle lifecycle changes
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
                            viewModel.clearSelectedShelter() // Call ViewModel's method instead
                        },
                        sheetState = sheetState
                    )
                }
            }
        }
    }
}
