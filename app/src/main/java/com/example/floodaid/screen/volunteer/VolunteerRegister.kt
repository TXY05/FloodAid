package com.example.floodaid.screen.volunteer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.floodaid.models.Screen
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerRegister(
    navController: NavHostController,
    viewModel: VolunteerViewModel
) {
    val registration by viewModel.volunteer.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        viewModel.isVolunteerRegistered { isRegistered ->
            if (isRegistered) {
                navController.popBackStack()
                navController.navigate("volunteer_main") {
                    launchSingleTop = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Volunteer Registration",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = registration.phoneNum,
            onValueChange = viewModel::updatePhoneNumber,
            label = { Text("Your Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            isError = !viewModel.validatePhoneNumber(registration.phoneNum),
            supportingText = {
                if (!viewModel.validatePhoneNumber(registration.phoneNum)) {
                    Text("Please enter a valid phone number")
                }
            }
        )

        OutlinedTextField(
            value = registration.emgContact,
            onValueChange = viewModel::updateEmergencyContact,
            label = { Text("Emergency Contact Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = registration.emgNum,
            onValueChange = viewModel::updateEmergencyPhone,
            label = { Text("Emergency Contact Phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.submitRegistration(userId)
                navController.popBackStack()
                navController.navigate("volunteer_main")},
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = registration.phoneNum.isNotEmpty() &&
                    registration.emgContact.isNotEmpty() &&
                    registration.emgNum.isNotEmpty()
        ) {
            Text("Register as Volunteer")
        }
    }
}