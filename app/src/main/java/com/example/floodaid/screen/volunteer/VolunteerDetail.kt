import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.floodaid.composable.VButton
import com.example.floodaid.composable.VolunteerTopBar
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import com.example.floodaid.screen.profile.ProfileViewModel
import com.example.floodaid.screen.volunteer.VolunteerHistory
import com.example.floodaid.screen.volunteer.VolunteerViewModel
import com.example.floodaid.screen.volunteer.checkIfUserIsVolunteer
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily
import com.example.jetpackcomposeauthui.components.CButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerDetail(
    eventId: String,
    navController: NavHostController,
    viewModel: VolunteerViewModel,
    profileViewModel: ProfileViewModel
) {
    val eventState by remember(eventId) {
        viewModel.getEvent(eventId)
    }.collectAsState()

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )

    val history by viewModel.history.collectAsState()
    val alrApplied = history.any { it.eventId == eventId }

    var userName by remember { mutableStateOf("") }

    LaunchedEffect(eventState) {
        eventState?.let {
            userName = getUsername(it.userId)
        }
    }

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
        BoxWithConstraints(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val isWideScreen = maxWidth > 600.dp

            val event = eventState
            if (event == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val parsedDate = try {
                    SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(event.date)
                } catch (e: Exception) {
                    null
                }
                val formattedDate = parsedDate?.let {
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                } ?: event.date

                val detailItems = listOf(
                    "Event ID" to event.firestoreId,
                    "User Name" to userName,
                    "Description" to event.description,
                    "District" to event.district,
                    "Date" to formattedDate,
                    "Start Time" to event.startTime,
                    "End Time" to event.endTime
                )

                if (isWideScreen) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ActionButtons(event, userId, alrApplied, navController, viewModel)
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxHeight()
                                .padding(start = 150.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            detailItems.forEach { (label, value) ->
                                item {
                                    EventDetailItem(label = label, value = value)
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        detailItems.forEach { (label, value) ->
                            EventDetailItem(label = label, value = value)
                        }

                        ActionButtons(event, userId, alrApplied, navController, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    event: VolunteerEvent,
    userId: String,
    alrApplied: Boolean,
    navController: NavHostController,
    viewModel: VolunteerViewModel,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (userId == event.userId) {
        Row (
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ){
            Button(onClick = {
                navController.navigate("editVolunteerEvent/${event.firestoreId}")
            },
                colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                disabledContainerColor = colorScheme.primary.copy(alpha = 0.4f),
                disabledContentColor = colorScheme.onPrimary.copy(alpha = 0.4f)
            ),
                modifier = Modifier.weight(1f)) {
                Text("Edit Event",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontFamily = AlegreyaSansFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onPrimary.copy(alpha = 0.4f) // optional, reinforces disabled look
                    ))
            }

            Button(onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.secondary,
                    contentColor = colorScheme.onSecondary,
                    disabledContainerColor = colorScheme.secondary.copy(alpha = 0.4f),
                    disabledContentColor = colorScheme.onSecondary.copy(alpha = 0.4f)
                ),
                modifier = Modifier.weight(1f)) {
                Text("Delete Event",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontFamily = AlegreyaSansFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSecondary.copy(alpha = 0.4f)
                    ))
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Confirmation") },
                text = {
                    Text("Are you sure you want to delete this event? This action cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteEventAndHistory(event) {
                                navController.navigate("volunteer_main") {
                                    popUpTo("volunteer_main") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (alrApplied) {
        VButton(onClick = {}, text = "Already Applied Event", enabled = false)
    } else {
        VButton(
            onClick = {
                viewModel.applyEvent(userId = userId, eventId = event.firestoreId)
                navController.popBackStack()
            },
            text = "Apply Event"
        )
    }
}

@Composable
fun EventDetailItem(label: String, value: String) {
    Column {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = value, fontSize = 16.sp)
    }
}

suspend fun getUsername(userId: String): String {
    return try {
        val db = FirebaseFirestore.getInstance()
        val document = db.collection("users").document(userId).get().await()
        document.getString("userName") ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}