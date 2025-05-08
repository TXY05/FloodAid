package com.example.floodaid.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.floodaid.R
import com.example.floodaid.screen.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerTopBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    onHistoryClick: () -> Unit,
    viewModel: ProfileViewModel
) {
    val profileState by viewModel.profile.collectAsState()

    TopAppBar(
        modifier = modifier
            .statusBarsPadding(),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.6f)
        ),
        windowInsets = WindowInsets(top = 0.dp),
        title = {

            Text(
                text = "Volunteer Event",
                color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
            )
        },
        navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        actions = {
            val imageUrl = profileState?.profilePictureUrl

            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { navController.navigate("profile") },
                    error = painterResource(R.drawable.ic_user)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Default Profile",
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { navController.navigate("profile") }
                )
            }

            IconButton(onClick = onHistoryClick) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History"
                )
            }
        },
    )
}