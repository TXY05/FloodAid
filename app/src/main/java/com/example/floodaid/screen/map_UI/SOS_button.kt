package com.example.floodaid.screen.map_UI

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SOSButtonPlacement {
    DASHBOARD,
    MAP
}

@SuppressLint("MissingPermission")
@Composable
fun SOSButton(
    viewModel: SOSViewModel,
    placement: SOSButtonPlacement,
    modifier: Modifier = Modifier
) {
    val isSOSActive by viewModel.isSOSActive.collectAsState()
    val needsPermission by viewModel.needsLocationPermission.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, show the SOS confirmation dialog
            showDialog = true
        } else {
            // Permission denied, show explanation dialog
            showPermissionDialog = true
        }
        // Clear the permission request flag in ViewModel
        viewModel.clearPermissionRequest()
    }

    // Handle permission request dialog when needed
    LaunchedEffect(needsPermission) {
        if (needsPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // SOS Status Indicator
        SOSStatusIndicator(
            isActive = isSOSActive,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 48.dp)
        )

        // SOS Button
        Button(
            onClick = {
                if (isSOSActive) {
                    // If SOS is active, just show dialog to confirm deactivation
                    showDialog = true
                } else {
                    // If SOS is not active, check location permission first
                    val hasPermission = viewModel.checkLocationPermission()
                    if (hasPermission) {
                        // Permission already granted, show confirmation dialog
                        showDialog = true
                    }
                    // If permission not granted, the LaunchedEffect will handle the request
                }
            },
            modifier = Modifier
                .align(when (placement) {
                    SOSButtonPlacement.DASHBOARD -> Alignment.BottomEnd
                    SOSButtonPlacement.MAP -> Alignment.BottomEnd
                })
                .padding(
                    bottom = when (placement) {
                        SOSButtonPlacement.DASHBOARD -> 120.dp // Above bottom bar
                        SOSButtonPlacement.MAP -> 120.dp
                    },
                    end = when (placement) {
                        SOSButtonPlacement.DASHBOARD -> 20.dp
                        SOSButtonPlacement.MAP -> 60.dp
                    }
                )
                .size(70.dp)
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSOSActive) Color(0xFFFF5252) else Color(0xFFFFCDD2)
            ),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SOS",
                    color = if (isSOSActive) Color.White else Color.Red,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }

    // SOS Confirmation Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (isSOSActive) "Cancel SOS" else "Call SOS") },
            text = {
                Column {
                    Text(if (isSOSActive)
                        "Are you sure you want to cancel the emergency alert?"
                    else
                        "Are you sure you want to send an emergency alert?"
                    )

                    if (!isSOSActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Your location will be tracked in the background to help emergency responders.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isSOSActive) {
                            // Deactivate SOS
                            viewModel.setSOSState(false)
                        } else {
                            // Activate SOS
                            viewModel.activateSOS()
                        }
                        showDialog = false
                    }
                ) {
                    Text(if (isSOSActive) "Yes, Cancel" else "Yes, Send Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(if (isSOSActive) "No, Keep Active" else "Cancel")
                }
            }
        )
    }

    // Permission Explanation Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Location Permission Required") },
            text = {
                Text("Location permission is needed to send your coordinates in case of emergency. Please grant location permission to use the SOS feature.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                ) {
                    Text("Try Again")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SOSStatusIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val color by infiniteTransition.animateColor(
        initialValue = Color(0xFFFFCDD2),
        targetValue = Color.Red,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SOS Blink"
    )

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isActive) color else Color(0xFFFFCDD2))
            .border(1.dp, Color.Black, CircleShape)
    )
}