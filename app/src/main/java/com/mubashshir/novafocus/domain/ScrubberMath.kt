package com.mubashshir.novafocus.domain

import kotlin.math.exp
import kotlin.math.pow

object ScrubberMath {

    /**
     * Calculates the horizontal displacement (inward towards left) using a Gaussian falloff curve.
     *
     * @param itemCenterY The Y coordinate of the center of the scrubber item.
     * @param touchY The current Y position of the user's touch.
     * @param maxBulgePx The maximum displacement at the exact touch location.
     * @param sigmaPx The standard deviation / falloff spread radius of the curve.
     * @return Horizontal offset in pixels (positive value indicating shift to the left).
     */
    fun calculateDisplacement(
        itemCenterY: Float,
        touchY: Float,
        maxBulgePx: Float,
        sigmaPx: Float
    ): Float {
        if (maxBulgePx <= 0f || sigmaPx <= 0f) return 0f
        val distance = itemCenterY - touchY
        val exponent = -(distance.pow(2)) / (2f * sigmaPx.pow(2))
        return (maxBulgePx * exp(exponent)).coerceAtLeast(0f)
    }

    /**
     * Finds the index of the closest scrubber item to the touch Y coordinate.
     */
    fun resolveClosestItemIndex(touchY: Float, itemCenters: List<Float>): Int {
        if (itemCenters.isEmpty()) return -1
        var closestIndex = 0
        var minDistance = Float.MAX_VALUE

        for (i in itemCenters.indices) {
            val dist = kotlin.math.abs(itemCenters[i] - touchY)
            if (dist < minDistance) {
                minDistance = dist
                closestIndex = i
            }
        }
        return closestIndex
    }

    /**
     * Calculates scale multiplier for an item based on its displacement.
     * Items at peak bulge scale up slightly for tactile feedback.
     */
    fun calculateScale(displacement: Float, maxBulgePx: Float, maxScaleIncrease: Float = 0.35f): Float {
        if (maxBulgePx <= 0f) return 1.0f
        val fraction = (displacement / maxBulgePx).coerceIn(0f, 1f)
        return 1.0f + (fraction * maxScaleIncrease)
    }
}
