package com.example.floodaid.screen.volunteer

import BottomBar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.jetpackcomposeauthui.components.CButton
import com.example.jetpackcomposeauthui.components.CTextField
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVolunteerEvent(
    eventId: String,
    navController: NavHostController,
    viewModel: VolunteerViewModel = viewModel(),
    profileViewModel: ProfileViewModel
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )

    val eventState by remember(eventId) {
        viewModel.getEvent(eventId)
    }.collectAsState()

    var date by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

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
        val event = eventState
        if (event == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn (
                modifier = Modifier
                    .fillMaxSize().padding(horizontal = 40.dp)
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item{
                    Text(
                        text = "Edit Volunteer Event",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                item{
                    Text(
                        text = "Event ID: ${event.firestoreId}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                item{
                    Text(
                        text = "User: ${event.userId}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                item{
                    VTextField(hint = "Description: ${event.description}", value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth())
                }

                item{
                    VTextField(hint = "District: ${event.district}", value = district, onValueChange = { district = it }, modifier = Modifier.fillMaxWidth())
                }

                item{
                    Text(
                        text = "Date: ${event.date}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 10.dp)
                    )
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
                        VTextField(hint = "Start Time (HH:MM): ${event.startTime}", value = startTime, onValueChange = { startTime = it }, modifier = Modifier.weight(1f))
                        VTextField(hint = "End Time (HH:MM): ${event.endTime}", value = endTime, onValueChange = { endTime = it }, modifier = Modifier.weight(1f))
                    }
                }

                item{
                    VButton(
                        onClick = {
                            if (date.isEmpty()) date = event.date
                            if (startTime.isEmpty()) startTime = event.startTime
                            if (endTime.isEmpty()) endTime = event.endTime
                            if (description.isEmpty()) description = event.description
                            if (district.isEmpty()) district = event.district

                            val newEvent = event.copy(
                                date = date,
                                startTime = startTime,
                                endTime = endTime,
                                description = description,
                                district = district,
                                userId = userId
                            )
                            viewModel.updateEvent(newEvent)
                            navController.popBackStack()
                        },
                        text = "Update Event"
                    )
                }
            }
        }
    }
}