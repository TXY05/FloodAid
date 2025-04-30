package com.example.floodaid.screen.map_UI

import android.annotation.SuppressLint
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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polygon
//import com.example.floodaid.utils.DistanceCalculator
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Map(
    navController: NavHostController,
    viewModel: MapViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // State from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

    // Map references
    var map by remember { mutableStateOf<GoogleMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var currentPolygon by remember { mutableStateOf<Polygon?>(null) }
//    val markerMap = remember { mutableStateMapOf<Long, Marker>() } // Key: Entity ID, Value: Marker

    // Camera position
    var savedCameraPosition by rememberSaveable(stateSaver = CameraPositionSaver) {
        mutableStateOf(CameraPosition(LatLng(4.2105, 101.9758), 7f, 0f, 0f))
    }

    // Initialize data
    LaunchedEffect(Unit) {
        viewModel.loadStates()
    }
//
//    // Handle district selection
//    LaunchedEffect(uiState.selectedDistrict) {
//        uiState.selectedDistrict?.let { district ->
//            map?.let { googleMap ->
//                // Move camera
//                val districtLatLng = LatLng(district.latitude, district.longitude)
//                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(districtLatLng, 10f))
//
//                // Draw border
//                currentPolygon?.remove()
//                district.borderCoordinates?.let { border ->
//                    currentPolygon = googleMap.addPolygon(
//                        PolygonOptions()
//                            .addAll(border.coordinates.map { LatLng(it[0], it[1]) })
//                            .strokeColor(Color.Blue)
//                            .fillColor(0x320000FF)
//                    )
//                }
//
//                // Load district data
//                viewModel.loadDistrictData(district.id)
//            }
//        }
//    }
//
//    // Update markers when data changes
//    LaunchedEffect(uiState.currentMarkers, uiState.currentShelters) {
//        map?.let { googleMap ->
//            // Clear existing markers
//            markerMap.values.forEach { it.remove() }
//            markerMap.clear()
//
//            // Add flood markers
//            uiState.currentMarkers.forEach { marker ->
//                val markerObj = googleMap.addMarker(
//                    MarkerOptions()
//                        .position(LatLng(marker.latitude, marker.longitude))
//                        .title("Flood: ${marker.floodStatus}")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
//                )
//                markerObj?.let { markerMap[marker.id] = it }
//
//                // Check expiration
//                if (marker.isAboutToExpire()) {
//                    // TODO: Show expiration dialog
//                }
//            }
//
//            // Add shelters
//            uiState.currentShelters.forEach { shelter ->
//                val markerObj = googleMap.addMarker(
//                    MarkerOptions()
//                        .position(LatLng(shelter.latitude, shelter.longitude))
//                        .title(shelter.helpCenterName)
//                        .snippet(shelter.descriptions)
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//                )
//                markerObj?.let { markerMap[shelter.id] = it }
//            }
//        }
//    }
//
//    ModalNavigationDrawer(
//        drawerState = drawerState,
//        gesturesEnabled = drawerState.isOpen,
//        drawerContent = {
//            ModalDrawerSheet {
//                Text(
//                    text = if (uiState.selectedState == null) "Select State"
//                    else "Districts in ${uiState.selectedState?.name}",
//                    style = MaterialTheme.typography.titleMedium,
//                    modifier = Modifier.padding(16.dp)
//                )
//
//                if (uiState.selectedState == null) {
//                    // State list
//                    LazyColumn {
//                        items(uiState.states) { state ->
//                            TextButton(
//                                onClick = {
//                                    viewModel.onStateSelected(state)
//                                    scope.launch { drawerState.open() } // Keep drawer open
//                                },
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                Text(state.name)
//                            }
//                            Divider()
//                        }
//                    }
//                } else {
//                    // District list
//                    LazyColumn {
//                        items(uiState.districts) { district ->
//                            TextButton(
//                                onClick = {
//                                    viewModel.onDistrictSelected(district)
//                                    scope.launch { drawerState.close() }
//                                },
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                Text(district.name)
//                            }
//                            Divider()
//                        }
//
//                        item {
//                            TextButton(
//                                onClick = { viewModel.clearSelectedState() },
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                Text("← Back to States")
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    ) {
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { Text("FloodAid Map") },
//                    navigationIcon = {
//                        IconButton(
//                            onClick = { scope.launch { drawerState.open() } }
//                        ) {
//                            Icon(Icons.Default.Menu, contentDescription = "Menu")
//                        }
//                    }
//                )
//            },
//            floatingActionButton = {
//                ExtendedFloatingActionButton(
//                    onClick = { /* TODO: Add new marker/shelter */ },
//                    icon = { Icon(Icons.Default.Add, "Add") },
//                    text = { Text("Report Flood") }
//                )
//            }
//        ) { paddingValues ->
//            AndroidView(
//                factory = { context ->
//                    MapView(context).apply {
//                        onCreate(null)
//                        getMapAsync { googleMap ->
//                            map = googleMap
//                            googleMap.uiSettings.apply {
//                                isZoomControlsEnabled = true
//                                isMyLocationButtonEnabled = true
//                            }
//
//                            // Set initial position
//                            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(savedCameraPosition))
//                            googleMap.setOnCameraIdleListener {
//                                savedCameraPosition = googleMap.cameraPosition
//                            }
//
//                            // Set current location if available
//                            currentLocation?.let { location ->
//                                googleMap.animateCamera(
//                                    CameraUpdateFactory.newLatLngZoom(
//                                        LatLng(location.latitude, location.longitude),
//                                        12f
//                                    )
//                                )
//                            }
//                        }
//                    }.also { mapView = it }
//                },
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues),
//                update = { view ->
//                    mapView = view
//                }
//            )
//
//            // TODO: Add bottom sheet for marker details
//        }
//    }
}

