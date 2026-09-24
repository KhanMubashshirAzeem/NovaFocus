package com.mubashshir.novafocus.ui.components

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mubashshir.novafocus.data.model.ScrubberItem
import com.mubashshir.novafocus.domain.ScrubberMath
import com.mubashshir.novafocus.ui.theme.LauncherDimensions
import com.mubashshir.novafocus.ui.theme.ScrubberBubbleBg
import com.mubashshir.novafocus.ui.theme.ScrubberBubbleBorder
import com.mubashshir.novafocus.ui.theme.ScrubberInactive
import kotlinx.coroutines.launch

@Composable
fun AlphabetScrubber(
    selectedLetter: Char?,
    onLetterSelected: (Char?) -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val maxBulgePx = with(density) { LauncherDimensions.ScrubberMaxBulge.toPx() }
    val sigmaPx = with(density) { LauncherDimensions.ScrubberFalloffSigma.toPx() }
    val bubbleRadiusPx = with(density) { (LauncherDimensions.BubbleSize / 2).toPx() }
    val bubbleDistanceOffsetPx = with(density) { LauncherDimensions.BubbleDistanceOffset.toPx() }
    val scrubberFontSizePx = with(density) { LauncherDimensions.ScrubberFontSize.toPx() }
    val bubbleFontSizePx = with(density) { LauncherDimensions.BubbleFontSize.toPx() }
    val marginEndPx = with(density) { LauncherDimensions.ScrubberMarginEnd.toPx() }

    val bulgeAnimatable = remember { Animatable(0f) }
    var touchY by remember { mutableFloatStateOf(0f) }
    var isTouching by remember { mutableStateOf(false) }
    var currentBubbleSymbol by remember { mutableStateOf<String?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(12L)
            }
        } catch (_: Exception) {}
    }

    val items = ScrubberItem.ALL_ITEMS

    // Paint objects reused for 120fps zero-allocation canvas rendering
    val itemPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
    }

    val bubbleTextPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            color = android.graphics.Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    fun getItemCenterYs(height: Float): List<Float> {
        val verticalPadding = height * 0.08f
        val availableHeight = height - (verticalPadding * 2f)
        val slotHeight = availableHeight / items.size
        return items.indices.map { i ->
            verticalPadding + (i + 0.5f) * slotHeight
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .onSizeChanged { canvasSize = it },
        contentAlignment = Alignment.CenterEnd
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        isTouching = true
                        touchY = down.position.y

                        coroutineScope.launch {
                            bulgeAnimatable.animateTo(
                                targetValue = maxBulgePx,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }

                        val height = size.height.toFloat()
                        val itemCenterYs = getItemCenterYs(height)
                        var lastIdx = ScrubberMath.resolveClosestItemIndex(down.position.y, itemCenterYs)
                        if (lastIdx != -1) {
                            val item = items[lastIdx]
                            currentBubbleSymbol = item.symbol
                            onLetterSelected(item.letterChar)
                            triggerHaptic()
                        }

                        val pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) {
                                break
                            }
                            change.consume()
                            touchY = change.position.y
                            val newIdx = ScrubberMath.resolveClosestItemIndex(touchY, itemCenterYs)
                            if (newIdx != -1 && newIdx != lastIdx) {
                                lastIdx = newIdx
                                val item = items[newIdx]
                                currentBubbleSymbol = item.symbol
                                onLetterSelected(item.letterChar)
                                triggerHaptic()
                            }
                        }

                        // Finger lifted
                        isTouching = false
                        onRelease()
                        coroutineScope.launch {
                            bulgeAnimatable.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = 0.52f, // Organic spring overshoot
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            val verticalPadding = height * 0.08f
            val availableHeight = height - (verticalPadding * 2f)
            val slotHeight = availableHeight / items.size
            val baseX = width - marginEndPx - (scrubberFontSizePx / 2f)

            val currentBulge = bulgeAnimatable.value
            val itemCenterYs = items.indices.map { i ->
                verticalPadding + (i + 0.5f) * slotHeight
            }

            // Draw each scrubber item along the dynamic Gaussian curve
            for (i in items.indices) {
                val item = items[i]
                val itemCenterY = itemCenterYs[i]

                val displacement = if (currentBulge > 0f) {
                    ScrubberMath.calculateDisplacement(itemCenterY, touchY, currentBulge, sigmaPx)
                } else {
                    0f
                }

                val itemX = baseX - displacement
                val scale = ScrubberMath.calculateScale(displacement, maxBulgePx, 0.35f)

                itemPaint.textSize = scrubberFontSizePx * scale

                // Highlight rule:
                // During active drag: highlight letters near finger peak displacement
                // In resting state: highlight the currently active letter (or star if on home)
                val isHighlighted = if (currentBulge > maxBulgePx * 0.2f) {
                    displacement > maxBulgePx * 0.45f
                } else {
                    (selectedLetter == null && item is ScrubberItem.Star) ||
                            (selectedLetter != null && item.letterChar == selectedLetter)
                }

                if (isHighlighted) {
                    itemPaint.color = android.graphics.Color.WHITE
                    itemPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                } else {
                    itemPaint.color = ScrubberInactive.toArgb()
                    itemPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }

                val baselineY = itemCenterY - ((itemPaint.descent() + itemPaint.ascent()) / 2f)

                drawContext.canvas.nativeCanvas.drawText(
                    item.symbol,
                    itemX,
                    baselineY,
                    itemPaint
                )
            }

            // Draw the enlarged Letter Bubble next to the finger when scrubbing
            val displaySymbol = currentBubbleSymbol ?: selectedLetter?.toString()
            if (currentBulge > 5f && displaySymbol != null) {
                val progress = (currentBulge / maxBulgePx).coerceIn(0f, 1f)
                val bubbleCenterX = baseX - currentBulge - bubbleRadiusPx - bubbleDistanceOffsetPx
                val bubbleCenterY = touchY.coerceIn(
                    verticalPadding + bubbleRadiusPx,
                    height - verticalPadding - bubbleRadiusPx
                )

                // Translucent bubble circular background
                drawCircle(
                    color = ScrubberBubbleBg.copy(alpha = 0.92f * progress),
                    radius = bubbleRadiusPx,
                    center = Offset(bubbleCenterX, bubbleCenterY)
                )

                // Subtle border
                drawCircle(
                    color = ScrubberBubbleBorder.copy(alpha = 0.4f * progress),
                    radius = bubbleRadiusPx,
                    center = Offset(bubbleCenterX, bubbleCenterY),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Symbol inside bubble
                bubbleTextPaint.textSize = bubbleFontSizePx
                bubbleTextPaint.alpha = (255 * progress).toInt()
                val bubbleBaselineY = bubbleCenterY - ((bubbleTextPaint.descent() + bubbleTextPaint.ascent()) / 2f)

                drawContext.canvas.nativeCanvas.drawText(
                    displaySymbol,
                    bubbleCenterX,
                    bubbleBaselineY,
                    bubbleTextPaint
                )
            }
        }
    }
}
