package com.example.floodaid.screen

import BottomBar
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.floodaid.composable.TopBar
import com.example.floodaid.roomDatabase.Database.FloodAidDatabase
import com.example.floodaid.roomDatabase.Entities.Border
import com.example.floodaid.roomDatabase.Entities.District
import com.example.floodaid.roomDatabase.Entities.FloodMarker
import com.example.floodaid.roomDatabase.Entities.Shelter
import com.example.floodaid.roomDatabase.Entities.State
import com.example.floodaid.roomDatabase.Repository.MapRepository
import com.example.floodaid.screen.map_UI.MapViewModel
import com.example.floodaid.screen.map_UI.MapUiState
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.days
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Profile(
    navController: NavHostController,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    Scaffold(
        topBar = { TopBar(scrollBehavior = scrollBehavior) },
        bottomBar = { BottomBar(navController = navController) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Profile", fontSize = 50.sp)
        }
        Column{

        }
    }
}