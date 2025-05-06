//package com.example.floodaid.utils
//
//import BottomBar
//import android.annotation.SuppressLint
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.material3.rememberTopAppBarState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavHostController
//import com.example.floodaid.composable.TopBar
//import com.example.floodaid.roomDatabase.Entities.Border
//import com.example.floodaid.roomDatabase.Entities.District
//import com.example.floodaid.roomDatabase.Entities.FloodMarker
//import com.example.floodaid.roomDatabase.Entities.Shelter
//import com.example.floodaid.roomDatabase.Entities.State
//import com.example.floodaid.screen.map_UI.MapViewModel
//import kotlinx.coroutines.launch
//import java.time.Instant
//import java.time.temporal.ChronoUnit
//
//@OptIn(ExperimentalMaterial3Api::class)
//@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//@Composable
//fun InsertMapData(
//    navController: NavHostController,
//) {
//    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
//        state = rememberTopAppBarState()
//    )
//    Scaffold(
//        topBar = { TopBar(scrollBehavior = scrollBehavior) },
//        bottomBar = { BottomBar(navController = navController) }
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text(text = "Profile", fontSize = 50.sp)
//        }
//        Column{
//            InsertDummyDataScreen()
//        }
//    }
//}
//
//@Composable
//fun InsertDummyDataScreen(viewModel: MapViewModel = viewModel()) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    val uiState by viewModel.uiState.collectAsState()
//    val statesEntity = uiState.states
//    val districtEntity = uiState.districts
//    val gombakDistrict = districtEntity.firstOrNull { it.name == "Gombak" }
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
//                val selangor = State(id = 1, name = "Selangor")
//
//                viewModel.insertState(selangor)
//            }
//        }) {
//            Text("Insert Dummy Data")
//        }
//        Button(onClick = {
//            scope.launch {
//                val stateId = statesEntity[0].id
//
//                val dis = listOf(
//                    District(
//                        id = 1,
//                        name = "Gombak",
//                        latitude = 3.2535,
//                        longitude = 101.5716,
//                        borderCoordinates = gombakBorders,
//                        stateId = stateId
//                    ),
//                    District(
//                        id = 2,
//                        name = "Hulu Langat",
//                        latitude = 3.1390,
//                        longitude = 101.8556,
//                        borderCoordinates = huluLangatBorders,
//                        stateId = stateId
//                    ),
//                    District(
//                        id = 3,
//                        name = "Hulu Selangor",
//                        latitude = 3.5058,
//                        longitude = 101.6349,
//                        borderCoordinates = huluSelangorBorders,
//                        stateId = stateId
//                    ),
//                    District(
//                        id = 4,
//                        name = "Klang",
//                        latitude = 3.0449,
//                        longitude = 101.4456,
//                        borderCoordinates = klangBorders,
//                        stateId = stateId
//                    ),
//                    District(
//                        id = 5,
//                        name = "Kuala Langat",
//                        latitude = 2.8031,
//                        longitude = 101.4952,
//                        borderCoordinates = kualaLangatBorders,
//                        stateId = stateId
//                    ),
//                    District(
//                        id = 6,
//                        name = "Kuala Selangor",
//                        latitude = 3.3398,
//                        longitude = 101.2501,
//                        borderCoordinates = kualaSelangorBorders,
//                        stateId = stateId
//                    ),
//                    District(
//                        id = 7,
//                        name = "Petaling",
//                        latitude = 3.1073,
//                        longitude = 101.6067,
//                        borderCoordinates = petalingBorders,
//                        stateId = stateId
//                    ),
//                    District(
//                        id = 8,
//                        name = "Sabak Bernam",
//                        latitude = 3.7694,
//                        longitude = 100.9879,
//                        borderCoordinates = sabakBernamBorders,
//                        stateId = stateId
//                    ),
//                    District(
//                        id = 9,
//                        name = "Sepang",
//                        latitude = 2.6913,
//                        longitude = 101.7505,
//                        borderCoordinates = sepangBorders,
//                        stateId = stateId
//                    )
//                )
//
//                viewModel.insertAllDistricts(dis)
//            }
//        }) {
//            Text("Insert District")
//        }
//        Button(onClick = {
//            scope.launch {
////                val districtId = gombakDistrict?.id
//
////                if (districtId != null) {
//                val shelters = listOf(
//                    Shelter(
//                        id = 1,
//                        helpCenterName = "Gombak Shelter 1",
//                        descriptions = "Temporary shelter for flood victims",
//                        latitude = 3.2715,
//                        longitude = 101.5850,
//                        districtId = 2// Gombak
//                    ),
//                    Shelter(
//                        id = 2,
//                        helpCenterName = "Gombak Shelter 2",
//                        descriptions = "Community relief center",
//                        latitude = 3.1450,
//                        longitude = 101.8600,
//                        districtId = 1 // Hulu Langat
//                    )
//                )
//                viewModel.insertAllShelters(shelters)
////                }
//            }
//        }) {
//            Text("Insert Shelter")
//        }
//        Button(onClick = {
//            scope.launch {
////                val districtId = districtEntity[0].id
//
//                val floodMarkers = listOf(
//                    FloodMarker(
//                        floodStatus = "flood",
//                        districtId = 1, // Gombak
//                        latitude = 3.2550,
//                        longitude = 101.5800,
//                        expiryTime = Instant.now().plus(10, ChronoUnit.MINUTES)
//                    ),
//                    FloodMarker(
//                        floodStatus = "safe",
//                        districtId = 1, // Hulu Langat
//                        latitude = 3.1400,
//                        longitude = 101.8500,
//                        expiryTime = Instant.now().plus(10, ChronoUnit.MINUTES)
//                    ),
//                    FloodMarker(
//                        floodStatus = "flood",
//                        districtId = 1, // Gombak
//                        latitude = 3.2570,
//                        longitude = 101.5800,
//                        expiryTime = Instant.now().plus(1, ChronoUnit.HOURS)
//                    ),
//                    FloodMarker(
//                        floodStatus = "safe",
//                        districtId = 1, // Hulu Langat
//                        latitude = 3.227760,
//                        longitude = 101.692636,
//                        expiryTime = Instant.now().plus(1, ChronoUnit.HOURS)
//                    )
////                     Add more markers...
//                )
//
//                viewModel.insertAllFloodMarkers(floodMarkers)
//
//            }
//        }) {
//            Text("Insert District")
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = {
//                scope.launch {
//                    viewModel.deleteAllStates()
//                    viewModel.deleteAllDistricts()
//                    viewModel.deleteAllShelters()
//                    viewModel.deleteAllFloodMarkers()
//                }
//            }
//        ) {
//            Text("Delete All States & Districts")
//        }
//    }
//}