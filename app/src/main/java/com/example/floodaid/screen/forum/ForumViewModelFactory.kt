package com.example.floodaid.screen.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.floodaid.viewmodel.ForumViewModel

class ForumViewModelFactory(private val dao: ForumDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForumViewModel::class.java)) {
            return ForumViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

