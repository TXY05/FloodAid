package com.example.floodaid

import android.app.Application
import com.example.floodaid.roomDatabase.database.FloodAidDatabase

class FloodAidApp : Application() {
    val database: FloodAidDatabase by lazy {
        FloodAidDatabase.Companion.getInstance(this)
    }
}