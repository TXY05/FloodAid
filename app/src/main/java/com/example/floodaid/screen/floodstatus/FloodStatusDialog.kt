package com.example.floodaid.screen.floodstatus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.floodaid.screen.map_UI.MapViewModel
import com.example.floodaid.viewmodel.FloodStatusViewModel
import com.example.floodaid.viewmodel.SaveState

@Composable
fun AddFloodStatusDialog(
    floodData: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    mapViewModel: MapViewModel,
    viewModel: FloodStatusViewModel
) {
    val locations = floodData.map { it.first }
    var selectedLocation by remember { mutableStateOf(locations.firstOrNull() ?: "") }
    var status by remember { mutableStateOf("Safe") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveState) {
        if (uiState.saveState == SaveState.SUCCESS) {
            // Dialog will be dismissed by the ViewModel
        }
    }

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
                                    viewModel.resetSaveState() // Reset error when location changes
                                },
                                text = { Text(location) }
                            )
                        }
                    }
                }

                if (uiState.errorMessage.isNotEmpty()) {
                    Text(
                        text = uiState.errorMessage,
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
            Button(
                onClick = {
                    if (selectedLocation.isEmpty()) {
                        viewModel.setErrorMessage("Please select a location.")
                    } else {
                        // Use ViewModel's method with state management
                        viewModel.saveFloodMarker(
                            location = selectedLocation,
                            status = status,
                            mapViewModel = mapViewModel
                        )
                    }
                },
                enabled = uiState.saveState != SaveState.SAVING
            ) {
                if (uiState.saveState == SaveState.SAVING) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, enabled = uiState.saveState != SaveState.SAVING) {
                Text("Cancel")
            }
        }
    )
} 