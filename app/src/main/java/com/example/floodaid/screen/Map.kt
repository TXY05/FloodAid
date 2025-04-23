package com.example.floodaid.screen

import BottomBar
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.LatLng
import com.example.floodaid.R
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floodaid.roomDatabase.Border
import com.example.floodaid.roomDatabase.District
import com.example.floodaid.roomDatabase.State
import com.example.floodaid.viewmodel.MapViewModel
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolygonOptions
import androidx.compose.runtime.DisposableEffect
import com.google.android.gms.maps.model.Polygon

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
fun Map(navController: NavHostController, viewModel: MapViewModel = viewModel()) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentPolygon by remember { mutableStateOf<Polygon?>(null) }

    // Collect state from ViewModel
    val states by viewModel.states
    val districts by viewModel.districts
    val selectedState by viewModel.selectedState
    val selectedDistrict by viewModel.selectedDistrict

    // Map-related variables
    val context = LocalContext.current
    var map by remember { mutableStateOf<GoogleMap?>(null) }
    var mapView by remember {
        mutableStateOf(
            MapView(context, GoogleMapOptions().mapId(context.getString(R.string.map_id))).apply {
                onCreate(null)
            }
        )
    }

    var savedCameraPosition by rememberSaveable(stateSaver = CameraPositionSaver) {
        mutableStateOf(
            CameraPosition(
                LatLng(4.2105, 101.9758), // Default position
                7f, // Default zoom
                0f,  // Default tilt
                0f   // Default bearing
            )
        )
    }

    // Handle district selection changes
    LaunchedEffect(selectedDistrict) {
        selectedDistrict?.let { district ->
            map?.let { googleMap ->
                // Move camera to selected district
                val districtLatLng = LatLng(district.latitude, district.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(districtLatLng, 10f))


                // Draw border polygon if available
                district.borderCoordinates?.let { border ->
                    val polygonPoints = border.coordinates.map { LatLng(it[0], it[1]) }
                    currentPolygon?.remove()
                    currentPolygon = googleMap.addPolygon(
                        PolygonOptions()
                            .addAll(polygonPoints)
                            .strokeColor(0xFFF5F5F5.toInt())
                            .fillColor(0x32FF0000.toInt())
                    )
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }
            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }
            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = if (selectedState == null) "Select a State"
                    else "Select a District in $selectedState",
                    modifier = Modifier.padding(16.dp)
                )

                if (selectedState == null) {
                    LazyColumn {
                        items(states) { state ->
                            TextButton(
                                onClick = {
                                    viewModel.onStateSelected(state.stateName)
                                    viewModel.loadDistrictsForState(state.stateName)
                                }
                            ) {
                                Text(state.stateName)
                            }
                        }
                    }
                } else {
                    LazyColumn {
                        items(districts) { district ->
                            TextButton(
                                onClick = {
                                    viewModel.onDistrictSelected(district)
                                    scope.launch { drawerState.close() }
                                }
                            ) {
                                Text(district.districtName)
                            }
                        }

                        item {
                            TextButton(
                                onClick = { viewModel.selectedState.value = null }
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
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("FloodAid Map") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                        }
                    }
                )
            }
        ) {
            // Main map content
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            ) {
                it.getMapAsync { googleMap ->
                    map = googleMap
                    // Initial map setup if needed
                    googleMap.uiSettings.isZoomControlsEnabled = true
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(savedCameraPosition))
                    googleMap.setOnCameraIdleListener {
                        savedCameraPosition = googleMap.cameraPosition
                    }

                    // Handle any existing selection
                    selectedDistrict?.let { district ->
                        val districtLatLng = LatLng(district.latitude, district.longitude)
//  Dont Need Gua                      googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(districtLatLng, 10f))

                        district.borderCoordinates?.let { border ->
                            val polygonPoints = border.coordinates.map { LatLng(it[0], it[1]) }
                            currentPolygon?.remove()
                            currentPolygon = googleMap.addPolygon(
                                PolygonOptions()
                                    .addAll(polygonPoints)
                                    .strokeColor(0xFFF5F5F5.toInt())
                                    .fillColor(0x32FF0000.toInt())
                            )
                        }
                    }
                }
            }
        }
    }
}

