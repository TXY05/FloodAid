package com.example.floodaid.screen.volunteer

import BottomBar
import android.annotation.SuppressLint
import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.floodaid.composable.VolunteerTopBar
import com.example.floodaid.models.VolunteerEvent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Volunteer(
    navController: NavHostController,
    viewModel: VolunteerViewModel
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    val listState = rememberLazyListState()
    val events by viewModel.events.collectAsState()

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
        bottomBar = { BottomBar(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addVolunteerEvent") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+") // Or use Icon(Icons.Default.Add, contentDescription = null)
            }
        }
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
            EventListScreen(
                viewModel = viewModel,
                onEventClick = { event ->
                    navController.navigate("volunteerDetail/${event.firestoreId}")
                },
                listState = listState,
//                modifier = Modifier.offset { IntOffset(0, currentCalSize) }
//                calendarHeight = currentCalSize.dp
            )
        }
    }
}

@Composable
fun CalendarViewComposable(
    viewModel: VolunteerViewModel,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            CalendarView(context).apply {
                setOnDateChangeListener { _, year, month, day ->
                    val formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                        month + 1, day, year)
                    viewModel.setSelectedDate(formattedDate)
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CalendarScreen(
    viewModel: VolunteerViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clip(RoundedCornerShape(50.dp)),
//            .height(size),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CalendarViewComposable(viewModel = viewModel)
        Text(
            text = selectedDate?.let { "Selected Date: $it" } ?: "No Date Selected",
            style = MaterialTheme.typography.bodyLarge
        )

        if (selectedDate != null) {
            Button(
                onClick = { viewModel.clearDateFilter() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Show All Events")
            }
        }
    }
}

@Composable
fun EventListScreen(
    viewModel: VolunteerViewModel,
    onEventClick: (VolunteerEvent) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val filteredEvents by viewModel.filteredEvents.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    LazyColumn(
        state = listState,
        modifier = modifier
            .padding(horizontal = 10.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CalendarScreen(viewModel = viewModel)
        }
        if (filteredEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedDate != null) {
                            "No events on $selectedDate"
                        } else {
                            "No events available"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(filteredEvents) { event ->
                EventCard(event, onClick = { onEventClick(event) })
            }
        }
    }
}

@Composable
fun EventCard(event: VolunteerEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.description, style = MaterialTheme.typography.titleLarge)
            val parsedDate = try {
                SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(event.date)
            } catch (e: Exception) {
                null
            }
            val formattedDate = parsedDate?.let {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
            } ?: event.date
            Text(text = formattedDate, style = MaterialTheme.typography.bodyMedium)
            Text(text = event.district, style = MaterialTheme.typography.bodySmall)
        }
    }
}