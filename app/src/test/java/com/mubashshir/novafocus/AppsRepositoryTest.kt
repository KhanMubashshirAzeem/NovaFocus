package com.mubashshir.novafocus

import com.mubashshir.novafocus.data.model.AppItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class AppsRepositoryTest {

    private val sampleApps = listOf(
        AppItem(id = "1", label = "WhatsApp", packageName = "com.whatsapp", activityName = "MainActivity"),
        AppItem(id = "2", label = "Chrome", packageName = "com.android.chrome", activityName = "MainActivity"),
        AppItem(id = "3", label = "Camera", packageName = "com.android.camera", activityName = "MainActivity"),
        AppItem(id = "4", label = "Gmail", packageName = "com.google.android.gm", activityName = "MainActivity"),
        AppItem(id = "5", label = "YouTube", packageName = "com.google.android.youtube", activityName = "MainActivity")
    )

    private fun searchApps(query: String, apps: List<AppItem>): List<AppItem> {
        val trimmed = query.trim().lowercase(Locale.ROOT)
        if (trimmed.isEmpty()) return apps
        return apps.filter {
            it.label.lowercase(Locale.ROOT).contains(trimmed) ||
                    it.packageName.lowercase(Locale.ROOT).contains(trimmed)
        }
    }

    @Test
    fun `search matches by partial name case insensitive`() {
        val results = searchApps("wha", sampleApps)
        assertEquals(1, results.size)
        assertEquals("WhatsApp", results.first().label)
    }

    @Test
    fun `search matches by package name`() {
        val results = searchApps("android.gm", sampleApps)
        assertEquals(1, results.size)
        assertEquals("Gmail", results.first().label)
    }

    @Test
    fun `search with empty query returns all apps`() {
        val results = searchApps("", sampleApps)
        assertEquals(sampleApps.size, results.size)
    }

    @Test
    fun `search with non-matching query returns empty list`() {
        val results = searchApps("xyz_unknown", sampleApps)
        assertTrue(results.isEmpty())
    }
}
