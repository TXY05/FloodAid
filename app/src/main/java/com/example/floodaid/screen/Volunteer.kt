package com.example.floodaid.screen

import BottomBar
import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.floodaid.composable.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Volunteer(
    navController: NavHostController,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    Scaffold(
        topBar = { TopBar(scrollBehavior = scrollBehavior) },
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val sampleEvents = listOf(
                Event(1, "Clean-Up in Klang", "April 20, 2025", "Hulu Langat"),
                Event(2, "Supply Drop to Kelantan", "April 22, 2025", "Kuala Lumpur"),
                Event(3, "Medical Aid Setup", "April 25, 2025", "Petaling")
            )

            EventListScreen(events = sampleEvents, onEventClick = {})
        }
    }
}

@Composable
fun EventListScreen(events: List<Event>, onEventClick: (Event) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events) { event ->
                EventCard(event, onClick = { onEventClick(event) })
            }
        }
}

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleLarge)
            Text(text = event.date, style = MaterialTheme.typography.bodyMedium)
            Text(text = event.location, style = MaterialTheme.typography.bodySmall)
        }
    }
}

data class Event(
    val id: Int,
    val title: String,
    val date: String,
    val location: String
)

@Preview(showBackground = true)
@Composable
fun EventListPreview() {
    Volunteer(navController = rememberNavController())
}
