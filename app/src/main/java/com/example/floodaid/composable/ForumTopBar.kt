package com.example.floodaid.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.floodaid.R
import com.example.floodaid.screen.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumTopBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit, // Callback for when the search query changes
    navController: NavHostController,
    viewModel: ProfileViewModel
) {
    val profileState by viewModel.profile.collectAsState()

    TopAppBar(
        modifier = modifier
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(32.dp)),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        windowInsets = WindowInsets(top = 0.dp),
        title = {
            // Search Bar in the TopAppBar
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Search Posts") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, // Remove bottom line when focused
                    unfocusedIndicatorColor = Color.Transparent // Remove bottom line when unfocused
                )
            )
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
        }
    )
}
