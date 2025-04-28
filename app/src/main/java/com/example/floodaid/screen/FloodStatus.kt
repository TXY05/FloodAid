package com.example.floodaid.screen

import BottomBar
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloodStatus(navController: NavHostController) {
    
    val locations = listOf(
        "Gombak",
        "Hulu Langat",
        "Hulu Selangor",
        "Klang",
        "Kuala Langat",
        "Petaling",
        "Sabak Bernam",
        "Sepang"
    )

    var floodData by remember {
        mutableStateOf(
            locations.map { it to "Safe" }.toMutableList().apply {
                this[this.indexOfFirst { it.first == "Sabak Bernam" }] = "Sabak Bernam" to "Flooded"
                this[this.indexOfFirst { it.first == "Sepang" }] = "Sepang" to "Flooded"
            }
        )
    }

    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        if (selectedLocation == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Klang's Valley Flood Status",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Text(
                    text = "Tap on the location to view details",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(onClick = { showDialog = true }) {
                    Text("Update Flood Status")
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(floodData) { (location, status) ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { selectedLocation = location }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = location,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            color = when (status) {
                                                "Flooded" -> Color.Red
                                                "Safe" -> Color.Green
                                                else -> Color.Gray
                                            },
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        } else {
            val currentStatus = floodData.firstOrNull { it.first == selectedLocation }?.second ?: "Unknown"
            FloodStatusDetail(
                location = selectedLocation!!,
                currentStatus = currentStatus,
                onBack = { selectedLocation = null }
            )
        }

        if (showDialog) {
            AddFloodStatusDialog(
                floodData = floodData,
                onDismiss = { showDialog = false },
                onSave = { location, status ->
                    floodData = floodData.map {
                        if (it.first == location) location to status else it
                    }.toMutableList()
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun AddFloodStatusDialog(
    floodData: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val locations = floodData.map { it.first }
    var selectedLocation by remember { mutableStateOf(locations.first()) }
    var status by remember { mutableStateOf("Safe") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Flood Status") },
        text = {
            Column {
                Text("Select Location:")
                Box {
                    Button(onClick = { isDropdownExpanded = true }) {
                        Text(selectedLocation)
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
                                },
                                text = { Text(location) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Select Status:")
                Row {
                    listOf("Safe", "Flooded").forEach { option ->
                        Button(
                            onClick = { status = option },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status == option) Color.Gray else Color.LightGray
                            )
                        ) {
                            Text(option)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedLocation, status) }) {
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
fun FloodStatusDetail(location: String, currentStatus: String, onBack: () -> Unit) {
    val history = listOf(
        "17/3/2025 - Safe",
        "16/3/2025 - Safe",
        "15/3/2025 - Flood",
        "14/3/2025 - Safe",
        "13/3/2025 - Safe",
        "12/3/2025 - Safe",
        "11/3/2025 - Safe"
    )

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

            history.forEach { item ->
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
                            text = item,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FloodStatusPreview() {
    FloodStatus(navController = rememberNavController())
}

@Preview(showBackground = true)
@Composable
fun FloodStatusDetailPreview() {
    FloodStatusDetail(location = "Gombak", currentStatus = "Safe", onBack = {})
}