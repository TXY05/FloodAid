package com.example.floodaid.screen.forum

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.floodaid.viewmodel.ForumViewModel
import androidx.lifecycle.viewmodel.CreationExtras

class ForumViewModelFactory(
    private val dao: ForumDao,
    private val application: Application,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return ForumViewModel(application, dao) as T
    }
}


