package com.example.floodaid.screen.floodstatus

import BottomBar
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.floodaid.models.Screen
import com.example.floodaid.roomDatabase.Repository.FirestoreRepository
import com.example.floodaid.roomDatabase.database.FloodAidDatabase
import com.example.floodaid.screen.map_UI.MapViewModel
import com.example.floodaid.viewmodel.FloodStatusViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FloodStatus(navController: NavHostController, viewModel: FloodStatusViewModel, database: FloodAidDatabase, mapViewModel: MapViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val scaffoldState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            scaffoldState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    LaunchedEffect(Unit) {
        if (user != null) {
            viewModel.syncFromFirestore()
        } else {
            // Redirect to login or show an error
            navController.navigate(Screen.Login.route)
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) },
        snackbarHost = { SnackbarHost(hostState = scaffoldState) }
    ) { innerPadding ->
        Crossfade(targetState = uiState.selectedLocation) { selectedLocation ->
            if (selectedLocation == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selangor's Flood Status",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Text(
                        text = "Tap a location below to view details or update status.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Safe",
                            tint = Color.Green,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            " Safe",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Spacer(modifier = Modifier.width(24.dp))

                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Flooded",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            " Flooded",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Button(onClick = { viewModel.showDialog() }) {
                        Text("Update Flood Status")
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        items(uiState.floodData) { locationStatus ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { viewModel.selectLocation(locationStatus.location) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = locationStatus.location,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Icon(
                                        imageVector = when (locationStatus.status) {
                                            "Flooded" -> Icons.Default.Warning
                                            "Safe" -> Icons.Default.CheckCircle
                                            else -> Icons.Default.Warning
                                        },
                                        contentDescription = locationStatus.status,
                                        tint = when (locationStatus.status) {
                                            "Flooded" -> Color.Red
                                            "Safe" -> Color.Green
                                            else -> Color.Gray
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Text(
                                        text = locationStatus.status,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when (locationStatus.status) {
                                            "Flooded" -> Color.Red
                                            "Safe" -> Color.Green
                                            else -> Color.Gray
                                        },
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                val currentStatus = uiState.floodData.firstOrNull { it.location == selectedLocation }?.status ?: "Unknown"
                FloodStatusDetail(
                    location = selectedLocation,
                    currentStatus = currentStatus,
                    onBack = { viewModel.clearSelectedLocation() },
                    viewModel = viewModel
                )
            }
        }

        if (uiState.showDialog) {
            AddFloodStatusDialog(
                floodData = uiState.floodData.map { it.location to it.status },
                onDismiss = { viewModel.dismissDialog() },
                onSave = { location, status ->
                    viewModel.updateFloodStatus(location, status)
                    viewModel.dismissDialog()
                },
                mapViewModel,
                viewModel
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FloodStatusPreview() {
    val context = LocalContext.current
    val database = FloodAidDatabase.getInstance(context)
    val roomRepository = FloodStatusRepository(database.floodStatusDao(), FirestoreRepository())
    val firestoreRepository = FirestoreRepository()
    val dao = database.floodStatusDao()
    val savedStateHandle = SavedStateHandle()

    val viewModelFactory = FloodStatusViewModelFactory(roomRepository, dao, firestoreRepository, savedStateHandle)
    val viewModel: FloodStatusViewModel = viewModel(factory = viewModelFactory)

//    FloodStatus(navController = rememberNavController(), viewModel = viewModel, database = database)
}

@Preview(showBackground = true)
@Composable
fun FloodStatusDetailPreview() {
    val mockDatabase = FloodAidDatabase.getInstance(LocalContext.current)
    val mockDao = mockDatabase.floodStatusDao()
    val mockRepository = FloodStatusRepository(mockDao, FirestoreRepository())
    val mockFirestoreRepository = FirestoreRepository()
    val savedStateHandle = SavedStateHandle()
    val mockViewModel = FloodStatusViewModel(mockRepository, mockDao, mockFirestoreRepository, savedStateHandle)

    FloodStatusDetail(
        location = "Gombak",
        currentStatus = "",
        onBack = {},
        viewModel = mockViewModel
    )
}