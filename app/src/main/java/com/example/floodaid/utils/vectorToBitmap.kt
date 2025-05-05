package com.example.floodaid.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

data class BitmapParameters(
    val id: Int,
    val iconColor: Color,
    val backgroundColor: Color,
    val size: Int = 48 // Default size in dp
)

fun vectorToBitmap(context: Context, parameters: BitmapParameters): Bitmap {
    val drawable = ContextCompat.getDrawable(context, parameters.id) ?: throw IllegalArgumentException("Drawable not found")
    val wrappedDrawable = DrawableCompat.wrap(drawable)
    DrawableCompat.setTint(wrappedDrawable, parameters.iconColor.toArgb())

    val bitmap = Bitmap.createBitmap(
        parameters.size,
        parameters.size,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    canvas.drawColor(parameters.backgroundColor.toArgb())
    wrappedDrawable.setBounds(0, 0, canvas.width, canvas.height)
    wrappedDrawable.draw(canvas)

    return bitmap
}