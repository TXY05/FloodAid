package com.example.floodaid.screen.volunteer

import BottomBar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.floodaid.composable.VButton
import com.example.floodaid.composable.VTextField
import com.example.floodaid.composable.VolunteerTopBar
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.screen.login.datePickerFieldToModal
import com.example.floodaid.screen.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVolunteerEvent(
    navController: NavHostController,
    viewModel: VolunteerViewModel = viewModel(),
    profileViewModel: ProfileViewModel
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )

    var date by rememberSaveable { mutableStateOf("") }
    var startTime by rememberSaveable { mutableStateOf("") }
    var endTime by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("") }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var showError by remember { mutableStateOf(false) }

    val allFieldsFilled = description.isNotBlank() &&
            district.isNotBlank() &&
            date.isNotBlank() &&
            startTime.isNotBlank() &&
            endTime.isNotBlank()

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            VolunteerTopBar(
                scrollBehavior = scrollBehavior,
                navController = navController,
                onHistoryClick = { navController.navigate("volunteerHistory") },
                viewModel = profileViewModel
            )
        },
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize().padding(horizontal = 40.dp)
                .padding(innerPadding)
                .padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item{
                Text(
                    text = "Create New Volunteer Event",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item{
                VTextField(hint = "Description", value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth())
            }

            item{
                VTextField(hint = "District", value = district, onValueChange = { district = it }, modifier = Modifier.fillMaxWidth())
            }

            item{
                datePickerFieldToModal(
                    birthOfDate = date,
                    onDateSelected = { date = it }
                )
            }

            item{
                Row (
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ){
                    VTextField(hint = "Start Time (HH:MM)", value = startTime, onValueChange = { startTime = it }, modifier = Modifier.weight(1f))
                    VTextField(hint = "End Time (HH:MM)", value = endTime, onValueChange = { endTime = it }, modifier = Modifier.weight(1f))
                }

                if (showError) {
                    Text(
                        text = "Start/End Time must be in HH:MM format (e.g., 09:30)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item{
                VButton(
                    onClick = {
                        val isStartValid = viewModel.isValidTimeFormat(startTime)
                        val isEndValid = viewModel.isValidTimeFormat(endTime)

                        showError = !isStartValid || !isEndValid
                        if (showError) return@VButton

                        if (!allFieldsFilled) return@VButton

                        val newEvent = VolunteerEvent(
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            description = description,
                            district = district,
                            userId = userId
                        )
                        viewModel.insertEvent(newEvent)
                        navController.popBackStack()
                    },
                    text = "Create Event",
                    enabled = !showError && allFieldsFilled
                )
                if (!allFieldsFilled) {
                    Text(
                        text = "All fields are required.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}