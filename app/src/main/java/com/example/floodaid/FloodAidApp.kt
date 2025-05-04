package com.example.floodaid

import android.app.Application
import com.example.floodaid.roomDatabase.Database.FloodAidDatabase

class FloodAidApp : Application() {
    val database: FloodAidDatabase by lazy {
        FloodAidDatabase.Companion.getInstance(this)
    }
}