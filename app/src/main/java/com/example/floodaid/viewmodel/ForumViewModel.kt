package com.example.floodaid.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.floodaid.models.ForumPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ForumViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _posts = mutableStateListOf<Pair<String, ForumPost>>()
    val posts: List<Pair<String, ForumPost>> = _posts

    init {
        loadPosts()
    }

    private fun loadPosts() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                _posts.clear()
                _posts.addAll(snapshot.documents.mapNotNull { doc ->
                    val post = doc.toObject(ForumPost::class.java)
                    if (post != null) {
                        Pair(doc.id, post)
                    } else {
                        null
                    }
                })
            }
    }
}
