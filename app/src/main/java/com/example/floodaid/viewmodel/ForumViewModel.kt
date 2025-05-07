package com.example.floodaid.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodaid.screen.forum.ForumDao
import com.example.floodaid.screen.forum.ForumEvent
import com.example.floodaid.screen.forum.ForumPost
import com.example.floodaid.screen.forum.ForumSortType
import com.example.floodaid.screen.forum.ForumState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Suppress("DEPRECATION")
@OptIn(ExperimentalCoroutinesApi::class)
class ForumViewModel(
    application: Application, // Pass the application context here
    private val dao: ForumDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val _sortType = MutableStateFlow(ForumSortType.TIME_STAMP)
    private val _forumPosts = MutableStateFlow<List<ForumPost>>(emptyList())
    val forumPosts: StateFlow<List<ForumPost>> = _forumPosts
    private val _state = MutableStateFlow(ForumState())

    val state = combine(_state, _sortType, _forumPosts) { state, sortType, forumPosts ->
        state.copy(
            forumPosts = forumPosts,
            sortType = sortType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ForumState())

    val uid = FirebaseAuth.getInstance().currentUser?.uid?: ""

    fun onEvent(event: ForumEvent) {
        when (event) {
            is ForumEvent.DeleteForumPost -> {
                viewModelScope.launch {
                    dao.deleteForumPost(event.forumPost)
                }
            }

            is ForumEvent.SetRegion -> {
                _state.update {
                    it.copy(region = event.region)
                }
            }

            is ForumEvent.SetImages -> {
                _state.update {
                    it.copy(imageUrls = event.images)
                }
            }


            is ForumEvent.SaveForumPost -> {
                viewModelScope.launch {
                    // Get the current user UID
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) return@launch

                    // Assuming the forum post content and images are passed in the event
                    val forumPost = event.forumPost

                    // Create the post with author image and other details
                    val postWithAuthorImage = ForumPost(
                        id = forumPost.id,
                        content = forumPost.content,
                        authorId = uid,
                        authorName = forumPost.authorName,
                        timestamp = forumPost.timestamp,
                        region = forumPost.region,
                        authorImageUrl = forumPost.authorImageUrl, // Assuming photoUrl is already set
                        imageUrls = forumPost.imageUrls // List of image URLs
                    )

                    // Upsert the post into the local database (DAO)
                    dao.upsertForumPost(listOf(postWithAuthorImage)) // Wrap postWithAuthorImage in a List

                    // Clear the current state or reset if needed
                    _state.update { ForumState() }

                    val context = getApplication<Application>().applicationContext
                    clearRoomDatabaseIfOnline(context, dao)

                    // Save the post to Firestore
                    try {
                        val documentReference = FirebaseFirestore.getInstance()
                            .collection("forumPosts")
                            .document(postWithAuthorImage.id) // Use the same ID as in the Room post
                            .set(postWithAuthorImage) // This will insert or update the document with that ID
                            .await()
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error saving post: ${e.message}")
                    }
                }
            }



            ForumEvent.EditForumPost -> {
                val currentState = state.value
                val forumPost = ForumPost(
                    id=currentState.id,
                    content = currentState.content,
                    authorId = currentState.authorId, // Assign current user ID here
                    timestamp = Timestamp.now(),
                    region = currentState.region,
                    imageUrls = currentState.imageUrls
                )
                viewModelScope.launch {
                    //dao.upsertForumPost(event.fo)
                }
                _state.update { ForumState() } // Reset all fields
            }

            is ForumEvent.SetContent -> {
                _state.update {
                    it.copy(
                        content = event.content
                    )
                }
            }


            is ForumEvent.SortForumPost -> {
                _sortType.value = event.sortType
            }
        }
    }

    fun fetchAndSaveForumPosts() {
        viewModelScope.launch {
            val snapshot = firestore.collection("forumPosts").get().await()
            val posts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ForumPost::class.java)?.copy(id = doc.id)
            }
            dao.upsertForumPost(posts) // use upsert instead of insert
        }
    }


    init {
        // Collect the flow from Room
        viewModelScope.launch {
            dao.getForumPostOrderedByTimeStamp()
                .collect { posts ->
                    _forumPosts.value = posts
                }
        }
    }
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = getSystemService(context, ConnectivityManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(network)
            return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            val activeNetworkInfo = connectivityManager?.activeNetworkInfo
            return activeNetworkInfo?.isConnected == true
        }
    }

// Inside your ForumViewModel or where you're saving posts

    fun clearRoomDatabaseIfOnline(context: Context, dao: ForumDao) {
        if (isNetworkAvailable(context)) {
            viewModelScope.launch {
                dao.clearAllForumPosts() // Call suspend function inside a coroutine
            }
        }
    }

}



