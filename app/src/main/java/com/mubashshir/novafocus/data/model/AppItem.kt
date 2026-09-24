package com.mubashshir.novafocus.data.model

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Represents a launchable application installed on the device.
 */
data class AppItem(
    val id: String,
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable? = null,
    val iconBitmap: ImageBitmap? = null,
    val firstChar: Char = label.firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' } ?: '#'
)
