package com.example.floodaid.screen

import BottomBar
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.LatLng
import android.view.View
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions

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
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedState by remember { mutableStateOf<String?>(null) }

    val states = listOf("California", "Texas", "New York")
    val citiesByState = mapOf(
        "California" to listOf("Los Angeles", "San Francisco", "San Diego"),
        "Texas" to listOf("Houston", "Dallas", "Austin"),
        "New York" to listOf("New York City", "Buffalo", "Albany")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = if (selectedState == null) "Select a State" else "Select a City in $selectedState",
                    modifier = Modifier.padding(16.dp)
                )

                if (selectedState == null) {
                    // State Buttons
                    states.forEach { state ->
                        TextButton(onClick = { selectedState = state }) {
                            Text(state)
                        }
                    }
                } else {
                    // City Buttons
                    citiesByState[selectedState]?.forEach { city ->
                        TextButton(onClick = {
                            // Handle city selection here (e.g., update map later)
                            scope.launch { drawerState.close() }
                        }) {
                            Text(city)
                        }
                    }

                    TextButton(onClick = { selectedState = null }) {
                        Text("← Back to States")
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
            Column(modifier = Modifier.fillMaxSize()) {
                val context = LocalContext.current
                val lifecycle = LocalLifecycleOwner.current.lifecycle

                // Save map position across recompositions and navigation
                var savedCameraPosition by rememberSaveable(stateSaver = CameraPositionSaver) {
                    mutableStateOf(CameraPosition(LatLng(28.7041, 77.1025), 19f, 0f, 0f))
                }

                val mapView = remember {
                    MapView(context, GoogleMapOptions().mapId(context.getString(R.string.map_id))).apply {
                        onCreate(null)
                    }
                }

                DisposableEffect(lifecycle) {
                    val observer = object : DefaultLifecycleObserver {
                        override fun onResume(owner: LifecycleOwner) = mapView.onResume()
                        override fun onPause(owner: LifecycleOwner) = mapView.onPause()
                        override fun onDestroy(owner: LifecycleOwner) = mapView.onDestroy()
                    }

                    lifecycle.addObserver(observer)
                    onDispose {
                        lifecycle.removeObserver(observer)
                    }
                }

                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize(),
                    update = {
                        mapView.getMapAsync { map ->
                            // Move to saved camera position
                            map.moveCamera(CameraUpdateFactory.newCameraPosition(savedCameraPosition))

                            // Save position when camera stops moving
                            map.setOnCameraIdleListener {
                                savedCameraPosition = map.cameraPosition
                            }

                            // UI settings
                            with(map.uiSettings) {
                                isZoomGesturesEnabled = true
                                isRotateGesturesEnabled = true
                                isScrollGesturesEnabled = true
                                isScrollGesturesEnabledDuringRotateOrZoom = true
                            }
                        }
                    }
                )
            }
        }
    }
}

//fun Map(navController: NavHostController) {
//    Scaffold(
//        bottomBar = { BottomBar(navController = navController) },
//        topBar = {
//            @OptIn(ExperimentalMaterial3Api::class)
//            TopAppBar(
//                title = { Text("FloodAid Map") },
//                navigationIcon = {
//                    IconButton(onClick = { /* TODO: Drawer toggle */ }) {
//                        Icon(Icons.Default.Menu, contentDescription = "Menu")
//                    }
//                }
//            )
//        }
//    ) {
//        GoogleMapView()
//    }
//}
//
//@Composable
//fun GoogleMapView() {
//    val context = LocalContext.current
//    val mapView = remember {
//        MapView(context).apply {
//            onCreate(null) // we manually control lifecycle
//        }
//    }
//
//    // Handle lifecycle properly to prevent memory leaks
//    val lifecycle = LocalLifecycleOwner.current.lifecycle
//    DisposableEffect(lifecycle) {
//        val observer = object : DefaultLifecycleObserver {
//            override fun onResume(owner: LifecycleOwner) = mapView.onResume()
//            override fun onPause(owner: LifecycleOwner) = mapView.onPause()
//            override fun onDestroy(owner: LifecycleOwner) = mapView.onDestroy()
//        }
//
//        lifecycle.addObserver(observer)
//        onDispose { lifecycle.removeObserver(observer) }
//    }
//
//    AndroidView(
//        factory = { mapView },
//        modifier = Modifier.fillMaxSize(),
//        update = {
//            mapView.getMapAsync { map ->
//                val latLng = LatLng(28.7041, 77.1025)
//                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f))
//
//                with(map.uiSettings) {
//                    isZoomGesturesEnabled = true
//                    isRotateGesturesEnabled = true
//                    isScrollGesturesEnabled = true
//                    isScrollGesturesEnabledDuringRotateOrZoom = true
//                }
//
//                // Optional: Apply cloud styling
//                try {
//                    val success = map.setMapStyle(
//                        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_json)
//                    )
//                    if (!success) {
//                        Log.e("MapView", "Style parsing failed.")
//                    }
//                } catch (e: Exception) {
//                    Log.e("MapView", "Style load failed: ${e.message}")
//                }
//            }
//        }
//    )
//}
