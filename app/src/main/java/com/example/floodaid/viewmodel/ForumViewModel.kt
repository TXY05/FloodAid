package com.example.floodaid.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodaid.screen.forum.ForumDao
import com.example.floodaid.screen.forum.ForumEvent
import com.example.floodaid.screen.forum.ForumPost
import com.example.floodaid.screen.forum.ForumSortType
import com.example.floodaid.screen.forum.ForumState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Suppress("DEPRECATION")
@OptIn(ExperimentalCoroutinesApi::class)
class ForumViewModel(
    private val dao: ForumDao,
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _sortType = MutableStateFlow(ForumSortType.TIME_STAMP)
    private val _forumPosts = _sortType
        .flatMapLatest { sortType ->
            when (sortType) {
                ForumSortType.REGION -> dao.getForumPostFromState("")
                ForumSortType.TIME_STAMP -> dao.getForumPostOrderedByTimeStamp()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
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
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) return@launch

                    val profileBase64 = getCurrentUserProfileBase64() ?: ""
                    val postWithAuthorImage = event.forumPost.copy(
                        authorId = uid,
                        authorImageBase64 = profileBase64,
                        timestamp = System.currentTimeMillis()
                    )

                    dao.insertForumPost(postWithAuthorImage)
                    _state.update { ForumState() }

                    try {
                        val documentReference = FirebaseFirestore.getInstance()
                            .collection("forumPosts")
                            .add(postWithAuthorImage)
                            .await()

                        Log.d("Firestore", "Post saved with ID: ${documentReference.id}")
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
                    timestamp = System.currentTimeMillis(),
                    region = currentState.region,
                    imageUrls = currentState.imageUrls
                )
                viewModelScope.launch {
                    dao.upsertForumPost(forumPost)
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


}

suspend fun getCurrentUserProfileBase64(): String? {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null

    return try {
        val document = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .await()

        document.getString("profileImageBase64")
    } catch (e: Exception) {
        null
    }
}

