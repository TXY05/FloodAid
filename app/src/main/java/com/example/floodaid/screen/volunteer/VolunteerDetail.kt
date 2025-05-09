import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.floodaid.screen.profile.ProfileViewModel
import com.example.floodaid.screen.volunteer.VolunteerViewModel
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerDetail(
    eventId: String,
    navController: NavHostController,
    viewModel: VolunteerViewModel,
    profileViewModel: ProfileViewModel
) {
    val eventState by viewModel.getEvent(eventId).collectAsState(initial = null)

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )

    val history by viewModel.history.collectAsState()
    val alrApplied = history.any { it.eventId == eventId }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                .background(MaterialTheme.colorScheme.background)
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

                        EventDetail(
                            event = event,
                            formattedDate = formattedDate,
                            userId = userId,
                            alrApplied = alrApplied,
                            navController = navController,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxHeight()
                                .padding(start = 150.dp),
                        )
                    }
                }else {
                    Column{
                        EventDetail(
                            event = event,
                            formattedDate = formattedDate,
                            userId = userId,
                            alrApplied = alrApplied,
                            navController = navController,
                            viewModel = viewModel,
                            isShow = true,
                            modifier = Modifier
                                .fillMaxSize().padding(horizontal = 40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetail(
    event: VolunteerEvent,
    formattedDate: String,
    userId: String,
    alrApplied: Boolean,
    navController: NavHostController,
    viewModel: VolunteerViewModel,
    isShow: Boolean = false,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DisplayField(label = "Description", value = event.description, icon = Icons.Default.Description)
        }
        item {
            DisplayField(label = "District", value = event.district, icon = Icons.Default.Place)
        }
        item {
            DisplayField(label = "Date", value = formattedDate, icon = Icons.Default.CalendarToday)
        }
        item {
            DisplayField(label = "Start Time", value = event.startTime, icon = Icons.Default.AccessTime)
        }
        item {
            DisplayField(label = "End Time", value = event.endTime, icon = Icons.Default.Schedule)
        }

        if (isShow) {
            item {
                ActionButtons(event, userId, alrApplied, navController, viewModel)
            }
        }
    }
}

@Composable
fun DisplayField(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 8.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
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
                        fontSize = 15.sp,
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
                        fontSize = 15.sp,
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
        Spacer(modifier = Modifier.height(12.dp))
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