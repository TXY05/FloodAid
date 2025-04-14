package com.example.floodaid.screen

import BottomBar
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.floodaid.models.ForumComment
import com.example.floodaid.models.ForumPost
import com.example.floodaid.viewmodel.ForumViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Forum(
    navController: NavHostController,
) {
    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Forum", fontSize = 50.sp)
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
    onPostClick: (String) -> Unit // Navigate to comments screen
) {
    val posts = viewModel.posts

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
