package com.example.floodaid.screen.map_UI

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.floodaid.screen.map_UI.SOSViewModel
import androidx.compose.ui.text.style.TextAlign

enum class SOSButtonPlacement {
    DASHBOARD,
    MAP
}

@Composable
fun SOSButton(
    viewModel: SOSViewModel,
    placement: SOSButtonPlacement,
    modifier: Modifier = Modifier
) {
    val isSOSActive by viewModel.isSOSActive.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // SOS Status Indicator
        SOSStatusIndicator(
            isActive = isSOSActive,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 24.dp)
        )

        // SOS Button
        Button(
            onClick = { showDialog = true },
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
                containerColor = Color(0xFFFFCDD2)
            ),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SOS",
                    color = Color.Red,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (isSOSActive) "Cancel SOS" else "Call SOS") },
            text = { Text(if (isSOSActive) "Are you sure you want to cancel SOS?" else "Are you sure you want to call SOS!!!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setSOSState(!isSOSActive)
                        showDialog = false
                    }
                ) {
                    Text(if (isSOSActive) "Yes" else "Call")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(if (isSOSActive) "No" else "Cancel")
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
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isActive) color else Color(0xFFFFCDD2))
            .border(1.dp, Color.Black, CircleShape)
    )
}