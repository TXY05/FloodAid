package com.example.floodaid.screen

import BottomBar
import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.floodaid.models.Screen
import com.example.floodaid.ui.theme.FloodAidTheme
import com.example.floodaid.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floodaid.repository.FirestoreRepository
import com.example.floodaid.screen.floodstatus.FloodStatusDao
import com.example.floodaid.screen.floodstatus.FloodStatusRepository
import com.example.floodaid.screen.floodstatus.FloodStatusViewModelFactory
import com.example.floodaid.viewmodel.FloodStatusViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.SavedStateHandle

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Dashboard(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    repository: FloodStatusRepository,
    dao: FloodStatusDao,
    firestoreRepository: FirestoreRepository
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) {
        DashboardScreen(
            navController = navController,
            authViewModel = authViewModel,
            repository = repository,
            dao = dao,
            firestoreRepository = firestoreRepository
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    repository: FloodStatusRepository,
    dao: FloodStatusDao,
    firestoreRepository: FirestoreRepository
) {
    val viewModel: FloodStatusViewModel = viewModel(
        factory = FloodStatusViewModelFactory(
            repository,
            dao,
            firestoreRepository,
            SavedStateHandle()
        )
    )
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dashboard") })
        }
    ) { paddingValues ->
        if (isLandscape) {
            // Landscape layout with fixed-size shortcuts
            Row(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // Left side - Additional content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Flood status information
                    FloodStatusHeader(
                        status = uiState.currentStatus,
                        locations = uiState.floodData.map { it.location },
                        selectedLocation = uiState.selectedLocation ?: "",
                        onLocationSelected = { location -> viewModel.selectLocation(location) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Right side - Features grid (same size as portrait)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFD6EAF8))
                            .padding(8.dp)
                    ) {
                        DashboardGrid(navController, isLandscape = isLandscape)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            authViewModel.signoutFunction()
                            navController.navigate("welcomeloading")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(text = "Logout")
                    }
                }
            }
        } else {
            // Original portrait layout
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FloodStatusHeader(
                    status = uiState.currentStatus,
                    locations = uiState.floodData.map { it.location },
                    selectedLocation = uiState.selectedLocation ?: "",
                    onLocationSelected = { location -> viewModel.selectLocation(location) }
                )

                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFD6EAF8))
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .wrapContentHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DashboardGrid(navController, isLandscape = isLandscape)
                        Button(
                            onClick = {
                                authViewModel.signoutFunction()
                                navController.navigate("welcomeloading")
                            },
                            enabled = true,
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text(text = "Logout")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloodStatusHeader(
    status: String = "Safe",
    locations: List<String>,
    onLocationSelected: (String) -> Unit,
    selectedLocation: String
) {
    val (showLegendDialog, setShowLegendDialog) = remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val (iconColor, textColor, message) = when (status) {
        "Flooded" -> Triple(Color.Red, Color.Red, "Flooded Area Detected")
        "Safe" -> Triple(Color(0xFF4CAF50), Color(0xFF4CAF50), "Safe")
        else -> Triple(Color.Gray, Color.Gray, "Unknown Status")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Add a logo at the top
        Icon(
            imageVector = if (status == "Flooded") Icons.Default.Warning else Icons.Default.CheckCircle,
            contentDescription = "Status Icon",
            modifier = Modifier.size(64.dp),
            tint = iconColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display message based on status
        Text(
            text = message,
            color = textColor,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Dropdown for Location Selection
        Box {
            Button(onClick = { isDropdownExpanded = true }) {
                Text(selectedLocation.ifEmpty { "Select a location" })
            }
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                locations.forEach { location ->
                    DropdownMenuItem(
                        onClick = {
                            onLocationSelected(location)
                            isDropdownExpanded = false
                        },
                        text = { Text(location) }
                    )
                }
            }
        }
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Press the button above to change districts.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

    }
}

@Composable
fun LegendItem(color: Color, label: String, logo: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = logo,
            contentDescription = "$label Logo",
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun DashboardGrid(navController: NavController, isLandscape: Boolean) {
    val features = listOf(
        Triple("Shelter Map", "map", Pair(Icons.Default.Place, Color(66, 165, 245))),
        Triple("Flood Status", "floodStatus", Pair(Icons.Default.Warning, Color(239, 83, 80))),
        Triple("Forum", "forum", Pair(Icons.Default.Forum, Color(98, 76, 199))),
        Triple("Volunteer", "volunteer", Pair(Icons.Default.HealthAndSafety, Color(88, 189, 133))),
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .wrapContentHeight()
            .wrapContentWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        items(features) { (title, route, iconWithColor) ->
            val (icon, color) = iconWithColor
            FeatureCard(
                title = title,
                icon = icon,
                color = color,
                size = if (isLandscape) 120.dp else 150.dp // Adjust size
            ) {
                navController.navigate(route) {
                    popUpTo(Screen.Dashboard.route) {
                        saveState = true
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    icon: ImageVector,
    color: Color,
    size: Dp, // Add size parameter
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(size) // Use the size parameter
            .padding(8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$title Icon",
                modifier = Modifier.size(48.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    FloodAidTheme { // Apply the theme here
        // Dashboard(navController = rememberNavController(), authViewModel = AuthViewModel())
    }
}