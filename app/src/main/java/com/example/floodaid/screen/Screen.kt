package com.example.floodaid.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    data object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        icon = Icons.Default.Home
    )

    data object FloodStatus : Screen(
        route = "floodStatus",
        title = "Status",
        icon = Icons.Default.Details
    )

     data object Forum : Screen(
        route = "forum",
        title = "Forum",
        icon = Icons.Default.Forum
    )

    data object Map : Screen(
        route = "map",
        title = "Map",
        icon = Icons.Default.LocationOn
    )

    data object Notification : Screen(
        route = "notification",
        title = "Notification",
        icon = Icons.Default.Home
    )

    data object Profile : Screen(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person
    )

    data object Signup : Screen(
        route = "signup",
        title = "Signup",
        icon = Icons.Default.AccountCircle
    )

    data object Volunteer : Screen(
        route = "volunteer",
        title = "Volunteer",
        icon = Icons.Default.Home
    )

}