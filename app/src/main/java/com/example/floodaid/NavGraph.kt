package com.example.floodaid

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.floodaid.models.Screen
import com.example.floodaid.screen.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(route = Screen.Dashboard.route) {
            Dashboard(navController = navController)
        }
        composable(route = Screen.FloodStatus.route) {
            FloodStatus(navController = navController)
        }

        composable(route = Screen.Forum.route) {
            Forum(navController = navController)
        }

        composable(route = Screen.Map.route) {
            Map(navController = navController)
        }

        composable(route = Screen.Notification.route) {
            Notification(navController = navController)
        }

        composable(route = Screen.Profile.route) {
            Profile(navController = navController)
        }

        composable(route = Screen.Signup.route) {
            Signup(navController = navController)
        }

        composable(route = Screen.Volunteer.route) {
            Volunteer(navController = navController)
        }
    }
}