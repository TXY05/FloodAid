import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.floodaid.composable.VolunteerTopBar
import com.example.floodaid.models.VolunteerEvent
import com.example.floodaid.models.VolunteerEventHistory
import com.example.floodaid.screen.volunteer.VolunteerHistory
import com.example.floodaid.screen.volunteer.VolunteerViewModel
import com.example.jetpackcomposeauthui.components.CButton
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerDetail(
    eventId: String,
    navController: NavHostController,
    viewModel: VolunteerViewModel
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

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            VolunteerTopBar(
                scrollBehavior = scrollBehavior,
                navController = navController,
                onHistoryClick = { navController.navigate("volunteerHistory") }
            )
        },
        bottomBar = { BottomBar(navController = navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val event = eventState
            if (event == null) {
                CircularProgressIndicator()
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EventDetailItem(label = "Event ID", value = event.firestoreId)
                    EventDetailItem(label = "User ID", value = event.userId)
                    EventDetailItem(label = "Description", value = event.description)
                    EventDetailItem(label = "District", value = event.district)
                    EventDetailItem(label = "Date", value = event.date)
                    EventDetailItem(label = "Start Time", value = event.startTime)
                    EventDetailItem(label = "End Time", value = event.endTime)

                    if (alrApplied){
                        CButton(
                            onClick = {},
                            text = "Already Applied Event",
                            enabled = false
                        )
                    }
                    else{
                        CButton(
                            onClick = {
                                viewModel.applyEvent(userId = userId, eventId = event.firestoreId)
                                navController.popBackStack()
                            },
                            text = "Apply Event"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetailItem(label: String, value: String) {
    Column {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = value, fontSize = 16.sp)
    }
}

fun CheckIfApplied(){
    var alrApplied: Boolean = false


}