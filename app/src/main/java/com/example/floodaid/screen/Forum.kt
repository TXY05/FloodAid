package com.example.floodaid.screen

import BottomBar
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.floodaid.composable.ForumTopBar
import com.example.floodaid.models.ForumComment
import com.example.floodaid.models.ForumPost
import com.example.floodaid.viewmodel.ForumViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Forum(
    navController: NavHostController,
    state: ForumPostState,
    onEvent: (ForumEvent) -> Unit,
) {

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { paddingValues ->
        ForumScreen(
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ForumTopBar(scrollBehavior = scrollBehavior)

        }
    ) { paddingValues ->
        ForumContent(
            paddingValues = paddingValues
        )
    }
}

@Composable
fun ForumContent(paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 16.dp
        )
    ) {
        items(10) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(400.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,

                ) {
                Text(
                    text = "Forum Post",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@SuppressLint("StaticFieldLeak")
val db = FirebaseFirestore.getInstance()

fun addPost(title: String, content: String, authorId: String) {
    val newPost = ForumPost(title, content, authorId)

    db.collection("posts")
        .add(newPost)
        .addOnSuccessListener { docRef ->
            Log.d("Forum", "Post added with ID: ${docRef.id}")
        }
        .addOnFailureListener { e ->
            Log.w("Forum", "Error adding post", e)
        }
}

fun addComment(postId: String, content: String, authorId: String) {
    val comment = ForumComment(content, authorId)

    db.collection("posts")
        .document(postId)
        .collection("comments")
        .add(comment)
        .addOnSuccessListener {
            Log.d("Forum", "Comment added!")
        }
        .addOnFailureListener { e ->
            Log.w("Forum", "Error adding comment", e)
        }
}

fun getPosts(onResult: (List<Pair<String, ForumPost>>) -> Unit) {
    db.collection("posts")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) {
                Log.w("Forum", "Listen failed.", e)
                return@addSnapshotListener
            }

            val posts = snapshot.documents.mapNotNull { doc ->
                val post = doc.toObject(ForumPost::class.java)
                if (post != null) Pair(doc.id, post) else null
            }

            onResult(posts)
        }
}

fun getComments(postId: String, onResult: (List<ForumComment>) -> Unit) {
    db.collection("posts")
        .document(postId)
        .collection("comments")
        .orderBy("timestamp", Query.Direction.ASCENDING)
        .addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) {
                Log.w("Forum", "Listen failed.", e)
                return@addSnapshotListener
            }

            val comments = snapshot.documents.mapNotNull {
                it.toObject(ForumComment::class.java)
            }

            onResult(comments)
        }
}

@Composable
fun PostListScreen(
    viewModel: ForumViewModel = viewModel(),
    onPostClick: (String) -> Unit, // Navigate to comments screen
) {
    //val posts = viewModel.posts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Forum", fontSize = 24.sp, modifier = Modifier.padding(bottom = 8.dp))

        LazyColumn {
            /*items(posts) { (postId, post) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onPostClick(postId) },
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(post.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(post.content, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }*/
        }
    }
}

