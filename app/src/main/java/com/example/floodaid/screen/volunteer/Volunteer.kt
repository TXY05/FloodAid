package com.example.floodaid.screen.volunteer

import BottomBar
import android.annotation.SuppressLint
import android.widget.CalendarView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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

    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { TopBar(scrollBehavior = scrollBehavior) },
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

//        val minCalSize = 0
//        val maxCalSize = 370
//
//        var currentCalSize by remember { mutableStateOf(maxCalSize) }
//
//        val nestedScrollConnection = remember {
//            object : NestedScrollConnection {
//                override fun onPreScroll(
//                    available: Offset,
//                    source: NestedScrollSource
//                ): Offset {
//                    val delta = available.y.toInt()
//
//                    val newCalSize = currentCalSize + delta
//                    val previousCalSize = currentCalSize
//                    currentCalSize =
//                        newCalSize.coerceIn(minCalSize, maxCalSize)
//                    val consumed = currentCalSize - previousCalSize
//
//                    return Offset(0f, consumed.toFloat())
//                }
//            }
//        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
//                .nestedScroll(nestedScrollConnection)
        ) {
            val sampleEvents = listOf(
                Event(1, "Clean-Up in Klang", "April 20, 2025", "Hulu Langat"),
                Event(2, "Supply Drop to Kelantan", "April 22, 2025", "Kuala Lumpur"),
                Event(3, "Medical Aid Setup", "April 25, 2025", "Petaling"),
                Event(1, "Clean-Up in Klang", "April 20, 2025", "Hulu Langat"),
                Event(2, "Supply Drop to Kelantan", "April 22, 2025", "Kuala Lumpur"),
                Event(3, "Medical Aid Setup", "April 25, 2025", "Petaling"),
                Event(1, "Clean-Up in Klang", "April 20, 2025", "Hulu Langat"),
                Event(2, "Supply Drop to Kelantan", "April 22, 2025", "Kuala Lumpur"),
                Event(3, "Medical Aid Setup", "April 25, 2025", "Petaling")
            )
            EventListScreen(
                events = sampleEvents,
                onEventClick = {},
                listState = listState,
//                modifier = Modifier.offset { IntOffset(0, currentCalSize) }
//                calendarHeight = currentCalSize.dp
            )
        }
    }
}

@Composable
fun CalendarViewComposable(
    onDateSelected: (year: Int, month: Int, dayOfMonth: Int) -> Unit
) {
    AndroidView(
        factory = { context ->
            CalendarView(context).apply {
                setOnDateChangeListener { _, year, month, dayOfMonth ->
                    onDateSelected(year, month, dayOfMonth)
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CalendarScreen() {
    var selectedDate by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
//            .height(size),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CalendarViewComposable { year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (selectedDate.isNotEmpty()) "Selected Date: $selectedDate" else "Please select a date",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun EventListScreen(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CalendarScreen()
        }
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
