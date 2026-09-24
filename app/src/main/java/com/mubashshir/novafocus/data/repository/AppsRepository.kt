package com.mubashshir.novafocus.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.mubashshir.novafocus.data.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

interface AppsRepository {
    suspend fun getInstalledApps(context: Context, forceReload: Boolean = false): List<AppItem>
    suspend fun getSmartFavorites(context: Context): List<AppItem>
    fun getAppsStartingWith(letter: Char): List<AppItem>
    fun searchApps(query: String): List<AppItem>
    fun launchApp(context: Context, app: AppItem): Boolean
}

class DefaultAppsRepository : AppsRepository {

    private var cachedApps: List<AppItem>? = null
    private var appsByLetter: Map<Char, List<AppItem>> = emptyMap()

    // Common favorite candidates in priority order
    private val preferredFavorites = listOf(
        "WhatsApp",
        "Chrome",
        "Camera",
        "ChatGPT",
        "CRED",
        "Calculator",
        "Gmail",
        "YouTube",
        "Spotify",
        "Maps",
        "Phone",
        "Messages"
    )

    override suspend fun getInstalledApps(context: Context, forceReload: Boolean): List<AppItem> {
        return withContext(Dispatchers.IO) {
            val existing = cachedApps
            if (!forceReload && existing != null) {
                return@withContext existing
            }

            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfoList = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(mainIntent, 0)
            }

            val myPackageName = context.packageName

            val apps = resolveInfoList
                .filter { it.activityInfo != null && it.activityInfo.packageName != myPackageName }
                .map { info ->
                    val label = info.loadLabel(pm)?.toString()?.trim() ?: info.activityInfo.name
                    val pkgName = info.activityInfo.packageName
                    val actName = info.activityInfo.name
                    val icon = try {
                        info.loadIcon(pm)
                    } catch (_: Exception) {
                        null
                    }
                    val iconBitmap = icon?.let { drawableToImageBitmap(it) }

                    AppItem(
                        id = "$pkgName/$actName",
                        label = label,
                        packageName = pkgName,
                        activityName = actName,
                        icon = icon,
                        iconBitmap = iconBitmap
                    )
                }
                .distinctBy { it.packageName }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })

            cachedApps = apps
            appsByLetter = apps.groupBy { it.firstChar }

            apps
        }
    }

    override suspend fun getSmartFavorites(context: Context): List<AppItem> {
        val allApps = getInstalledApps(context)
        if (allApps.isEmpty()) return emptyList()

        val matchedFavorites = mutableListOf<AppItem>()

        // 1. First look for preferred popular apps matching by label or package
        for (preferred in preferredFavorites) {
            val match = allApps.firstOrNull { app ->
                app.label.equals(preferred, ignoreCase = true) ||
                        app.packageName.contains(preferred, ignoreCase = true)
            }
            if (match != null && !matchedFavorites.contains(match)) {
                matchedFavorites.add(match)
            }
            if (matchedFavorites.size >= 7) break
        }

        // 2. Fall back / fill with first installed apps if fewer than 5-7 found
        if (matchedFavorites.size < 7) {
            for (app in allApps) {
                if (!matchedFavorites.contains(app)) {
                    matchedFavorites.add(app)
                }
                if (matchedFavorites.size >= 7) break
            }
        }

        return matchedFavorites
    }

    override fun getAppsStartingWith(letter: Char): List<AppItem> {
        val upper = letter.uppercaseChar()
        return appsByLetter[upper] ?: emptyList()
    }

    override fun searchApps(query: String): List<AppItem> {
        val current = cachedApps ?: return emptyList()
        val trimmed = query.trim().lowercase(Locale.ROOT)
        if (trimmed.isEmpty()) return current
        return current.filter {
            it.label.lowercase(Locale.ROOT).contains(trimmed) ||
                    it.packageName.lowercase(Locale.ROOT).contains(trimmed)
        }
    }

    override fun launchApp(context: Context, app: AppItem): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(app.packageName, app.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    true
                } else {
                    false
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun drawableToImageBitmap(drawable: Drawable, targetSize: Int = 144): ImageBitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
            return drawable.bitmap.asImageBitmap()
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else targetSize
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else targetSize
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap.asImageBitmap()
    }
}
