package com.example.floodaid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.floodaid.screen.Dashboard
import com.example.floodaid.screen.*
import com.example.floodaid.ui.theme.FloodAidTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.floodaid.viewmodel.ForumViewModel
import kotlin.getValue
import com.google.android.gms.maps.SupportMapFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.floodaid.viewmodel.AuthViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng

//class MainActivity : AppCompatActivity(), OnMapReadyCallback{
@Suppress("UNCHECKED_CAST")
//class MainActivity : ComponentActivity() {
class MainActivity : AppCompatActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            ForumDatabase::class.java,
            "forumposts.db"
        ).build()
    }

    private val viewModel by viewModels<ForumViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ForumViewModel(db.dao) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val state by viewModel.state.collectAsState()
            val authViewModel: AuthViewModel by viewModels()
            FloodAidTheme {

                NavGraph(navController,state = state, onEvent = viewModel::onEvent,authViewModel)
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun Preview(){
    //Dashboard()
}