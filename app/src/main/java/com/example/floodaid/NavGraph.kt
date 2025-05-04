package com.example.floodaid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.floodaid.models.Screen
import com.example.floodaid.roomDatabase.Database.FloodAidDatabase
import com.example.floodaid.screen.*
import com.example.floodaid.screen.floodstatus.FloodStatusRepository
import com.example.floodaid.screen.forum.Forum
import com.example.floodaid.screen.forum.ForumEvent
import com.example.floodaid.screen.forum.ForumPostState
import com.example.floodaid.screen.login.Login
import com.example.floodaid.screen.login.RegisterProfile
import com.example.floodaid.screen.login.Signup
import com.example.floodaid.screen.login.Welcome
import com.example.floodaid.screen.login.WelcomeLoading
import com.example.floodaid.screen.map_UI.Map
import com.example.floodaid.screen.volunteer.Volunteer
import com.example.floodaid.viewmodel.AuthViewModel
import com.example.floodaid.viewmodel.FloodStatusViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    state: ForumPostState,
    onEvent: (ForumEvent) -> Unit,
    authViewModel: AuthViewModel,
) {
    NavHost(navController = navController, startDestination = Screen.WelcomeLoading.route) {
        composable(route = Screen.Dashboard.route) {
            Dashboard(navController = navController, authViewModel)
        }
        composable(route = Screen.FloodStatus.route) {
            val context = LocalContext.current
            val database = FloodAidDatabase.getInstance(context)
            val repository = FloodStatusRepository(database.floodStatusDao())
            val viewModel = FloodStatusViewModel(repository)
            FloodStatus(navController = navController, viewModel = viewModel, database = database)

            LaunchedEffect(Unit) {
                repository.initializePredefinedDistricts()
            }
        }

        composable(route = Screen.Forum.route) {
            Forum(
                navController = navController,
                state = state,
                onEvent = onEvent,
            )
        }

//        composable(route = Screen.Map.route) {
//            Map(navController = navController)
//        }
        composable(route = Screen.Map.route) {
            key("persistent_map") {
                Map(navController = navController)
            }
        }

        composable(route = Screen.Notification.route) {
            Notification(navController = navController)
        }

        composable(route = Screen.Profile.route) {
            Profile(navController = navController)
        }

        composable(route = Screen.Signup.route) {
            Signup(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(route = Screen.Volunteer.route) {
            Volunteer(navController = navController)
//            Profile(navController = navController)
        }

        composable(route = Screen.Welcome.route) {
            Welcome(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(route = Screen.Login.route) {
            Login(
                navController = navController,
                authViewModel = authViewModel,
            )
        }

        composable(route = Screen.WelcomeLoading.route) {
            WelcomeLoading(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(route = Screen.RegisterProfile.route) {
            RegisterProfile(
                navController = navController,
            )
        }
    }
}