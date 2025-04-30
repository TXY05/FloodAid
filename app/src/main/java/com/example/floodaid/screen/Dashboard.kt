package com.example.floodaid.screen

import BottomBar
import android.annotation.SuppressLint
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.floodaid.ui.theme.FloodAidTheme
import com.example.floodaid.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Dashboard(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) {
        DashboardScreen(navController = navController,authViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController,authViewModel: AuthViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dashboard") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Flood Warning Image + Status Label
            FloodStatusHeader()

            Spacer(modifier = Modifier.height(24.dp))

            // Grid of Main Features
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
                        DashboardGrid(navController)
                        Button(
                            onClick = {
                                authViewModel.signoutFunction()
                                navController.navigate("welcome")
                            },

                            enabled = true,
                            shape = MaterialTheme.shapes.medium,
                        ){
                            Text(text = "Logout")
                        }
                    }
            }
        }
    }
}

@Composable
fun FloodStatusHeader(status: String = "Safe") {
    val (showLegendDialog, setShowLegendDialog) = remember { mutableStateOf(false) }

    val (iconColor, textColor, message) = when (status) {
        "Flooded" -> Triple(Color.Red, Color.Red, "Flooded Area Detected")
        "Safe" -> Triple(Color(0xFF4CAF50), Color(0xFF4CAF50), "Safe")
        else -> Triple(Color.Gray, Color.Gray, "Unknown Status")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { setShowLegendDialog(true) }, // Open dialog on click
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Flood Status Icon",
            tint = iconColor,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = textColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }

    if (showLegendDialog) {
        AlertDialog(
            onDismissRequest = { setShowLegendDialog(false) },
            title = { Text("Flood Status Information") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LegendItem(color = Color(0xFFE53935), label = "Flooded", logo = Icons.Default.Warning)
                    LegendItem(color = Color(0xFF4CAF50), label = "Safe", logo = Icons.Default.CheckCircle)
                }
            },
            confirmButton = {
                Button(onClick = { setShowLegendDialog(false) }) {
                    Text("Close")
                }
            }
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
fun DashboardGrid(navController: NavController) {
    val features = listOf(
        "Shelter Map" to "map",
        "Flood Status" to "floodStatus",
        "Forum" to "forum",
        "Volunteer" to "volunteer",
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
        items(features) { (title, route) ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                FeatureCard(title = title) {
                    navController.navigate(route)
                }
            }
        }
    }
}


@Composable
fun FeatureCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
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
        //Dashboard(navController = rememberNavController())
    }
}