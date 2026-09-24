package com.mubashshir.novafocus.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrubberMathTest {

    @Test
    fun `displacement at exact touch location equals maxBulge`() {
        val touchY = 300f
        val itemCenterY = 300f
        val maxBulge = 80f
        val sigma = 60f

        val displacement = ScrubberMath.calculateDisplacement(itemCenterY, touchY, maxBulge, sigma)
        assertEquals(maxBulge, displacement, 0.001f)
    }

    @Test
    fun `displacement falls off symmetrically with distance`() {
        val touchY = 300f
        val maxBulge = 80f
        val sigma = 60f

        val displacementAbove = ScrubberMath.calculateDisplacement(250f, touchY, maxBulge, sigma)
        val displacementBelow = ScrubberMath.calculateDisplacement(350f, touchY, maxBulge, sigma)

        assertEquals(displacementAbove, displacementBelow, 0.001f)
        assertTrue(displacementAbove < maxBulge)
        assertTrue(displacementAbove > 0f)
    }

    @Test
    fun `displacement approaches zero beyond 3 sigma`() {
        val touchY = 300f
        val maxBulge = 80f
        val sigma = 50f

        val farDisplacement = ScrubberMath.calculateDisplacement(300f + (3.5f * sigma), touchY, maxBulge, sigma)
        assertTrue("Far displacement should be negligible, was $farDisplacement", farDisplacement < 0.2f)
    }

    @Test
    fun `resolveClosestItemIndex selects nearest item accurately`() {
        val itemCenters = listOf(100f, 150f, 200f, 250f, 300f)

        // Exact match
        assertEquals(2, ScrubberMath.resolveClosestItemIndex(200f, itemCenters))

        // Closer to item 1
        assertEquals(1, ScrubberMath.resolveClosestItemIndex(140f, itemCenters))

        // Closer to item 2
        assertEquals(2, ScrubberMath.resolveClosestItemIndex(180f, itemCenters))

        // Off top edge
        assertEquals(0, ScrubberMath.resolveClosestItemIndex(10f, itemCenters))

        // Off bottom edge
        assertEquals(4, ScrubberMath.resolveClosestItemIndex(500f, itemCenters))
    }

    @Test
    fun `calculateScale increases near peak displacement`() {
        val maxBulge = 100f

        val minScale = ScrubberMath.calculateScale(0f, maxBulge, 0.35f)
        val midScale = ScrubberMath.calculateScale(50f, maxBulge, 0.35f)
        val peakScale = ScrubberMath.calculateScale(100f, maxBulge, 0.35f)

        assertEquals(1.0f, minScale, 0.001f)
        assertEquals(1.175f, midScale, 0.001f)
        assertEquals(1.35f, peakScale, 0.001f)
    }
}