//@Composable
//fun ShelterListOverlay(
//    shelters: List<Shelter>,
//    currentLocation: LatLng?,
//    onShelterSelected: (Shelter) -> Unit,
//    onAddNew: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        modifier = modifier
//            .fillMaxWidth()
//            .heightIn(max = 400.dp),
//        elevation = CardDefaults.cardElevation(8.dp)
//    ) {
//        Column {
//            Text(
//                "Nearby Shelters",
//                style = MaterialTheme.typography.titleLarge,
//                modifier = Modifier.padding(16.dp)
//            )
//
//            LazyColumn(modifier = Modifier.weight(1f)) {
//                items(shelters) { shelter ->
//                    ShelterItem(
//                        shelter = shelter,
//                        currentLocation = currentLocation,
//                        onClick = { onShelterSelected(shelter) }
//                    )
//                }
//            }
//
//            Button(
//                onClick = onAddNew,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp)
//            ) {
//                Text("Add New Shelter")
//            }
//        }
//    }
//}
//
//@Composable
//fun ShelterItem(
//    shelter: Shelter,
//    currentLocation: LatLng?,
//    onClick: () -> Unit
//) {
//    val distance = currentLocation?.let {
//        DistanceCalculator.calculateDistance(
//            it.latitude,
//            it.longitude,
//            shelter.latitude,
//            shelter.longitude
//        )
//    }
//
//    Card(
//        onClick = onClick,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 8.dp, vertical = 4.dp)
//    ) {
//        Column(modifier = Modifier.padding(8.dp)) {
//            Text(shelter.helpCenterName, style = MaterialTheme.typography.titleMedium)
//            Text("${shelter.districtName}, ${shelter.stateName}")
//            distance?.let {
//                Text("Distance: %.1f km".format(it))
//            }
//        }
//    }
//}
//
////fun showExpirationDialog(marker: FloodMarker, viewModel: MapViewModel) {
////    val newExpiryTime = marker.expiryTime.plus(Duration.ofDays(1)) // Example: extend by 1 day
////
////    AlertDialog(
////        onDismissRequest = { /* Close dialog */ },
////        title = { Text("Marker Expiring Soon") },
////        text = { Text("This flood marker will expire soon. Do you want to extend or remove it?") },
////        confirmButton = {
////            Button(onClick = {
////                viewModel.extendFloodMarker(marker.id, newExpiryTime)
////            }) {
////                Text("Extend")
////            }
////        },
////        dismissButton = {
////            Button(onClick = {
////                viewModel.deleteFloodMarker(marker.id)
////            }) {
////                Text("Delete")
////            }
////        }
////    )
////}
//
////// Check when displaying markers
////floodMarkers.value.forEach { marker ->
////    if (marker.isAboutToExpire()) {
////        showExpirationDialog(marker, viewModel)
////    }
////}

