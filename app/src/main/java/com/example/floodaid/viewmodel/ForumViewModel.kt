package com.example.floodaid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodaid.screen.forum.ForumDao
import com.example.floodaid.models.ForumPost
import com.example.floodaid.screen.forum.ForumEvent
import com.example.floodaid.screen.forum.ForumPostState
import com.example.floodaid.screen.forum.ForumSortType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    private val _state = MutableStateFlow(ForumPostState())

    val state = combine(_state, _sortType, _forumPosts) { state, sortType, forumPosts ->
        state.copy(
            forumPost = forumPosts,
            sortType = sortType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ForumPostState())

    fun onEvent(event: ForumEvent) {
        when (event) {
            is ForumEvent.DeleteForumPost -> {
                viewModelScope.launch {
                    dao.deleteForumPost(event.forumPost)
                }
            }

            ForumEvent.SaveForumPost -> {
                val title = state.value.title
                val content = state.value.content

                if(title.isBlank()||content.isBlank()){
                    return
                }

                val forumPost = ForumPost(
                    title = title,
                    content = content,
                    authorId = "", //link to user id
                    timestamp = System.currentTimeMillis(),
                    region = TODO()

                )
                viewModelScope.launch {
                    dao.upsertForumPost(forumPost)
                }
                _state.update { it.copy(
                    title="",
                    content = "",
                    authorId = "",
                    timestamp = 0,
                    region = ""
                ) }
            }

            is ForumEvent.SetContent -> {
                _state.update {
                    it.copy(
                        content = event.content
                    )
                }
            }

            is ForumEvent.SetTitle -> {
                _state.update {
                    it.copy(
                        title = event.title
                    )
                }
            }

            is ForumEvent.SortForumPost -> {
                _sortType.value = event.sortType
            }
        }
    }


}
