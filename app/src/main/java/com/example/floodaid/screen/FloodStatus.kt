package com.example.floodaid.screen

import BottomBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloodStatus(navController: NavHostController) {
    val floodData = listOf(
        "Kuala Lumpur" to "Flooded",
        "Gombak" to "Safe",
        "Hulu Langat" to "Safe",
        "Hulu Selangor" to "Safe",
        "Klang" to "Safe",
        "Kuala Langat" to "Safe",
        "Petaling" to "Safe",
        "Sabak Bernam" to "Flooded",
        "Sepang" to "Flooded"
    )

    var selectedLocation by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        if (selectedLocation == null) {
            // Flood Status List
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color.Red, shape = CircleShape)
                    )
                    Text(
                        text = " Flooded",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color.Green, shape = CircleShape)
                    )
                    Text(
                        text = " Safe",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
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
            // Flood Status Detail
            FloodStatusDetail(
                location = selectedLocation!!,
                onBack = { selectedLocation = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloodStatusDetail(location: String, onBack: () -> Unit) {
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
                text = "Current Status: Prepare for Flood",
                fontWeight = FontWeight.Bold,
                color = Color.Red,
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
    FloodStatusDetail(location = "Kuala Lumpur", onBack = {})
}