//@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//@Composable
//fun Map(navController: NavHostController, viewModel: MapViewModel = viewModel()) {
//    val drawerState = rememberDrawerState(DrawerValue.Closed)
//    val scope = rememberCoroutineScope()
//    var currentPolygon by remember { mutableStateOf<Polygon?>(null) }
//
//    // Collect state from ViewModel
//    val states by viewModel.states
//    val districts by viewModel.districts
//    val selectedState by viewModel.selectedState
//    val selectedDistrict by viewModel.selectedDistrict
//
//    // Map-related variables
//    val context = LocalContext.current
//    var map by remember { mutableStateOf<GoogleMap?>(null) }
//    var mapView by remember {
//        mutableStateOf(
//            MapView(context, GoogleMapOptions().mapId(context.getString(R.string.map_id))).apply {
//                onCreate(null)
//            }
//        )
//    }
//
//    var savedCameraPosition by rememberSaveable(stateSaver = CameraPositionSaver) {
//        mutableStateOf(
//            CameraPosition(
//                LatLng(4.2105, 101.9758), // Default position
//                7f, // Default zoom
//                0f,  // Default tilt
//                0f   // Default bearing
//            )
//        )
//    }
//
//    // Handle district selection changes
//    LaunchedEffect(selectedDistrict) {
//        selectedDistrict?.let { district ->
//            map?.let { googleMap ->
//                // Move camera to selected district
//                val districtLatLng = LatLng(district.latitude, district.longitude)
//                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(districtLatLng, 10f))
//
//
//                // Draw border polygon if available
//                district.borderCoordinates?.let { border ->
//                    val polygonPoints = border.coordinates.map { LatLng(it[0], it[1]) }
//                    currentPolygon?.remove()
//                    currentPolygon = googleMap.addPolygon(
//                        PolygonOptions()
//                            .addAll(polygonPoints)
//                            .strokeColor(0xFFF5F5F5.toInt())
//                            .fillColor(0x32FF0000.toInt())
//                    )
//                }
//            }
//        }
//    }
//
//    val lifecycleOwner = LocalLifecycleOwner.current
//    DisposableEffect(lifecycleOwner) {
//        val lifecycle = lifecycleOwner.lifecycle
//        val observer = object : DefaultLifecycleObserver {
//            override fun onResume(owner: LifecycleOwner) {
//                mapView.onResume()
//            }
//            override fun onPause(owner: LifecycleOwner) {
//                mapView.onPause()
//            }
//            override fun onDestroy(owner: LifecycleOwner) {
//                mapView.onDestroy()
//            }
//        }
//
//        lifecycle.addObserver(observer)
//
//        onDispose {
//            lifecycle.removeObserver(observer)
//        }
//    }
//
//    ModalNavigationDrawer(
//        drawerState = drawerState,
//        drawerContent = {
//            ModalDrawerSheet {
//                Text(
//                    text = if (selectedState == null) "Select a State"
//                    else "Select a District in $selectedState",
//                    modifier = Modifier.padding(16.dp)
//                )
//
//                if (selectedState == null) {
//                    LazyColumn {
//                        items(states) { state ->
//                            TextButton(
//                                onClick = {
//                                    viewModel.onStateSelected(state.stateName)
//                                    viewModel.loadDistrictsForState(state.stateName)
//                                }
//                            ) {
//                                Text(state.stateName)
//                            }
//                        }
//                    }
//                } else {
//                    LazyColumn {
//                        items(districts) { district ->
//                            TextButton(
//                                onClick = {
//                                    viewModel.onDistrictSelected(district)
//                                    scope.launch { drawerState.close() }
//                                }
//                            ) {
//                                Text(district.districtName)
//                            }
//                        }
//
//                        item {
//                            TextButton(
//                                onClick = { viewModel.selectedState.value = null }
//                            ) {
//                                Text("← Back to States")
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    ) {
//        Scaffold(
//            bottomBar = { BottomBar(navController = navController) },
//            topBar = {
//                @OptIn(ExperimentalMaterial3Api::class)
//                TopAppBar(
//                    title = { Text("FloodAid Map") },
//                    navigationIcon = {
//                        IconButton(onClick = {
//                            scope.launch { drawerState.open() }
//                        }) {
//                            Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
//                        }
//                    }
//                )
//            }
//        ) {
//            // Main map content
//            AndroidView(
//                factory = { mapView },
//                modifier = Modifier.fillMaxSize()
//            ) {
//                it.getMapAsync { googleMap ->
//                    map = googleMap
//                    // Initial map setup if needed
//                    googleMap.uiSettings.isZoomControlsEnabled = true
//                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(savedCameraPosition))
//                    googleMap.setOnCameraIdleListener {
//                        savedCameraPosition = googleMap.cameraPosition
//                    }
//
//                    // Handle any existing selection
//                    selectedDistrict?.let { district ->
//                        val districtLatLng = LatLng(district.latitude, district.longitude)
////  Dont Need Gua                      googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(districtLatLng, 10f))
//
//                        district.borderCoordinates?.let { border ->
//                            val polygonPoints = border.coordinates.map { LatLng(it[0], it[1]) }
//                            currentPolygon?.remove()
//                            currentPolygon = googleMap.addPolygon(
//                                PolygonOptions()
//                                    .addAll(polygonPoints)
//                                    .strokeColor(0xFFF5F5F5.toInt())
//                                    .fillColor(0x32FF0000.toInt())
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