//@Composable
//fun InsertDummyDataScreen(viewModel: MapViewModel = viewModel()) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    val gombakBorders = Border(
//        coordinates = listOf(
//            listOf(3.5058, 101.6349),  // Kuala Kubu Bharu (Hulu Selangor border)
//            listOf(3.4667, 101.6667),  // Genting Sempah (Titiwangsa foothills)
//            listOf(3.4000, 101.6333),   // Ulu Yam
//            listOf(3.3500, 101.6000),   // Batu Caves (limestone hills)
//            listOf(3.3000, 101.5667),   // Gombak town
//            listOf(3.2500, 101.5500),   // Taman Melati (KL border)
//            listOf(3.2000, 101.5667),   // Wangsa Maju
//            listOf(3.1833, 101.6333),   // Selayang
//            listOf(3.2333, 101.6833),   // Rawang (southern edge)
//            listOf(3.3000, 101.7000),   // Reconnects to Hulu Selangor
//            listOf(3.4167, 101.7000)    // Completes the loop
//        )
//    )
//
//    val huluLangatBorders = Border(
//        coordinates = listOf(
//            listOf(3.4167, 101.8333),  // Semenyih Dam
//            listOf(3.3500, 101.8500),  // Beranang
//            listOf(3.2500, 101.8500),  // Kajang center
//            listOf(3.1500, 101.8167),  // Bangi
//            listOf(3.0833, 101.7833),  // Cheras border (Taman Connaught)
//            listOf(2.9833, 101.7667),  // Putrajaya boundary
//            listOf(2.9167, 101.7667),  // Dengkil
//            listOf(2.8833, 101.8000),  // Nilai border (N9)
//            listOf(2.9333, 101.8667),  // Eastern rural area
//            listOf(3.0500, 101.9000),  // Reconnects to Semenyih
//            listOf(3.2000, 101.8500)   // Completes the loop
//        )
//    )
//
//    val huluSelangorBorders = Border(
//        coordinates = listOf(
//            listOf(3.7667, 101.5000),  // Perak border (near Slim River)
//            listOf(3.7000, 101.5500),  // Kuala Kubu Bharu
//            listOf(3.6333, 101.6167),  // Rasa
//            listOf(3.5500, 101.6333),  // Serendah
//            listOf(3.5000, 101.6500),  // Batang Kali
//            listOf(3.4167, 101.6667),  // Genting Highlands foothills
//            listOf(3.3667, 101.6000),  // Ulu Yam
//            listOf(3.3833, 101.5000),  // Bestari Jaya
//            listOf(3.4500, 101.4500),  // Tanjung Malim border
//            listOf(3.6000, 101.4500),   // Reconnects to Sabak Bernam
//            listOf(3.7000, 101.4667)    // Completes the loop
//        )
//    )
//
//    val klangBorders = Border(
//        coordinates = listOf(
//            listOf(3.2500, 101.3167),  // Kapar
//            listOf(3.2000, 101.3667),  // Meru
//            listOf(3.1500, 101.4000),  // Klang city center
//            listOf(3.0833, 101.4333),  // Bukit Raja
//            listOf(3.0167, 101.4500),  // Port Klang
//            listOf(2.9500, 101.4000),  // Pulau Indah
//            listOf(2.9167, 101.3500),  // Kuala Langat border
//            listOf(2.9667, 101.3000),  // Telok Gong
//            listOf(3.0667, 101.2500),  // Jeram
//            listOf(3.1667, 101.2667),  // Tanjung Karang
//            listOf(3.2333, 101.3000)   // Reconnects to Kapar
//        )
//    )
//
//    val kualaLangatBorders = Border(
//        coordinates = listOf(
//            listOf(2.9500, 101.4000),  // Pulau Carey
//            listOf(2.9000, 101.4500),  // Banting
//            listOf(2.8333, 101.4833),  // Kuala Langat town
//            listOf(2.7500, 101.4500),  // Tanjung Sepat
//            listOf(2.6833, 101.4000),  // Sungai Pelek
//            listOf(2.6333, 101.3333),  // Sepang border
//            listOf(2.7333, 101.3000),  // Sijangkang
//            listOf(2.8333, 101.3333),  // Teluk Panglima Garang
//            listOf(2.9167, 101.3500),  // Reconnects to Pulau Indah
//            listOf(2.9667, 101.3833)   // Completes the loop
//        )
//    )
//
//    val kualaSelangorBorders = Border(
//        coordinates = listOf(
//            listOf(3.5000, 101.2500),  // Sabak Bernam border
//            listOf(3.4333, 101.2833),  // Bestari Jaya
//            listOf(3.3667, 101.2500),  // Kuala Selangor town
//            listOf(3.3000, 101.2333),  // Tanjung Karang
//            listOf(3.2000, 101.3000),  // Kapar border
//            listOf(3.1500, 101.2000),  // Sungai Besar
//            listOf(3.2500, 101.1500),  // Sekinchan
//            listOf(3.3500, 101.1667),  // Sungai Ayer Tawar
//            listOf(3.4333, 101.2000),  // Reconnects to Sabak Bernam
//            listOf(3.4833, 101.2333)   // Completes the loop
//        )
//    )
//
//    val petalingBorders = Border(
//        coordinates = listOf(
//            listOf(3.2167, 101.5500),  // Damansara
//            listOf(3.1833, 101.6000),  // Kepong
//            listOf(3.1333, 101.6167),  // Selayang border
//            listOf(3.0833, 101.6333),  // Petaling Jaya
//            listOf(3.0333, 101.6500),  // Subang Jaya
//            listOf(2.9833, 101.6000),  // Putra Heights
//            listOf(2.9500, 101.5500),  // Puchong
//            listOf(2.9833, 101.5000),  // Bukit Raja (Klang border)
//            listOf(3.0667, 101.5000),  // Shah Alam
//            listOf(3.1500, 101.5333)   // Reconnects to Damansara
//        )
//    )
//
//    val sabakBernamBorders = Border(
//        coordinates = listOf(
//            listOf(3.8000, 101.0000),  // Perak border
//            listOf(3.7333, 101.0333),  // Sungai Besar
//            listOf(3.6667, 101.0833),  // Sekinchan
//            listOf(3.5833, 101.1667),  // Sungai Ayer Tawar
//            listOf(3.5000, 101.2500),  // Kuala Selangor border
//            listOf(3.4333, 101.2000),  // Coastal area
//            listOf(3.5333, 101.1000),  // Sabak Bernam town
//            listOf(3.6333, 101.0500),  // Sungai Panjang
//            listOf(3.7000, 101.0167),  // Reconnects to Perak
//            listOf(3.7667, 100.9833)   // Completes the loop
//        )
//    )
//
//    val sepangBorders = Border(
//        coordinates = listOf(
//            listOf(2.9500, 101.7500),  // Putrajaya
//            listOf(2.9000, 101.7667),  // Dengkil
//            listOf(2.8167, 101.7500),  // KLIA
//            listOf(2.6833, 101.7000),  // Salak Tinggi
//            listOf(2.6000, 101.6333),  // Bagan Lalang
//            listOf(2.5500, 101.5000),  // Sepang town
//            listOf(2.6333, 101.4000),  // Sungai Pelek
//            listOf(2.7333, 101.4500),  // Reconnects to Kuala Langat
//            listOf(2.8333, 101.5500),  // Cyberjaya
//            listOf(2.9000, 101.6500)   // Completes the loop
//        )
//    )
//
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        Spacer(modifier = Modifier.height(25.dp))
//
//        Text("Insert Initial Data", style = MaterialTheme.typography.titleLarge)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(onClick = {
//            scope.launch {
//                // Insert dummy states
//                val states = listOf(
//                    State("Selangor")
//                )
//
//                val cities = listOf(
//                    District("Gombak", "Selangor", 3.2535, 101.5716, gombakBorders),
//                    District("Hulu Langat", "Selangor", 3.1390, 101.8556, huluLangatBorders),
//                    District("Hulu Selangor", "Selangor", 3.5058, 101.6349, huluSelangorBorders),
//                    District("Klang", "Selangor", 3.0449, 101.4456, klangBorders),
//                    District("Kuala Langat", "Selangor", 2.8031, 101.4952, kualaLangatBorders),
//                    District("Kuala Selangor", "Selangor", 3.3398, 101.2501, kualaSelangorBorders),
//                    District("Petaling", "Selangor", 3.1073, 101.6067, petalingBorders),
//                    District("Sabak Bernam", "Selangor", 3.7694, 100.9879, sabakBernamBorders),
//                    District("Sepang", "Selangor", 2.6913, 101.7505, sepangBorders)
//                )
//
//                viewModel.insertAllStates(states)
//                viewModel.insertAllCities(cities)
//            }
//        }) {
//            Text("Insert Dummy Data")
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = {
//                scope.launch {
//                    viewModel.deleteAllDistricts()
//                    viewModel.deleteAllStates()
//                }
//            }
//        ) {
//            Text("Delete All States & Districts")
//        }
//    }
//}