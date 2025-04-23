package com.example.floodaid.roomDatabase

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converter {

    @TypeConverter
    fun fromBorder(value: Border?): String? = Gson().toJson(value)

    @TypeConverter
    fun toBorder(value: String?): Border? =
        Gson().fromJson(value, Border::class.java)
}