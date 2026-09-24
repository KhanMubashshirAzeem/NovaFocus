package com.mubashshir.novafocus.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppItemTest {

    @Test
    fun `firstChar extracts uppercase letter properly`() {
        val app1 = AppItem(id = "1", label = "WhatsApp", packageName = "com.whatsapp", activityName = "MainActivity")
        val app2 = AppItem(id = "2", label = "chrome", packageName = "com.android.chrome", activityName = "MainActivity")
        val app3 = AppItem(id = "3", label = "1Password", packageName = "com.onepassword", activityName = "MainActivity")

        assertEquals('W', app1.firstChar)
        assertEquals('C', app2.firstChar)
        assertEquals('#', app3.firstChar)
    }

    @Test
    fun `ScrubberItem ALL_ITEMS contains Star, A-Z and Dot`() {
        val items = ScrubberItem.ALL_ITEMS
        assertEquals(28, items.size)
        assertEquals(ScrubberItem.Star, items.first())
        assertEquals(ScrubberItem.Dot, items.last())

        // Check A-Z sequence
        val letters = items.subList(1, 27).mapNotNull { it.letterChar }
        assertEquals(26, letters.size)
        assertEquals('A', letters.first())
        assertEquals('Z', letters.last())

        assertNull(items.first().letterChar)
        assertNull(items.last().letterChar)
    }
}
