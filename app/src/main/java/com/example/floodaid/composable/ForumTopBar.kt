package com.example.floodaid.composable

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.floodaid.screen.forum.getCurrentUserProfileImageUrl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumTopBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var imageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        imageUrl = getCurrentUserProfileImageUrl()
    }

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
            Text(
                text = "Search Forum",
                color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                fontSize = 17.sp
            )
        },
        actions = {
            AsyncImage(
                model = imageUrl,
                contentDescription = "User Profile Image",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
        }
    )
}

suspend fun getCurrentUserProfileImageUrl(): String? {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null

    return try {
        val document = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .await()

        document.getString("profilePictureUrl") // Adjust the field name if needed
    } catch (e: Exception) {
        null
    }
}