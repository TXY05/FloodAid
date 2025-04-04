package com.example.floodaid.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        icon = Icons.Default.Home
    )

    object FloodStatus : Screen(
        route = "floodStatus",
        title = "Status",
        icon = Icons.Default.Notifications
    )

    object Forum : Screen(
        route = "forum",
        title = "Forum",
        icon = Icons.Default.Email
    )

    object Map : Screen(
        route = "map",
        title = "Map",
        icon = Icons.Default.LocationOn
    )

    object Notification : Screen(
        route = "notification",
        title = "Notification",
        icon = Icons.Default.Home
    )

    object Profile : Screen(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person
    )

    object Signup : Screen(
        route = "signup",
        title = "Signup",
        icon = Icons.Default.AccountCircle
    )

    object Volunteer : Screen(
        route = "volunteer",
        title = "Volunteer",
        icon = Icons.Default.Home
    )

}