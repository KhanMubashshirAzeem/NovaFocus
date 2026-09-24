package com.mubashshir.novafocus.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream

object IconCache {

    private const val DIR_NAME = "fav_icons"

    fun getCachedIcon(context: Context, packageName: String): ImageBitmap? {
        return try {
            val file = File(File(context.filesDir, DIR_NAME), "${packageName}.png")
            if (file.exists() && file.length() > 0) {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun saveIcon(context: Context, packageName: String, bitmap: Bitmap) {
        try {
            val dir = File(context.filesDir, DIR_NAME)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, "${packageName}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (_: Exception) {}
    }

    fun drawableToBitmap(drawable: Drawable, targetSize: Int = 144): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else targetSize
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else targetSize
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
