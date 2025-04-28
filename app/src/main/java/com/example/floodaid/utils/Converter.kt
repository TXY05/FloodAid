package com.example.floodaid.utils

import androidx.room.TypeConverter
import com.example.floodaid.roomDatabase.Entities.Border
import com.google.gson.Gson
import java.time.Instant

class Converter {

    @TypeConverter
    fun fromBorder(value: Border?): String? = Gson().toJson(value)

    @TypeConverter
    fun toBorder(value: String?): Border? =
        Gson().fromJson(value, Border::class.java)

    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }
}