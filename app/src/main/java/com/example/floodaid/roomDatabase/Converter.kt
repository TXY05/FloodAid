package com.example.floodaid.roomDatabase

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converter {

//    @TypeConverter
//    fun fromBorderCoordinates(value: List<Pair<Double, Double>>): String {
//        return Gson().toJson(value)
//    }
//
//    @TypeConverter
//    fun toBorderCoordinates(value: String): List<Pair<Double, Double>> {
//        val type = object : TypeToken<List<Pair<Double, Double>>>() {}.type
//        return Gson().fromJson(value, type)
//    }
    @TypeConverter
    fun fromBorder(value: Border?): String? = Gson().toJson(value)

    @TypeConverter
    fun toBorder(value: String?): Border? =
        Gson().fromJson(value, Border::class.java)
}