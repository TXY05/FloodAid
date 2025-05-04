package com.example.floodaid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.floodaid.ui.theme.FloodAidTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.floodaid.viewmodel.ForumViewModel
import kotlin.getValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.floodaid.roomDatabase.Database.FloodAidDatabase
import com.example.floodaid.roomDatabase.Repository.VolunteerRepository
import com.example.floodaid.utils.GeocodingHelper
import com.example.floodaid.roomDatabase.Database.FloodAidDatabase
import com.example.floodaid.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.floodaid.screen.login.AuthRepository
import com.example.floodaid.screen.login.AuthViewModelFactory
import com.example.floodaid.screen.volunteer.VolunteerViewModel
import com.example.floodaid.screen.volunteer.VolunteerViewModelFactory

private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

//class MainActivity : AppCompatActivity(), OnMapReadyCallback{
@Suppress("UNCHECKED_CAST")
//class MainActivity : ComponentActivity() {
class MainActivity : AppCompatActivity() {

    private val db by lazy {
        FloodAidDatabase.getInstance(applicationContext)
    }

    private val viewModel by viewModels<ForumViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as FloodAidApp
                return ForumViewModel(app.database.forumDao()) as T
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val firebaseAuth = FirebaseAuth.getInstance()
        val authRepository = AuthRepository(application, firebaseAuth)
        val authViewModelFactory = AuthViewModelFactory(authRepository)
        val authViewModel = ViewModelProvider(this, authViewModelFactory)[AuthViewModel::class.java]
        val floodAidDatabase = FloodAidDatabase.getInstance(applicationContext)
        val volunteerRepository = VolunteerRepository(
            floodAidDatabase.volunteerDao(),
            floodAidDatabase.volunteerEventHistoryDao()
        )
        val volunteerViewModel: VolunteerViewModel by viewModels {
            VolunteerViewModelFactory(volunteerRepository)
        }


        setContent {
            val navController = rememberNavController()
            val state by viewModel.state.collectAsState()
            val authViewModel: AuthViewModel by viewModels()


            FloodAidTheme {
                NavGraph(
                    navController = navController,
                    state = state,
                    onEvent = viewModel::onEvent,
                    authViewModel = authViewModel,
                    volunteerViewModel = volunteerViewModel
                )
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // Explain why you need permission
                showPermissionRationale()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Needed")
            .setMessage("This app needs location permission to calculate distances to shelters")
            .setPositiveButton("Grant") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

}

@Composable
@Preview(showBackground = true)
fun Preview() {
    //Dashboard()
}