package com.example.floodaid.screen.map_UI

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.floodaid.roomDatabase.Entities.Shelter
import com.example.floodaid.utils.DistanceCalculator
import com.google.android.gms.maps.model.LatLng
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SheetState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelterDetailsBottomSheet(
    shelter: Shelter?,
    currentLocation: LatLng?, // Add this back
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState() // Add this parameter
) {
    val distance by remember(currentLocation, shelter) {
        derivedStateOf {
            if (currentLocation != null && shelter != null) {
                DistanceCalculator.calculateDistance(
                    currentLocation,
                    LatLng(shelter.latitude, shelter.longitude)
                )
            } else null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = shelter?.helpCenterName ?: "No shelter selected",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                when {
                    currentLocation == null -> {
                        Text(
                            text = "No GPS",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    distance != null -> {
                        Text(
                            text = "%.1f km".format(distance),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    else -> {
                        Text(
                            text = "Calculating...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            shelter?.descriptions?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            shelter?.address?.let { address ->
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}