package com.example.floodaid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.floodaid.roomDatabase.Repository.FirestoreRepository
import com.example.floodaid.roomDatabase.Repository.ProfileRepository
import com.example.floodaid.roomDatabase.database.FloodAidDatabase
import com.example.floodaid.roomDatabase.repository.MapRepository
import com.example.floodaid.roomDatabase.repository.VolunteerRepository
import com.example.floodaid.screen.forum.ForumViewModelFactory
import com.example.floodaid.screen.login.AuthRepository
import com.example.floodaid.screen.login.AuthViewModelFactory
import com.example.floodaid.screen.map_UI.MapViewModel
import com.example.floodaid.screen.map_UI.MapViewModelFactory
import com.example.floodaid.screen.profile.ProfileViewModel
import com.example.floodaid.screen.profile.ProfileViewModelFactory
import com.example.floodaid.screen.volunteer.VolunteerViewModel
import com.example.floodaid.screen.volunteer.VolunteerViewModelFactory
import com.example.floodaid.ui.theme.FloodAidTheme
import com.example.floodaid.viewmodel.AuthViewModel
import com.example.floodaid.viewmodel.ForumViewModel
import com.google.firebase.auth.FirebaseAuth

private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

//class MainActivity : AppCompatActivity(), OnMapReadyCallback{
@Suppress("UNCHECKED_CAST")
//class MainActivity : ComponentActivity() {
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Auth ViewModel
        val firebaseAuth = FirebaseAuth.getInstance()
        val authRepository = AuthRepository(application, firebaseAuth)
        val authViewModelFactory = AuthViewModelFactory(authRepository)
        val authViewModel = ViewModelProvider(this, authViewModelFactory)[AuthViewModel::class.java]

        // Forum ViewModel
        val forumDao = FloodAidDatabase.getInstance(applicationContext).forumDao()
        val forumViewModelFactory = ForumViewModelFactory(forumDao, application)
        val forumViewModel = ViewModelProvider(this, forumViewModelFactory).get(ForumViewModel::class.java)


        // Volunteer ViewModel
        val floodAidDatabase = FloodAidDatabase.getInstance(applicationContext)
        val volunteerRepository = VolunteerRepository(
            floodAidDatabase.volunteerDao(),
            floodAidDatabase.volunteerEventHistoryDao(),
            floodAidDatabase.volunteerProfileDao()
        )
        val volunteerViewModel: VolunteerViewModel by viewModels {
            VolunteerViewModelFactory(volunteerRepository, firebaseAuth)
        }

        // Map ViewModel
        val repository = MapRepository(
            dao = FloodAidDatabase.getInstance(application).MapDao(),
            FirestoreRepository = FirestoreRepository()
        )
        val factory = MapViewModelFactory(application, repository)
        val mapViewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]

        // UserProfile ViewModel
        val profileRepository = ProfileRepository(floodAidDatabase.userProfileDao())
        val profileViewModel: ProfileViewModel by viewModels {
            ProfileViewModelFactory(profileRepository, firebaseAuth)
        }

        setContent {
            val navController = rememberNavController()

            FloodAidTheme {
                NavGraph(
                    navController = navController,
                    onEvent = forumViewModel::onEvent,
                    authViewModel = authViewModel,
                    volunteerViewModel = volunteerViewModel,
                    forumViewModel = forumViewModel,
                    mapViewModel = mapViewModel,
                    profileViewModel = profileViewModel
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