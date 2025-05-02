package com.example.floodaid

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.floodaid.ui.theme.FloodAidTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.floodaid.viewmodel.ForumViewModel
import kotlin.getValue
import androidx.appcompat.app.AppCompatActivity
import com.example.floodaid.screen.forum.ForumDatabase
import com.example.floodaid.screen.login.AuthRepository
import com.example.floodaid.screen.login.AuthViewModelFactory
import com.example.floodaid.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

//class MainActivity : AppCompatActivity(), OnMapReadyCallback{
@Suppress("UNCHECKED_CAST")
//class MainActivity : ComponentActivity() {
class MainActivity : AppCompatActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext, ForumDatabase::class.java, "forumposts.db"
        ).build()
    }

    private val viewModel by viewModels<ForumViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ForumViewModel(db.dao) as T
                }
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val firebaseAuth = FirebaseAuth.getInstance()
        val authRepository = AuthRepository(application, firebaseAuth)
        val authViewModelFactory = AuthViewModelFactory(authRepository)
        val authViewModel = ViewModelProvider(this, authViewModelFactory)[AuthViewModel::class.java]

        setContent {
            val navController = rememberNavController()
            val state by viewModel.state.collectAsState()

            FloodAidTheme {
                NavGraph(
                    navController = navController,
                    state = state,
                    onEvent = viewModel::onEvent,
                    authViewModel = authViewModel
                )
            }
        }
    }

}

@Composable
@Preview(showBackground = true)
fun Preview() {
    //Dashboard()
}