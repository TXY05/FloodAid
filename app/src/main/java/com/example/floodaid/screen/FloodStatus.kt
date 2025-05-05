package com.example.floodaid.screen

import BottomBar
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.floodaid.models.Screen
import com.example.floodaid.repository.FirestoreRepository
import com.example.floodaid.roomDatabase.Database.FloodAidDatabase
import com.example.floodaid.screen.floodstatus.FloodStatusRepository
import com.example.floodaid.screen.floodstatus.FloodStatusViewModelFactory
import com.example.floodaid.viewmodel.FloodStatusViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FloodStatus(navController: NavHostController, viewModel: FloodStatusViewModel, database: FloodAidDatabase) {
    val uiState by viewModel.uiState.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val locations by viewModel.locations.observeAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        if (user != null) {
            viewModel.syncFromFirestore()
        } else {
            // Redirect to login or show an error
            navController.navigate(Screen.Login.route)
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
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
                            "= Safe",
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
                            "= Flooded",
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

                    Button(
                        onClick = { viewModel.clearAllData() },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Clear All Data")
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
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFloodStatusDialog(
    floodData: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val locations = floodData.map { it.first }
    var selectedLocation by remember { mutableStateOf(locations.firstOrNull() ?: "") }
    var status by remember { mutableStateOf("Safe") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Flood Status") },
        text = {
            Column {
                Text("Select Location:")
                Box {
                    Button(onClick = { isDropdownExpanded = true }) {
                        Text(if (selectedLocation.isNotEmpty()) selectedLocation else "Select a location")
                    }
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        locations.forEach { location ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedLocation = location
                                    isDropdownExpanded = false
                                    errorMessage = "" // Clear error when a location is selected
                                },
                                text = { Text(location) }
                            )
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Select Status:")
                Row {
                    listOf("Safe", "Flooded").forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { status = option }
                        ) {
                            RadioButton(
                                selected = status == option,
                                onClick = { status = option }
                            )
                            Text(option)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (selectedLocation.isEmpty()) {
                    errorMessage = "Please select a location."
                } else {
                    onSave(selectedLocation, status)
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloodStatusDetail(
    location: String,
    currentStatus: String,
    onBack: () -> Unit,
    viewModel: FloodStatusViewModel
) {

    val history by viewModel.historyState.collectAsState()

    LaunchedEffect(location) {
        viewModel.fetchFloodHistory(location)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$location Flood Status", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Current Status: $currentStatus",
                fontWeight = FontWeight.Bold,
                color = if (currentStatus == "Flooded") Color.Red else Color.Green,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Past 7 Days History:",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn {
                items(history) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.date} : ${item.status}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FloodStatusPreview() {
    val context = LocalContext.current
    val database = FloodAidDatabase.getInstance(context)
    val roomRepository = FloodStatusRepository(database.floodStatusDao())
    val firestoreRepository = FirestoreRepository()
    val dao = database.floodStatusDao()

    val viewModelFactory = FloodStatusViewModelFactory(roomRepository, dao, firestoreRepository)
    val viewModel: FloodStatusViewModel = viewModel(factory = viewModelFactory)

    FloodStatus(navController = rememberNavController(), viewModel = viewModel, database = database)
}

@Preview(showBackground = true)
@Composable
fun FloodStatusDetailPreview() {
    val mockDatabase = FloodAidDatabase.getInstance(LocalContext.current)
    val mockDao = mockDatabase.floodStatusDao()
    val mockRepository = FloodStatusRepository(mockDao)
    val mockFirestoreRepository = FirestoreRepository()
    val mockViewModel = FloodStatusViewModel(mockRepository, mockDao, mockFirestoreRepository)

    FloodStatusDetail(
        location = "Gombak",
        currentStatus = "",
        onBack = {},
        viewModel = mockViewModel
    )
}