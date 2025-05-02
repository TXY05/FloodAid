package com.example.floodaid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.floodaid.models.Screen
import com.example.floodaid.screen.*
import com.example.floodaid.screen.map_UI.Map
import com.example.floodaid.screen.volunteer.Volunteer
import com.example.floodaid.viewmodel.AuthViewModel

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
            FloodStatus(navController = navController)
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
                authViewModel = authViewModel
            )
        }

        composable(route = Screen.WelcomeLoading.route) {
            WelcomeLoading(
                navController = navController,
                authViewModel = authViewModel
            )
        }

    }
}